package kr.jadekim.jext.apm.es.ktor

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Transaction
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kr.jadekim.jext.apm.es.EsApmContext
import kr.jadekim.jext.ktor.hook.PhaseHook
import kr.jadekim.jext.ktor.module.KtorApplicationModuleInstaller
import kr.jadekim.jext.ktor.module.KtorModuleConfiguration
import kr.jadekim.jext.ktor.module.KtorModuleDefinition

object EsApmServerModule : KtorModuleDefinition<EsApmServerModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var transactionName: (ApplicationCall) -> String = {
            when (it) {
                is RoutingCall -> it.route.toString()
                is RoutingPipelineCall -> it.route.toString()
                else -> it.request.path()
            }
        }
        var finalizeTransaction: Transaction.(ApplicationCall, Map<String, Any?>) -> Unit = { _, logContext ->
            logContext.forEach { (key, value) ->
                when (value) {
                    is Boolean -> addCustomContext(key, value)
                    is Number -> addCustomContext(key, value)
                    else -> addCustomContext(key, value.toString())
                }
            }
        }
        var shouldTrace: (ApplicationCall) -> Boolean = { true }
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller = KtorApplicationModuleInstaller {
        install(Plugin) {
            transactionName = config.transactionName
            finalizeTransaction = config.finalizeTransaction
            shouldTrace = config.shouldTrace
        }
    }

    override fun createDefaultConfiguration() = Configuration()

    val Plugin = createRouteScopedPlugin(
        "EsApmIntegration",
        { Configuration() },
    ) {
        on(PhaseHook(ApplicationCallPipeline.Monitoring)) {
            if (!pluginConfig.shouldTrace(call)) {
                return@on
            }

            val transaction = ElasticApm.startTransactionWithRemoteParent { call.request.header(it) }

            var apmCoroutineContext = currentCoroutineContext()
            var logContext = emptyMap<String, Any?>()

            if (isInstalledJLogger) {
                val coroutineLogContext =
                    kr.jadekim.logger.coroutine.context.CoroutineLogContext.get(apmCoroutineContext)

                coroutineLogContext["tracing"] = mapOf(
                    "traceId" to transaction.traceId,
                    "transactionId" to transaction.id,
                )

                apmCoroutineContext += coroutineLogContext
                logContext = coroutineLogContext
            }

            transaction.setName(pluginConfig.transactionName(call))
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
                pluginConfig.finalizeTransaction(transaction, call, logContext)

                transaction.end()
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