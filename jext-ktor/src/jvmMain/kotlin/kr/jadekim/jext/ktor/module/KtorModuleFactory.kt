package kr.jadekim.jext.ktor.module

import io.ktor.server.application.*

typealias KtorModule = Application.() -> Unit

fun ktorModule(block: KtorModule): KtorModule = block

interface KtorModuleFactory<Config : KtorModuleConfiguration> {

    fun create(): KtorModule = create(createDefaultConfiguration())

    fun create(config: Config): KtorModule

    fun create(configure: Config.() -> Unit) = create(createDefaultConfiguration().apply(configure))

    fun createDefaultConfiguration(): Config
}

interface KtorModuleConfiguration