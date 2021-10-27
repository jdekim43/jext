package kr.jadekim.jext.ktor.module

import com.google.gson.Gson

class KtorDefaultModules {

    private val modules = mutableMapOf<KtorModuleFactory<KtorModuleConfiguration>, KtorModuleConfiguration>()

    init {
        defaultFeature()
        logFeature()
        gson()
        errorHandlerFeature()
    }

    @Suppress("UNCHECKED_CAST")
    fun <Config : KtorModuleConfiguration> module(
        factory: KtorModuleFactory<Config>,
        configure: Config.() -> Unit = {}
    ): KtorDefaultModules {
        modules[factory as KtorModuleFactory<KtorModuleConfiguration>] =
            factory.createDefaultConfiguration().apply(configure)

        return this
    }

    fun get() = modules.map { it.key.create(it.value) }

    fun defaultFeature() = module(DefaultFeatureModule)

    fun logFeature(configure: LogFeatureModule.Configuration.() -> Unit = {}) = module(LogFeatureModule, configure)

    fun contentNegotiationFeature(
        configure: ContentNegotiationFeatureModule.Configuration.() -> Unit = {}
    ) = module(ContentNegotiationFeatureModule, configure)

    fun gson(gson: Gson = Gson()) = contentNegotiationFeature { gson(gson) }

    fun errorHandlerFeature(
        configure: ErrorHandlerFeatureModule.Configuration.() -> Unit = {}
    ) = module(ErrorHandlerFeatureModule, configure)
}