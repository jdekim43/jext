package kr.jadekim.jext.apm.datadog.ktor.adapter

import io.ktor.http.*
import io.opentracing.propagation.TextMap

open class HeadersTextMapAdapter(open val headers: Headers) : TextMap {

    override fun put(key: String, value: String) {
        error("This Adapter doesn't support writing new headers")
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, String>> {
        return headers.entries()
            .associateBy { it.key }
            .filterValues { it.value.isNotEmpty() }
            .mapValues { (_, value) -> value.value.first() }
            .toMutableMap().iterator()//This is only due to opentracing api using mutable map Iterator

    }
}

fun Headers.asTextMap(): TextMap = HeadersTextMapAdapter(this)
