package kr.jadekim.jext.ktor.module

import io.ktor.application.*

typealias KtorModule = Application.() -> Unit

fun ktorModule(block: KtorModule): KtorModule = block

interface KtorModuleFactory<Config : KtorModuleConfiguration> {

    fun create(): KtorModule = create(createDefaultConfiguration())

    fun create(config: Config): KtorModule

    fun createDefaultConfiguration(): Config
}

interface KtorModuleConfiguration