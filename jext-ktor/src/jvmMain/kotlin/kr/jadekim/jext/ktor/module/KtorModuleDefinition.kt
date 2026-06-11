package kr.jadekim.jext.ktor.module

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.dsl.ModuleDeclaration

interface KtorModuleConfiguration

typealias KoinModuleInstaller = ModuleDeclaration

fun KoinModuleInstaller(block: KoinModuleInstaller): KoinModuleInstaller = block

typealias KtorApplicationModuleInstaller = suspend Application.() -> Unit

fun KtorApplicationModuleInstaller(block: KtorApplicationModuleInstaller): KtorApplicationModuleInstaller = block

typealias KtorRouteScopedModuleInstaller = Route.() -> Unit

fun KtorRouteScopedModuleInstaller(block: KtorRouteScopedModuleInstaller): KtorRouteScopedModuleInstaller = block

class KtorModuleFactory<Config : KtorModuleConfiguration> internal constructor(
    val definition: KtorModuleDefinition<Config>,
    val config: Config,
) {

    fun createKoinModule(): KoinModuleInstaller? = definition.createKoinModule(config)

    fun createApplicationModule(): KtorApplicationModuleInstaller? = definition.createApplicationModule(config)

    fun createRouteScopedModule(): KtorRouteScopedModuleInstaller? = definition.createRouteScopedModule(config)
}

interface KtorModuleDefinition<Config : KtorModuleConfiguration> {

    fun createDefaultConfiguration(): Config

    fun createKoinModule(config: Config): KoinModuleInstaller? = null

    fun createApplicationModule(config: Config): KtorApplicationModuleInstaller? = null

    fun createRouteScopedModule(config: Config): KtorRouteScopedModuleInstaller? = null

    operator fun invoke() = invoke(createDefaultConfiguration())

    operator fun invoke(config: Config) = KtorModuleFactory(this, config)

    operator fun invoke(configure: Config.() -> Unit) = invoke(createDefaultConfiguration().apply(configure))
}

class KtorApplicationModule<B : Any, F : Any>(
    val plugin: Plugin<Application, B, F>
) : KtorModuleDefinition<KtorApplicationModule.KtorNativeModuleConfig<B>> {

    class KtorNativeModuleConfig<B> : KtorModuleConfiguration {
        var configure: B.() -> Unit = {}
    }

    override fun createDefaultConfiguration(): KtorNativeModuleConfig<B> = KtorNativeModuleConfig()

    override fun createApplicationModule(config: KtorNativeModuleConfig<B>): KtorApplicationModuleInstaller =
        KtorApplicationModuleInstaller {
            install(plugin, config.configure)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (plugin == other) return true
        if (other !is KtorApplicationModule<*, *>) return false

        return plugin == other.plugin
    }

    override fun hashCode(): Int = plugin.hashCode()
}

class KtorModuleConfigurator {
    private val modules = mutableListOf<KtorModuleFactory<*>>()

    fun install(module: KtorModuleFactory<*>, insertAt: ((List<KtorModuleFactory<*>>) -> Int)? = null) {
        removeModule(module)
        if (insertAt == null) {
            modules.add(module)
        } else {
            modules.add(insertAt(modules.toList()), module)
        }
    }

    fun <Config : KtorModuleConfiguration> install(
        moduleDefinition: KtorModuleDefinition<Config>,
        insertAt: ((List<KtorModuleFactory<*>>) -> Int)? = null,
        configure: Config.() -> Unit = {},
    ) {
        install(moduleDefinition(configure), insertAt)
    }

    fun <B : Any, F : Any> install(
        plugin: Plugin<Application, B, F>,
        insertAt: ((List<KtorModuleFactory<*>>) -> Int)? = null,
        configure: B.() -> Unit = {},
    ) {
        val module = KtorApplicationModule(plugin)
        val factory = module {
            this.configure = configure
        }
        install(factory, insertAt)
    }

    fun removeModule(module: KtorModuleFactory<*>) = modules.removeIf { it.definition == module.definition }

    fun build() = modules.toList()
}
