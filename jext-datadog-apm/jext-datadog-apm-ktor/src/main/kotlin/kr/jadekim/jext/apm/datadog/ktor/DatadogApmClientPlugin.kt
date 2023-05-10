package kr.jadekim.jext.apm.datadog.ktor

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.util.GlobalTracer
import kr.jadekim.jext.apm.datadog.context
import kr.jadekim.jext.apm.datadog.datadogApmContext
import kr.jadekim.jext.apm.datadog.extractSpan
import kr.jadekim.jext.apm.datadog.ktor.adapter.asTextMap
import kr.jadekim.jext.apm.datadog.withApmSpan

class DatadogApmClientPlugin(config: Configuration) {

    private val tracer = config.tracer

    class Configuration {
        var tracer: Tracer = GlobalTracer.get()
    }

    fun getTraceName(method: HttpMethod, url: String) = "$method - $url"

    companion object Feature : HttpClientPlugin<Configuration, DatadogApmClientPlugin> {

        override val key: AttributeKey<DatadogApmClientPlugin> = AttributeKey("DatadogApmClientPlugin")

        private val spanKey = AttributeKey<Span>("DatadogApmClientPlugin-SendingSpan")

        override fun install(plugin: DatadogApmClientPlugin, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                withApmSpan(plugin.getTraceName(context.method, context.url.toString())) {
                    proceed()
                }
            }

            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                (coroutineContext.datadogApmContext.span)?.let {
                    plugin.tracer.inject(it.context, Format.Builtin.HTTP_HEADERS, context.headers.asTextMap())
                    context.attributes.put(spanKey, it)
                }
                proceed()
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.Before) {
                withApmSpan(plugin.getTraceName(it.request.method, it.request.url.toString()), spanBuilder = {
                    extractSpan(
                        it.headers.asTextMap(),
                        Format.Builtin.HTTP_HEADERS,
                        plugin.tracer,
                    )
                    asChildOf(it.request.attributes.getOrNull(spanKey))
                }) {
                    proceed()
                }
            }

        }

        override fun prepare(block: Configuration.() -> Unit): DatadogApmClientPlugin {
            val config = Configuration().apply(block)
            return DatadogApmClientPlugin(config)
        }
    }
}