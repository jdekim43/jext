package kr.jadekim.jext.ktor

import io.ktor.config.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kr.jadekim.server.http.BaseHttpServer
import kr.jadekim.jext.ktor.module.KtorDefaultModules
import kr.jadekim.jext.ktor.module.KtorModule
import kr.jadekim.jext.ktor.module.ktorModule
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
abstract class BaseKtorServer(
    serverName: String? = null,
    serviceHost: String = "0.0.0.0",
    servicePort: Int = 80,
    val isDevelopmentMode: Boolean = false,
    val rootPath: String = "",
    private val parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : BaseHttpServer(serverName, serviceHost, servicePort) {

    var blockingStart = true
    var stopGracePeriod = Duration.seconds(15)

    private val configuration: ApplicationConfig = MapApplicationConfig()

    private val nettyConfiguration: NettyApplicationEngine.Configuration.() -> Unit = { configureNetty() }

    private var ktorServer: ApplicationEngine? = null

    abstract fun Routing.configureRouting()

    override fun onStart() {
        ktorServer = createKtorServer()
        ktorServer?.start(wait = blockingStart)
    }

    override fun onStop(timeout: Duration) {
        ktorServer?.stop(stopGracePeriod.inWholeMilliseconds, timeout.inWholeMilliseconds)
    }

    protected open fun ApplicationEngineEnvironmentBuilder.configureApplicationEngineEnvironment() {
        //do nothing
    }

    protected open fun NettyApplicationEngine.Configuration.configureNetty() {
        //do nothing
    }

    protected open fun createModules(defaultModules: KtorDefaultModules = KtorDefaultModules()): List<KtorModule> {
        return defaultModules.get()
    }

    protected open fun createKtorServer(): ApplicationEngine {
        return embeddedServer(Netty, createApplicationEngineEnvironment(), nettyConfiguration)
    }

    protected fun createApplicationEngineEnvironment(): ApplicationEngineEnvironment = applicationEngineEnvironment {
        parentCoroutineContext = this@BaseKtorServer.parentCoroutineContext
        log = LoggerFactory.getLogger("ktor.application")
        config = configuration

        connector {
            host = serviceHost
            port = servicePort
        }

        modules.addAll(createModules())
        modules.add(createRoutingModule())

        rootPath = this@BaseKtorServer.rootPath
        developmentMode = isDevelopmentMode

        configureApplicationEngineEnvironment()
    }

    private fun createRoutingModule() = ktorModule {
        routing {
            configureRouting()
        }
    }
}