package kr.jadekim.jext.ktor.module

import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.endpoints.CohortConfiguration
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kr.jadekim.jext.ktor.hook.PhaseHook
import kr.jadekim.logger.JLog
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.LogLevel
import kr.jadekim.logger.context.MutableLogContext
import kr.jadekim.logger.coroutine.sDebug
import kr.jadekim.logger.integration.ktor.JLogContext
import kr.jadekim.logger.integration.ktor.RequestLogger

object MonitoringModule : KtorModuleDefinition<MonitoringModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var enableRestriction: Boolean = false
        internal var restriction: (RestrictionConfiguration.() -> Unit)? = null

        fun restriction(configure: RestrictionConfiguration.() -> Unit) {
            restriction = configure
        }

        var configureCallId: CallIdConfig.() -> Unit = {
            retrieveFromHeader(HttpHeaders.XRequestId)
            retrieveFromHeader("traceparent")
            retrieveFromHeader("cf-ray")
        }

        // if null, would be not logged.
        var logLevel: (ApplicationCall) -> LogLevel? = { LogLevel.INFO }
        var logContext: ApplicationCall.(MutableLogContext) -> Unit = {}
        var logMeta: ApplicationCall.(MutableMap<String, Any?>) -> Unit = {}
        var canLogBody: ApplicationCall.() -> Boolean = { false }
        var logger: JLogger = JLog.get("RequestLogger")

        var configureCohort: CohortConfiguration.() -> Unit = {}

        var prometheusRegistry: PrometheusMeterRegistry? = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        var prometheusEndpoint: String = "/metrics"
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller =
        KtorApplicationModuleInstaller {
            install(CallId, config.configureCallId)

            install(JLogContext) {
                setupContext = config.logContext
            }

            install(RequestLogger) {
                logLevel = config.logLevel
                canLogBody = config.canLogBody
                additionalMeta = config.logMeta
                logger = config.logger
            }

            install(Cohort) {
                operatingSystem = config.enableRestriction
                jvmInfo = config.enableRestriction
                sysprops = config.enableRestriction
                heapDump = config.enableRestriction
                threadDump = config.enableRestriction
                verboseHealthCheckResponse = config.enableRestriction

                config.configureCohort(this)
            }

            if (config.prometheusRegistry != null) {
                install(MicrometerMetrics) {
                    registry = config.prometheusRegistry!!
                }

                routing {
                    get(config.prometheusEndpoint) {
                        call.respond(config.prometheusRegistry?.scrape() ?: "")
                    }
                }
            }

            if (config.enableRestriction) {
                install(AccessRestriction, config.restriction!!)
            }

            //todo: OpenTelemetry
        }

    override fun createDefaultConfiguration(): Configuration = Configuration()

    enum class PortRestrictionMode {
        NOT_FOUND,
        FORBIDDEN,
        SUCCESS,
        ALLOW,
    }

    class RestrictionConfiguration {
        var mode: PortRestrictionMode = PortRestrictionMode.SUCCESS
        internal var restrictPaths: MutableList<String> =
            mutableListOf("/metrics", "/cohort", "/liveness", "/readiness", "/health", "/healthcheck")
        var allowPorts: List<Int>? = null

        var logger: JLogger = JLog.get("MonitoringAccessRestriction")

        fun startsWith(path: String) {
            restrictPaths.add(path)
        }
    }

    private val AccessRestriction = createApplicationPlugin("MonitoringAccessRestriction", ::RestrictionConfiguration) {
        val key = AttributeKey<Boolean>("AppliedMonitoringAccessRestriction")

        on(PhaseHook(ApplicationCallPipeline.Plugins)) {
            val allowPorts = pluginConfig.allowPorts ?: return@on
            if (call.request.port() in allowPorts) return@on
            val path = call.request.path()
            if (!pluginConfig.restrictPaths.any { path.startsWith(it) }) return@on

            call.attributes.put(key, true)

            pluginConfig.logger.sDebug("Ignored Access")

            when (pluginConfig.mode) {
                PortRestrictionMode.NOT_FOUND -> call.respond(HttpStatusCode.NotFound)
                PortRestrictionMode.FORBIDDEN -> call.respond(HttpStatusCode.Forbidden)
                PortRestrictionMode.SUCCESS, PortRestrictionMode.ALLOW -> proceed()
            }
        }

        onCallRespond { call, _ ->
            if (call.attributes.getOrNull(key) != true) return@onCallRespond

            transformBody {
                if (call.response.status()?.isSuccess() == false) "not healthy" else "ok"
            }
        }
    }
}