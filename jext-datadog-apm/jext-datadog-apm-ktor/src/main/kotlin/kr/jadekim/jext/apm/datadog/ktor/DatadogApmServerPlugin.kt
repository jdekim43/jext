package kr.jadekim.jext.apm.datadog.ktor

import io.ktor.server.application.*
import io.ktor.server.logging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.util.GlobalTracer
import kotlinx.coroutines.withContext
import kr.jadekim.jext.apm.datadog.*
import kr.jadekim.jext.apm.datadog.ktor.adapter.asTextMap
import kr.jadekim.jext.ktor.module.KtorModule
import kr.jadekim.jext.ktor.module.KtorModuleConfiguration
import kr.jadekim.jext.ktor.module.KtorModuleFactory
import kr.jadekim.jext.ktor.module.ktorModule
import kotlin.coroutines.CoroutineContext

object DatadogApmServerModule : KtorModuleFactory<DatadogApmServerModule.Configuration> {

    private val defaultConfiguration = DatadogApmServerPlugin.Configuration()

    class Configuration : KtorModuleConfiguration {
        var getSpanName: (ApplicationCall) -> String = defaultConfiguration.getSpanName
        var setupSpan: Span.(ApplicationCall, CoroutineContext) -> Unit = defaultConfiguration.setupSpan
        var shouldTrace: (ApplicationCall) -> Boolean = defaultConfiguration.shouldTrace
    }

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(DatadogApmServerPlugin) {
            getSpanName = config.getSpanName
            setupSpan = config.setupSpan
            shouldTrace = config.shouldTrace
        }
    }

    override fun createDefaultConfiguration() = Configuration()
}

class DatadogApmServerPlugin private constructor(configuration: Configuration) {

    class Configuration {
        var tracer: Tracer = GlobalTracer.get()
        var getSpanName: (ApplicationCall) -> String = { it.attributes.getOrNull(ATTRIBUTE_ROUTE) ?: it.request.path() }
        var setupSpan: Span.(ApplicationCall, CoroutineContext) -> Unit = { _, coroutineContext ->
            if (isInstalledJLogger) {
                coroutineContext[kr.jadekim.logger.coroutine.context.CoroutineLogContext]?.forEach { key, value ->
                    when (value) {
                        is Boolean -> setTag(key, value)
                        is Number -> setTag(key, value)
                        else -> setTag(key, value.toString())
                    }
                }
            }
        }
        var shouldTrace: (ApplicationCall) -> Boolean = { true }
    }

    val tracer = configuration.tracer
    val getSpanName = configuration.getSpanName
    val setupSpan = configuration.setupSpan
    val shouldTrace = configuration.shouldTrace

    private suspend fun PipelineContext<Unit, ApplicationCall>.injectRootSpan() {
        val span = context.request.extractSpan(tracer, context.request.toLogString())
        var apmCoroutineContext = coroutineContext

        if (isInstalledJLogger) {
            val logContext = apmCoroutineContext[kr.jadekim.logger.coroutine.context.CoroutineLogContext]
                ?: kr.jadekim.logger.coroutine.context.CoroutineLogContext()

            logContext["tracing"] = mapOf(
                "traceId" to span.context.toTraceId(),
                "spanId" to span.context.toSpanId(),
                "parentIdInHeader" to context.request.headers["x-datadog-parent-id"],
                "traceIdInHeader" to context.request.headers["x-datadog-trace-id"],
            )

            apmCoroutineContext += logContext
        }

        span.setupSpan(context, apmCoroutineContext)

        withContext(apmCoroutineContext) {
            withApm(tracer, getSpan = { span }) {
                proceed()
            }
        }
    }

    private fun PipelineContext<Unit, ApplicationCall>.injectSpanToHeaders() {
        (coroutineContext.datadogApmContext.span)?.let {
            it.setOperationName(getSpanName(context))
            context.response.injectSpan(tracer, it)
        }
    }

    private fun ApplicationRequest.extractSpan(tracer: Tracer, spanName: String): Span {
        return tracer.span(spanName) {
            this.extractSpan(headers.asTextMap(), Format.Builtin.HTTP_HEADERS, tracer)
        }
    }

    private fun ApplicationResponse.injectSpan(tracer: Tracer, span: Span) {
        if (!this.isCommitted) {
            tracer.inject(span.context, Format.Builtin.HTTP_HEADERS, headers.asTextMap())
        }
    }

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, DatadogApmServerPlugin> {

        override val key = AttributeKey<DatadogApmServerPlugin>("DatadogApmServerPlugin")

        val ATTRIBUTE_ROUTE = AttributeKey<String>("DatadogApmServerPlugin.route")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): DatadogApmServerPlugin {
            val plugin = DatadogApmServerPlugin(Configuration().apply(configure))

            pipeline.environment!!.monitor.subscribe(Routing.RoutingCallStarted) {
                it.attributes.put(ATTRIBUTE_ROUTE, it.route.toString())
            }

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                if (!plugin.shouldTrace(context)) {
                    return@intercept
                }

                with(plugin) {
                    injectRootSpan()
                }
            }

            pipeline.intercept(ApplicationCallPipeline.Call) {
                if (!plugin.shouldTrace(context)) {
                    return@intercept
                }

                with(plugin) {
                    injectSpanToHeaders()
                }

                proceed()
            }

            return plugin
        }
    }
}

private val isInstalledJLogger: Boolean = try {
    Class.forName("kr.jadekim.logger.JLogger")
    true
} catch (e: ClassNotFoundException) {
    false
}