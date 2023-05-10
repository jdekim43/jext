package kr.jadekim.jext.ktor.sentry

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.sentry.Sentry
import io.sentry.kotlin.SentryContext
import io.sentry.protocol.Request
import kotlinx.coroutines.withContext
import kr.jadekim.jext.ktor.extension.canReadBody
import java.nio.charset.Charset

class SentryFeature {

    class Configuration

    companion object Feature : BaseApplicationPlugin<Application, Configuration, SentryFeature> {

        override val key: AttributeKey<SentryFeature> = AttributeKey("SentryIntegration")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): SentryFeature {
            val feature = SentryFeature()

            pipeline.intercept(ApplicationCallPipeline.Call) {
                var body: Any? = null

                if (context.request.httpMethod.canReadBody) {
                    body = when (context.request.contentType()) {
                        ContentType.Application.Json, ContentType.Text.Plain -> {
                            val charset = try {
                                Charset.forName(context.request.acceptEncoding())
                            } catch (e: Exception) {
                                Charset.defaultCharset()
                            }
                            String(context.receive(), charset)
                        }
                        else -> mapOf("body_bytes" to context.receive<ByteArray>().encodeBase64())
                    }
                }

                withContext(SentryContext()) {
                    Sentry.configureScope { scope ->
                        val request = Request()
                        request.url = context.request.path()
                        request.cookies = context.request.cookies.rawCookies.map { "${it.key}=${it.value}" }.joinToString()
                        request.method = context.request.httpMethod.value
                        request.queryString = context.request.queryString()
                        request.headers = context.request.headers.toMap().mapValues { it.value.joinToString(" | ") }
                        request.bodySize = context.request.contentLength()
                        request.data = body

                        scope.request = request

                        scope.setTag("method", context.request.httpMethod.value)
                    }

                    proceed()
                }
            }

            return feature
        }
    }
}