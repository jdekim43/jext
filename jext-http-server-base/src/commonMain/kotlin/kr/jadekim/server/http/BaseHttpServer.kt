package kr.jadekim.server.http

import kr.jadekim.logger.JLog
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.context.GlobalLogContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
abstract class BaseHttpServer(
    serverName: String? = null,
    val serviceHost: String = "0.0.0.0",
    val servicePort: Int = 80,
    logger: JLogger? = null,
) {

    val serverName = serverName ?: this::class.simpleName ?: "HttpServer"

    protected val logger = logger ?: JLog.get(this.serverName)

    init {
        GlobalLogContext["server"] = mapOf(
            (serverName ?: "http-server") to mapOf(
                "serviceHost" to serviceHost,
                "servicePort" to servicePort,
            )
        )
    }

    protected abstract fun onStart()

    protected abstract fun onStop(timeout: Duration)

    fun start() {
        val meta = mapOf("serviceHost" to serviceHost, "servicePort" to servicePort.toString())
        val metaString = meta.toList().joinToString(prefix = "(", postfix = ")") { "${it.first}=${it.second}" }

        logger.info("Start $serverName $metaString", meta = meta)

        onStart()
    }

    fun stop(timeout: Duration = 30.seconds) {
        logger.info("Request stop $serverName")

        onStop(timeout)

        logger.info("Stopped $serverName")
    }
}