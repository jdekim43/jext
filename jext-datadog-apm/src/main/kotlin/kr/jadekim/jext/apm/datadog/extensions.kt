package kr.jadekim.jext.apm.datadog

import io.opentracing.Span
import io.opentracing.SpanContext
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.propagation.TextMap
import io.opentracing.propagation.TextMapAdapter
import io.opentracing.propagation.TextMapExtract
import kotlinx.coroutines.Job
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.coroutines.coroutineContext

fun Map<String, String>.toTextMap(): TextMapExtract = TextMapAdapter(this)

fun MutableMap<String, String>.toTextMap(): TextMap = TextMapAdapter(this)

val Span.context: SpanContext
    get() = context()

suspend fun Span.addOnJobCompletion(
    body: Span.(error: Throwable?) -> Unit = { this.finish() },
) {
    coroutineContext[Job]?.invokeOnCompletion {
        it?.also {
            val errors = StringWriter()
            it.printStackTrace(PrintWriter(errors))
            setTag("error", true)
            log(mapOf("stack" to errors))
        }
        body(it)
    }
}

inline fun Tracer.span(
    operationName: String,
    builderBody: Tracer.SpanBuilder.() -> Tracer.SpanBuilder,
): Span = buildSpan(operationName).builderBody()
    .start()

suspend inline fun span(
    operationName: String,
    builderBody: Tracer.SpanBuilder.() -> Tracer.SpanBuilder,
): Span = coroutineContext.tracer.span(operationName, builderBody)

fun <T> Tracer.SpanBuilder.extractSpan(carrier: T, format: Format<T>, tracer: Tracer): Tracer.SpanBuilder {
    val context: SpanContext? = tracer.extract(format, carrier)

    return context?.let { asChildOf(it) } ?: this
}
