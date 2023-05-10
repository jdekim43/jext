package kr.jadekim.jext.apm.datadog.ktor.adapter

import io.ktor.http.*
import io.opentracing.propagation.TextMap

internal class HeadersBuilderTextMapAdapter(private val headers: HeadersBuilder) : TextMap {

    override fun put(key: String, value: String) {
        headers.append(key, value)
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, String>> {
        return headers.entries()
            .filter { (_, values) -> values.isNotEmpty() }
            .associate { (key, values) -> key to values.first() }
            .toMutableMap() //This is only due to opentracing api using mutable map Iterator
            .iterator()
    }

}

fun HeadersBuilder.asTextMap(): TextMap = HeadersBuilderTextMapAdapter(this)

