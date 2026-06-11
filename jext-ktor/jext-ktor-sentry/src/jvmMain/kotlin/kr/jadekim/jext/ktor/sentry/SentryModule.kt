package kr.jadekim.jext.ktor.sentry

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.sentry.Sentry
import io.sentry.kotlin.SentryContext
import io.sentry.protocol.Request
import kotlinx.coroutines.withContext
import kr.jadekim.jext.ktor.extension.canReadBody
import kr.jadekim.jext.ktor.hook.PhaseHook
import kr.jadekim.jext.ktor.module.KtorApplicationModuleInstaller
import kr.jadekim.jext.ktor.module.KtorModuleConfiguration
import kr.jadekim.jext.ktor.module.KtorModuleDefinition
import java.nio.charset.Charset

object SentryModule : KtorModuleDefinition<SentryModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var resolvePath: (ApplicationCall) -> String = {
            when (it) {
                is RoutingCall -> it.route.toString()
                is RoutingPipelineCall -> it.route.toString()
                else -> it.request.path()
            }
        }
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller = KtorApplicationModuleInstaller {
        install(Plugin)
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()

    val Plugin = createRouteScopedPlugin("SentryPlugin", { createDefaultConfiguration() }) {
        on(PhaseHook(ApplicationCallPipeline.Monitoring)) {
            withContext(SentryContext()) {
                var body: Any? = null

                if (call.request.httpMethod.canReadBody) {
                    body = when (call.request.contentType()) {
                        ContentType.Application.Json, ContentType.Text.Plain -> {
                            val charset = try {
                                Charset.forName(call.request.acceptEncoding())
                            } catch (e: Exception) {
                                Charset.defaultCharset()
                            }
                            String(call.receive(), charset)
                        }

                        else -> mapOf("body_bytes" to call.receive<ByteArray>().encodeBase64())
                    }
                }

                Sentry.configureScope { scope ->
                    val request = Request()
                    request.url = pluginConfig.resolvePath(call)
                    request.cookies = call.request.cookies.rawCookies.map { "${it.key}=${it.value}" }.joinToString()
                    request.method = call.request.httpMethod.value
                    request.queryString = call.request.queryString()
                    request.headers = call.request.headers.toMap().mapValues { it.value.joinToString(" | ") }
                    request.bodySize = call.request.contentLength()
                    request.data = body
                    request.others = mapOf("requestPath" to call.request.path())

                    scope.request = request

                    scope.setTag("method", call.request.httpMethod.value)
                }

                proceed()
            }
        }
    }
}