package kr.jadekim.jext.apm.datadog.ktor

val DATADOG_TRACE_HEADERS = setOf(
    "x-datadog-trace-id",
    "x-datadog-parent-id",
    "x-datadog-origin",
    "x-datadog-sampling-priority",
    "traceparent",
    "b3",
    "X-B3-TraceId",
    "X-B3-SpanId",
    "X-B3-Sampled",
)