package kr.jadekim.jext.ktor.hook

import io.ktor.server.application.*

data class HookContext(
    val call: PipelineCall,
    val proceed: suspend () -> Any,
)

typealias HookHandler = suspend HookContext.() -> Unit
