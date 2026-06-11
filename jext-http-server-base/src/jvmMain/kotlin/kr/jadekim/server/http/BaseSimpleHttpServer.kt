package kr.jadekim.server.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetSocketAddress
import kotlin.time.Duration

abstract class BaseSimpleHttpServer(serverName: String? = null) : BaseHttpServer<BaseSimpleHttpServer.Options>(serverName) {

    data class Options(
        val host: String = "0.0.0.0",
        val port: Int = 80,
    )

    private var server: HttpServer? = null

    private val routes = mutableListOf<Pair<String, HttpExchange.() -> Unit>>()

    override fun createDefaultOptions(): Options = Options()

    override fun onStart(options: Options) {
        this.server = HttpServer.create(InetSocketAddress(options.host, options.port), 0).apply {
            routes.forEach { (path, block) -> createContext(path, block) }
            start()
        }
    }

    override fun onStop(timeout: Duration) {
        server?.stop(timeout.inWholeSeconds.toInt())
    }

    protected fun route(path: String, block: HttpExchange.() -> Unit) {
        routes.add(path to block)
    }

    protected fun HttpExchange.response(body: String, httpStatus: Int = 200, contentType: String = "text/plain") {
        responseHeaders.add("Content-Type", contentType)

        sendResponseHeaders(httpStatus, body.length.toLong())

        PrintWriter(responseBody).use {
            it.print(body)
        }
    }
}
