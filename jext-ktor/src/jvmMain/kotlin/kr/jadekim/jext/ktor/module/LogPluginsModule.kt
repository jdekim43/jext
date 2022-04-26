package kr.jadekim.jext.ktor.module

import io.ktor.server.application.*
import kr.jadekim.logger.JLog
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.LogLevel
import kr.jadekim.logger.context.MutableLogContext
import kr.jadekim.logger.integration.ktor.JLogContext
import kr.jadekim.logger.integration.ktor.RequestLogger
import kr.jadekim.logger.integration.ktor.defaultLogLevel

object LogPluginsModule : KtorModuleFactory<LogPluginsModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var ignoreRequestBodyLog: ApplicationCall.() -> Boolean = { false }
        var requestLogContext: ApplicationCall.(MutableLogContext) -> Unit = {}
        var logger: JLogger = JLog.get("RequestLogger")
        var logLevel: ApplicationCall.(Throwable?) -> LogLevel = { response.status()?.defaultLogLevel ?: LogLevel.INFO }
    }

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(JLogContext)

        install(RequestLogger) {
            canLogBody = config.ignoreRequestBodyLog
            logContext = config.requestLogContext
            logger = config.logger
            logLevel = config.logLevel
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}