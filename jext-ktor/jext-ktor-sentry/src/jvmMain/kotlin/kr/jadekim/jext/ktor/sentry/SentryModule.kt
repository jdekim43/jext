package kr.jadekim.jext.ktor.sentry

import io.ktor.server.application.*
import kr.jadekim.jext.ktor.module.KtorModule
import kr.jadekim.jext.ktor.module.KtorModuleConfiguration
import kr.jadekim.jext.ktor.module.KtorModuleFactory
import kr.jadekim.jext.ktor.module.ktorModule

object SentryModule : KtorModuleFactory<SentryModule.Configuration> {

    class Configuration : KtorModuleConfiguration

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(SentryFeature)
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}