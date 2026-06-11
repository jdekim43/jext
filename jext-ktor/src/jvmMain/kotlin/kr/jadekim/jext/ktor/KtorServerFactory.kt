package kr.jadekim.jext.ktor

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kr.jadekim.jext.ktor.module.KtorApplicationModule
import kr.jadekim.jext.ktor.module.KtorApplicationModuleInstaller
import kr.jadekim.jext.ktor.module.KtorModuleConfigurator
import kr.jadekim.jext.ktor.module.KtorModuleFactory
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koinModule
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

open class KtorServerFactory<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    private val engine: ApplicationEngineFactory<TEngine, TConfiguration>,
    private val applicationConfig: ApplicationConfig = MapApplicationConfig(),
) {

    data class Options(
        val serviceHost: String = "0.0.0.0",
        val servicePort: Int = 80,
        val internalHost: String = "127.0.0.1",
        val internalPort: Int? = null,
        val isDevelopmentMode: Boolean = false,
        val rootPath: String = "",
        val stopGracePeriod: Duration = if (isDevelopmentMode) {
            1.toDuration(DurationUnit.SECONDS)
        } else {
            15.toDuration(DurationUnit.SECONDS)
        }
    )

    private val moduleConfigurator = KtorModuleConfigurator()

    private var serverConfigs: MutableList<ServerConfigBuilder.() -> Unit> = mutableListOf()
    private var routingConfigs: MutableList<Route.() -> Unit> = mutableListOf()
    private var internalRoutingConfigs: MutableList<Route.() -> Unit> = mutableListOf()

    fun create(
        options: Options = Options(),
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): EmbeddedServer<TEngine, TConfiguration> {
        val applicationEnvironment = createApplicationEnvironment()
        val serverConfig = createServerConfig(options, applicationEnvironment, coroutineContext)
        return embeddedServer(engine, serverConfig) { configureEngine(options) }
    }

    fun configureServer(body: ServerConfigBuilder.() -> Unit) {
        serverConfigs.add(body)
    }

    fun routing(body: Route.() -> Unit) {
        routingConfigs.add(body)
    }

    fun internalRouting(body: Route.() -> Unit) {
        internalRoutingConfigs.add(body)
    }

    fun module(body: KtorModuleConfigurator.() -> Unit) = moduleConfigurator.apply(body)

    protected open fun createApplicationEnvironment(): ApplicationEnvironment = applicationEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        config = applicationConfig
    }

    protected open fun TConfiguration.configureEngine(options: Options) {
        connector {
            host = options.serviceHost
            port = options.servicePort
        }

        if (options.internalPort != null) {
            connector {
                host = options.internalHost
                port = options.internalPort
            }
        }
    }

    private fun createServerConfig(
        options: Options,
        environment: ApplicationEnvironment,
        coroutineContext: CoroutineContext,
    ): ServerConfig = serverConfig(environment) {
        parentCoroutineContext = coroutineContext

        rootPath = options.rootPath
        developmentMode = options.isDevelopmentMode

        val ktorModules = moduleConfigurator.build()
        val koinModule = KtorApplicationModule(Koin)
        val (preKoinInstall, postKoinInstall) = ktorModules.partition { it.definition == koinModule }

        preKoinInstall.mapNotNull { it.createApplicationModule() }
            .forEach { module(body = it) }

        module {
            ktorModules.mapNotNull { it.createKoinModule() }
                .forEach { koinModule(it) }
        }

        postKoinInstall.mapNotNull { it.createApplicationModule() }
            .forEach { module(body = it) }

        module(body = createRoutingModule(ktorModules, options))

        serverConfigs.forEach { it() }
    }

    private fun createRoutingModule(ktorModules: List<KtorModuleFactory<*>>, options: Options) =
        KtorApplicationModuleInstaller {
            routing {
                ktorModules.mapNotNull { it.createRouteScopedModule() }
                    .forEach { installModule -> installModule() }

                routingConfigs.forEach { it() }

                if (options.internalPort != null) {
                    port(options.internalPort) {
                        internalRoutingConfigs.forEach { it() }
                    }
                }
            }
        }
}