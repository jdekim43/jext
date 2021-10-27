package kr.jadekim.jext.ktor.module

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kr.jadekim.common.exception.ExceptionLevel
import kr.jadekim.common.exception.FriendlyException
import kr.jadekim.server.http.exception.HttpServerException
import kr.jadekim.jext.ktor.extension.locale
import kr.jadekim.jext.ktor.extension.toResponse
import java.util.Locale

object ErrorHandlerFeatureModule : KtorModuleFactory<ErrorHandlerFeatureModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var supportLocale = listOf(Locale.getDefault())
        var responseError: suspend PipelineContext<*, ApplicationCall>.(HttpServerException) -> Unit = {
            context.respond(HttpStatusCode.fromValue(it.httpStatus), it.toResponse(locale(supportLocale)))
        }
        var configure: StatusPages.Configuration.() -> Unit = {}
        var getExceptionMeta: (FriendlyException) -> Map<String, Any?> = { emptyMap() }
    }

    private val ExceptionLevel.httpCode
        get() = when (this) {
            ExceptionLevel.ERROR, ExceptionLevel.FETAL -> HttpStatusCode.InternalServerError.value
            else -> HttpStatusCode.BadRequest.value
        }

    override fun create(config: Configuration): KtorModule = ktorModule {
        with(config) {
            install(StatusPages) {
                val serverErrorStatusCodes = HttpStatusCode.allStatusCodes.filter { it.value / 100 == 5 }
                status(*serverErrorStatusCodes.toTypedArray()) {
                    responseError(HttpServerException("UKN-1", it.value, message = it.description, level = ExceptionLevel.ERROR))
                }

                val requestErrorStatusCodes = HttpStatusCode.allStatusCodes.filter { it.value / 100 == 4 }
                status(*requestErrorStatusCodes.toTypedArray()) {
                    responseError(HttpServerException("UKN-3", it.value, message = it.description, level = ExceptionLevel.WARNING))
                }

                exception<Throwable> {
                    val wrapper = HttpServerException(
                        "UKN-2",
                        HttpStatusCode.InternalServerError.value,
                        cause = it,
                        message = it.message,
                        level = ExceptionLevel.ERROR
                    )

                    responseError(wrapper)
                }

                exception<FriendlyException> {
                    responseError(
                        HttpServerException(
                            it.code,
                            it.level.httpCode,
                            cause = it,
                            message = it.message,
                            level = it.level,
                            data = it.data
                        )
                    )
                }

                exception<HttpServerException> {
                    responseError(it)
                }

                configure()
            }
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}