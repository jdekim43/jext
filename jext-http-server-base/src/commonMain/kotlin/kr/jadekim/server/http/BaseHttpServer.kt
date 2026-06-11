package kr.jadekim.server.http

import kr.jadekim.logger.JLog
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.context.GlobalLogContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class BaseHttpServer<O>(
    serverName: String? = null,
    logger: JLogger? = null,
) {

    val serverName = serverName ?: this::class.simpleName ?: "http-server"

    protected val logger = logger ?: JLog.get(this.serverName)

    abstract fun createDefaultOptions(): O

    protected abstract fun onStart(options: O)

    protected abstract fun onStop(timeout: Duration)

    protected open fun setup(options: O): O = options

    fun start(options: O = createDefaultOptions()) {
        val options = setup(options)

        GlobalLogContext["serverName"] = serverName

        val meta = mapOf("options" to options)

        logger.info("Start $serverName", meta = meta)

        onStart(options)
    }

    fun stop(timeout: Duration = 30.seconds) {
        logger.info("Request stop $serverName")

        onStop(timeout)

        logger.info("Stopped $serverName")
    }
}