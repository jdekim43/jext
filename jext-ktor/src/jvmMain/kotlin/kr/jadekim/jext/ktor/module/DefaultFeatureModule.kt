package kr.jadekim.jext.ktor.module

import io.ktor.application.*
import io.ktor.features.*

object DefaultFeatureModule : KtorModuleFactory<DefaultFeatureModule.Configuration> {

    class Configuration : KtorModuleConfiguration

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(XForwardedHeaderSupport)

        install(DoubleReceive) {
            receiveEntireContent = true
        }

        install(AutoHeadResponse)
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}