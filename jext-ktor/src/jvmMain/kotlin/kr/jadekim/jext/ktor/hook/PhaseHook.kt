package kr.jadekim.jext.ktor.hook

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

class PhaseHook(val phase: PipelinePhase) : Hook<HookHandler> {

    override fun install(pipeline: ApplicationCallPipeline, handler: HookHandler) {
        pipeline.intercept(phase) {
            with(HookContext(call, ::proceed)) {
                handler()
            }
        }
    }
}
