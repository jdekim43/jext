package kr.jadekim.jext.koin.util

import kr.jadekim.jext.koin.BaseKoinApplication
import kr.jadekim.common.util.loadProperties
import java.io.File
import java.io.InputStream
import java.util.*

private val CLASSPATH_PREFIX = "classpath:"

actual fun shutdownHook(block: () -> Unit) = kr.jadekim.common.util.shutdownHook(block)

actual fun loadPropertiesFromArguments(arguments: Map<String, List<String>>): Map<String, String> {
    val externalPropertyFiles = mutableListOf<InputStream>()

    arguments.filterKeys { it == "config" || it == "c" }
        .flatMap { it.value }
        .map {
            if (it.startsWith(CLASSPATH_PREFIX)) {
                it.substring(CLASSPATH_PREFIX.length)
                    .let { path -> BaseKoinApplication::class.java.getResourceAsStream(path) }
            } else {
                File(it).inputStream()
            }
        }
        .let { externalPropertyFiles.addAll(it) }

    val properties = Properties()

    loadProperties(externalPropertyFiles, properties)

    return properties.asIterable().associate { it.key.toString() to it.value.toString() }
}
