package kr.jadekim.jext.apm.es.ktor

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Transaction
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.withContext
import kr.jadekim.jext.apm.es.EsApmContext
import kr.jadekim.jext.ktor.module.KtorModule
import kr.jadekim.jext.ktor.module.KtorModuleConfiguration
import kr.jadekim.jext.ktor.module.KtorModuleFactory
import kr.jadekim.jext.ktor.module.ktorModule
import kotlin.coroutines.CoroutineContext

object EsApmServerModule : KtorModuleFactory<EsApmServerModule.Configuration> {

    private val defaultConfiguration = EsApmServerPlugin.Configuration()

    class Configuration : KtorModuleConfiguration {
        var transactionName: (ApplicationCall) -> String = defaultConfiguration.transactionName
        var setupTransaction: Transaction.(ApplicationCall, CoroutineContext) -> Unit =
            defaultConfiguration.setupTransaction
        var shouldTrace: (ApplicationCall) -> Boolean = defaultConfiguration.shouldTrace
    }

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(EsApmServerPlugin) {
            transactionName = config.transactionName
            setupTransaction = config.setupTransaction
            shouldTrace = config.shouldTrace
        }
    }

    override fun createDefaultConfiguration() = Configuration()
}

class EsApmServerPlugin private constructor(configuration: Configuration) {

    class Configuration {
        var transactionName: (ApplicationCall) -> String = { it.attributes[ATTRIBUTE_ROUTE] }
        var setupTransaction: Transaction.(ApplicationCall, CoroutineContext) -> Unit = { _, coroutineContext ->
            if (isInstalledJLogger) {
                coroutineContext[kr.jadekim.logger.coroutine.context.CoroutineLogContext]?.forEach { key, value ->
                    when (value) {
                        is Boolean -> addCustomContext(key, value)
                        is Number -> addCustomContext(key, value)
                        else -> addCustomContext(key, value.toString())
                    }
                }
            }
        }
        var shouldTrace: (ApplicationCall) -> Boolean = { true }
    }

    val transactionName = configuration.transactionName
    val setupTransaction = configuration.setupTransaction
    val shouldTrace = configuration.shouldTrace

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, EsApmServerPlugin> {

        override val key = AttributeKey<EsApmServerPlugin>("EsApmIntegration")

        val ATTRIBUTE_ROUTE = AttributeKey<String>("EsApmIntegration.route")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): EsApmServerPlugin {
            val plugin = EsApmServerPlugin(Configuration().apply(configure))

            pipeline.environment!!.monitor.subscribe(Routing.RoutingCallStarted) {
                it.attributes.put(ATTRIBUTE_ROUTE, it.route.toString())
            }

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                if (!plugin.shouldTrace(context)) {
                    return@intercept
                }

                val transaction = ElasticApm.startTransactionWithRemoteParent { context.request.header(it) }

                var apmCoroutineContext = coroutineContext

                if (isInstalledJLogger) {
                    val logContext = apmCoroutineContext[kr.jadekim.logger.coroutine.context.CoroutineLogContext]
                        ?: kr.jadekim.logger.coroutine.context.CoroutineLogContext()

                    logContext["tracing"] = mapOf(
                        "traceId" to transaction.traceId,
                        "transactionId" to transaction.id,
                    )

                    apmCoroutineContext += logContext
                }

                transaction.setFrameworkName("ktor")
                apmCoroutineContext += EsApmContext(transaction)

                try {
                    withContext(apmCoroutineContext) {
                        proceed()
                    }
                } catch (e: Throwable) {
                    transaction.captureException(e)

                    throw e
                } finally {
                    transaction.setName(plugin.transactionName(context))
                    plugin.setupTransaction(transaction, context, apmCoroutineContext)

                    transaction.end()
                }
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