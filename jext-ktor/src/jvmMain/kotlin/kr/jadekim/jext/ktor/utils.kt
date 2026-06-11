package kr.jadekim.jext.ktor

import kr.jadekim.common.util.load
import kr.jadekim.common.util.loadFromFileTree
import java.io.File
import java.util.*

private val CLASSPATH_PREFIX = "classpath:"

internal fun loadPropertiesFromArguments(arguments: Map<String, List<String>>): Map<String, String> {
    val properties = Properties()

    arguments.filterKeys { it == "config" || it == "c" }
        .flatMap { it.value }
        .map {
            if (it.startsWith(CLASSPATH_PREFIX)) {
                it.substring(CLASSPATH_PREFIX.length)
                    .let { path -> BaseKtorServer::class.java.getResourceAsStream(path) }
            } else {
                File(it).inputStream()
            }
        }
        .let(properties::load)

    arguments.filterKeys { it == "config-tree" || it == "t" }
        .flatMap { it.value }
        .map { File(it) }
        .forEach(properties::loadFromFileTree)

    return properties.asIterable().associate { it.key.toString() to it.value.toString() }
}
