package kr.jadekim.jext.apm.datadog.ktor.adapter

import io.ktor.http.*
import io.ktor.server.response.*
import io.opentracing.propagation.TextMap

internal class ResponseHeadersTextMapAdapter(private val responseHeaders: ResponseHeaders) :
    HeadersTextMapAdapter(responseHeaders.allValues()) {

    override val headers: Headers
        get() = responseHeaders.allValues()

    override fun put(key: String, value: String) {
        responseHeaders.append(key, value)
    }
}

internal fun ResponseHeaders.asTextMap(): TextMap {
    return ResponseHeadersTextMapAdapter(this)
}
