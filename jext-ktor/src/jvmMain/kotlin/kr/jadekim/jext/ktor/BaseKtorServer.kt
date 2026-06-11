package kr.jadekim.jext.ktor

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kr.jadekim.common.enumeration.Environment
import kr.jadekim.common.enumeration.IEnvironment
import kr.jadekim.common.util.parseArgument
import kr.jadekim.jext.koin.APP_PROPERTIES
import kr.jadekim.jext.koin.extension.Properties
import kr.jadekim.jext.ktor.module.DefaultFeatureModule
import kr.jadekim.jext.ktor.module.ErrorHandlerModule
import kr.jadekim.jext.ktor.module.KtorModuleConfigurator
import kr.jadekim.jext.ktor.module.MonitoringModule
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.context.GlobalLogContext
import kr.jadekim.logger.integration.koin.KoinLogger
import kr.jadekim.server.http.BaseHttpServer
import org.koin.core.module.Module
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

abstract class BaseKtorServer<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    private val factory: KtorServerFactory<TEngine, TConfiguration>,
    serverName: String? = null,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext,
    logger: JLogger? = null,
) : BaseHttpServer<KtorServerFactory.Options>(serverName, logger) {

    var properties = Properties()
        protected set

    private var ktorServer: EmbeddedServer<TEngine, TConfiguration>? = null

    private lateinit var options: KtorServerFactory.Options

    protected abstract val koinModules: List<Module>

    protected abstract fun Route.setupRouting()

    protected abstract fun KtorModuleConfigurator.setupKtorModules(options: KtorServerFactory.Options)

    protected open fun Route.setupInternalRouting() {
        //do nothing
    }

    fun init(vararg args: String, environment: IEnvironment? = null, version: String? = null) =
        init(parseArgument(*args), environment, version)

    fun init(arguments: Map<String, List<String>>, environment: IEnvironment? = null, version: String? = null) {
        logger.info {
            meta = arguments

            "Initialize the $serverName with ${arguments.size} arguments"
        }

        val propertiesMap = mutableMapOf<String, String>()
        if (environment != null) {
            propertiesMap["environment"] = environment.name
        }
        if (version != null) {
            propertiesMap["version"] = version
        }

        propertiesMap.putAll(System.getenv())
        propertiesMap.putAll(loadPropertiesFromArguments(arguments))
        propertiesMap.putAll(System.getProperties().mapKeys { it.toString() }.mapValues { it.toString() })

        properties = Properties(propertiesMap)

        GlobalLogContext["applicationName"] = serverName
        GlobalLogContext["environment"] = environment?.name ?: properties["environment"]?.firstOrNull()
        GlobalLogContext["version"] = version ?: properties["version"]?.firstOrNull()

        onInit()
    }

    protected open fun onInit() {
        //do nothing
    }

    override fun onStart(options: KtorServerFactory.Options) {
        this.options = options

        configureKtorModules(options)
        factory.configureServer { configureServer() }
        factory.routing { setupRouting() }
        factory.internalRouting { setupInternalRouting() }

        ktorServer = factory.create(options, coroutineContext).apply {
            start(wait = true)
        }
    }

    override fun onStop(timeout: Duration) {
        ktorServer?.stop(options.stopGracePeriod.inWholeMilliseconds, timeout.inWholeMilliseconds)
    }

    protected open fun ServerConfigBuilder.configureServer() {
        //do nothing
    }

    private fun configureKtorModules(options: KtorServerFactory.Options) {
        factory.module {
            install(Koin) {
                logger(KoinLogger())
                properties(properties)
                modules(module {
                    single {
                        Environment.from(properties["environment"]) ?: object : IEnvironment {
                            override val name: String = properties["environment"] ?: "unknown"
                        }
                    } withOptions {
                        binds(listOf(IEnvironment::class, Environment::class))
                    }
                    single(APP_PROPERTIES) { properties }
                    single { properties }
                })
                modules(koinModules)


//                createEagerInstances()
            }
            install(DefaultFeatureModule())
            install(MonitoringModule {
                if (options.internalPort != null) {
                    enableRestriction = true
                    restriction {
                        allowPorts = listOf(options.internalPort)
                    }
                }
            })
            install(ErrorHandlerModule())

            setupKtorModules(options)
        }
    }
}
