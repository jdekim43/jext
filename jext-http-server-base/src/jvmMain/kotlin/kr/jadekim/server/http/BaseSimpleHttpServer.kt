package kr.jadekim.server.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetSocketAddress
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class BaseSimpleHttpServer(
    serverName: String? = null,
    serviceHost: String = "0.0.0.0",
    servicePort: Int = 80,
) : BaseHttpServer(serverName, serviceHost, servicePort) {

    private val server = HttpServer.create(InetSocketAddress(serviceHost, servicePort), 0)

    override fun onStart() {
        server.start()
    }

    @OptIn(ExperimentalTime::class)
    override fun onStop(timeout: Duration) {
        server.stop(timeout.inWholeSeconds.toInt())
    }

    protected fun route(path: String, block: HttpExchange.() -> Unit) {
        server.createContext(path, block)
    }

    protected fun HttpExchange.response(body: String, httpStatus: Int = 200, contentType: String = "text/plain") {
        responseHeaders.add("Content-Type", contentType)

        sendResponseHeaders(httpStatus, body.length.toLong())

        PrintWriter(responseBody).use {
            it.print(body)
        }
    }
}
