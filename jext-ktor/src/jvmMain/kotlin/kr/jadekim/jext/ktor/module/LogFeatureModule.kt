package kr.jadekim.jext.ktor.module

import io.ktor.application.*
import kr.jadekim.logger.context.MutableLogContext
import kr.jadekim.logger.integration.ktor.JLogContext
import kr.jadekim.logger.integration.ktor.RequestLogger

object LogFeatureModule : KtorModuleFactory<LogFeatureModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var ignoreRequestBodyLog: ApplicationCall.() -> Boolean = { false }
        var requestLogContext: ApplicationCall.(MutableLogContext) -> Unit = {}
    }

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(JLogContext)

        install(RequestLogger) {
            canLogBody = config.ignoreRequestBodyLog
            logContext = config.requestLogContext
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}