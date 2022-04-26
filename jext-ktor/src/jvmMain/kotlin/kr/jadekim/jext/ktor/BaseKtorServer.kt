package kr.jadekim.jext.ktor

import com.google.gson.Gson
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kr.jadekim.jext.ktor.module.*
import kr.jadekim.server.http.BaseHttpServer
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.*

abstract class BaseKtorServer(
    serverName: String? = null,
    serviceHost: String = "0.0.0.0",
    servicePort: Int = 80,
    val isDevelopmentMode: Boolean = false,
    val rootPath: String = "",
    private val parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : BaseHttpServer(serverName, serviceHost, servicePort) {

    var blockingStart = true
    var stopGracePeriod = 15.toDuration(DurationUnit.SECONDS)

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

    protected open fun createModules(): List<KtorModule> = listOf(
        DefaultFeatureModule.create(),
        LogPluginsModule.create(),
        ContentNegotiationModule.create { gson(Gson()) },
        ErrorHandlerModule.create(),
    )

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