package kr.jadekim.jext.apm.datadog

import io.opentracing.Scope
import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer
import kotlinx.coroutines.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class DatadogApmContext(
    val tracer: Tracer = GlobalTracer.get(),
    val span: Span? = tracer.activeSpan()
) : ThreadContextElement<Scope>,
    AbstractCoroutineContextElement(DatadogApmContext) {

    companion object Key : CoroutineContext.Key<DatadogApmContext>

    override fun restoreThreadContext(context: CoroutineContext, oldState: Scope) {
        runCatching {
            if (context.isActive) {
                span?.log(mapOf("event" to "suspend", "coroutine" to (context[CoroutineName]?.name ?: "unknown")))
            }
        }
        oldState.close()
    }

    override fun updateThreadContext(context: CoroutineContext): Scope {
        if (context.isActive) {
            span?.log(mapOf("event" to "resumed", "coroutine" to (context[CoroutineName]?.name ?: "unknown")))
        }

        return tracer.activateSpan(span)
    }
}

val CoroutineContext.tracer: Tracer
    get() {
        return datadogApmContext.tracer
    }

val CoroutineContext.datadogApmContext: DatadogApmContext
    get() {
        return get(DatadogApmContext)
            ?: throw IllegalStateException("DatadogApmContext is required for proper propagation of Traces through coroutines")
    }

suspend inline fun apmContext() = coroutineContext[DatadogApmContext] ?: DatadogApmContext()

suspend fun <T> withApm(
    tracer: Tracer = GlobalTracer.get(),
    getSpan: Tracer.() -> Span? = { null },
    block: suspend CoroutineScope.() -> T,
): T {
    val span = tracer.getSpan()

    return withContext(DatadogApmContext(tracer, span)) {
        span?.addOnJobCompletion()
        block()
    }
}

suspend fun <T> withApm(span: Span, block: suspend CoroutineScope.(Span) -> T): T {
    val context = if (coroutineContext[DatadogApmContext]?.span == span) {
        coroutineContext
    } else {
        coroutineContext + DatadogApmContext(coroutineContext.tracer, span)
    }

    return withContext(coroutineContext) {
        block(span)
    }
}

suspend inline fun <T> withApmSpan(
    operationName: String,
    noinline spanBuilder: Tracer.SpanBuilder.() -> Tracer.SpanBuilder = { this },
    noinline cleanup: Span.(error: Throwable?) -> Unit = { this.finish() },
    crossinline block: suspend CoroutineScope.(Span) -> T,
): T {
    val span = span(operationName, spanBuilder)

    return withApm(span) {
        span.addOnJobCompletion(cleanup)
        block(span)
    }
}
