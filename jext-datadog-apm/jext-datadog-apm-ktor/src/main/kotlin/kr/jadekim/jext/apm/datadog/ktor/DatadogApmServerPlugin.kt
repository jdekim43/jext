package kr.jadekim.jext.apm.datadog.ktor

import io.ktor.server.application.*
import io.ktor.server.logging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.util.GlobalTracer
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kr.jadekim.jext.apm.datadog.*
import kr.jadekim.jext.apm.datadog.ktor.adapter.asTextMap
import kr.jadekim.jext.ktor.hook.PhaseHook
import kr.jadekim.jext.ktor.module.KtorApplicationModuleInstaller
import kr.jadekim.jext.ktor.module.KtorModuleConfiguration
import kr.jadekim.jext.ktor.module.KtorModuleDefinition
import kotlin.coroutines.coroutineContext

object DatadogApmServerModule : KtorModuleDefinition<DatadogApmServerModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var tracer: Tracer = GlobalTracer.get()
        var getSpanName: (ApplicationCall) -> String = {
            when (it) {
                is RoutingCall -> it.route.toString()
                is RoutingPipelineCall -> it.route.toString()
                else -> it.request.path()
            }
        }
        var finalizeSpan: Span.(ApplicationCall, Map<String, Any?>) -> Unit = { _, logContext ->
            logContext.forEach { (key, value) ->
                when (value) {
                    is Boolean -> setTag(key, value)
                    is Number -> setTag(key, value)
                    else -> setTag(key, value.toString())
                }
            }
        }
        var shouldTrace: (ApplicationCall) -> Boolean = { true }
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller = KtorApplicationModuleInstaller {
        install(Plugin) {
            tracer = config.tracer
            getSpanName = config.getSpanName
            finalizeSpan = config.finalizeSpan
            shouldTrace = config.shouldTrace
        }
    }

    override fun createDefaultConfiguration() = Configuration()

    val Plugin = createRouteScopedPlugin("DatadogApmServerPlugin", { createDefaultConfiguration() }) {

        fun ApplicationRequest.extractSpan(tracer: Tracer, spanName: String): Span {
            return tracer.span(spanName) {
                this.extractSpan(headers.asTextMap(), Format.Builtin.HTTP_HEADERS, tracer)
            }
        }

        fun ApplicationResponse.injectSpan(tracer: Tracer, span: Span) {
            if (!this.isCommitted) {
                tracer.inject(span.context, Format.Builtin.HTTP_HEADERS, headers.asTextMap())
            }
        }

        on(PhaseHook(ApplicationCallPipeline.Monitoring)) {
            if (!pluginConfig.shouldTrace(call)) {
                return@on
            }

            val span = call.request.extractSpan(pluginConfig.tracer, call.request.toLogString())

            var apmCoroutineContext = currentCoroutineContext()
            var logContext = emptyMap<String, Any?>()

            if (isInstalledJLogger) {
                val coroutineLogContext =
                    kr.jadekim.logger.coroutine.context.CoroutineLogContext.get(apmCoroutineContext)

                coroutineLogContext["tracing"] = mapOf(
                    "traceId" to span.context.toTraceId(),
                    "spanId" to span.context.toSpanId(),
                    "parentIdInHeader" to call.request.headers["x-datadog-parent-id"],
                    "traceIdInHeader" to call.request.headers["x-datadog-trace-id"],
                )

                apmCoroutineContext += coroutineLogContext
                logContext = coroutineLogContext
            }

            span.setOperationName(pluginConfig.getSpanName(call))

            withContext(apmCoroutineContext) {
                withApm(pluginConfig.tracer, getSpan = { span }) {
                    try {
                        proceed()
                    } finally {
                        pluginConfig.finalizeSpan(span, call, logContext)

                        span.finish()
                    }
                }
            }
        }

        on(PhaseHook(ApplicationCallPipeline.Call)) {
            if (!pluginConfig.shouldTrace(call)) {
                return@on
            }

            (coroutineContext.datadogApmContext.span)?.let {
                call.response.injectSpan(pluginConfig.tracer, it)
            }
        }
    }
}

private val isInstalledJLogger: Boolean = try {
    Class.forName("kr.jadekim.logger.JLogger")
    true
} catch (e: ClassNotFoundException) {
    false
}