package kr.jadekim.jext.ktor.module

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kr.jadekim.common.exception.ExceptionLevel
import kr.jadekim.common.exception.FriendlyException
import kr.jadekim.jext.ktor.extension.locale
import kr.jadekim.jext.ktor.extension.toResponse
import kr.jadekim.server.http.exception.HttpServerException
import java.util.*

object ErrorHandlerModule : KtorModuleFactory<ErrorHandlerModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var supportLocale = listOf(Locale.getDefault())
        var responseError: suspend ApplicationCall.(HttpServerException) -> Unit = {
            respond(HttpStatusCode.fromValue(it.httpStatus), it.toResponse(locale(supportLocale)))
        }
        var configure: StatusPagesConfig.() -> Unit = {}
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
                status(*serverErrorStatusCodes.toTypedArray()) { call, statusCode ->
                    call.responseError(
                        HttpServerException(
                            "UKN-1",
                            statusCode.value,
                            message = statusCode.description,
                            level = ExceptionLevel.ERROR
                        )
                    )
                }

                val requestErrorStatusCodes = HttpStatusCode.allStatusCodes.filter { it.value / 100 == 4 }
                status(*requestErrorStatusCodes.toTypedArray()) { call, statusCode ->
                    call.responseError(
                        HttpServerException(
                            "UKN-3",
                            statusCode.value,
                            message = statusCode.description,
                            level = ExceptionLevel.DEBUG
                        )
                    )
                }

                exception<Throwable> { call, cause ->
                    val wrapper = HttpServerException(
                        "UKN-2",
                        HttpStatusCode.InternalServerError.value,
                        cause = cause,
                        message = cause.message,
                        level = ExceptionLevel.ERROR
                    )

                    call.responseError(wrapper)
                }

                exception<FriendlyException> { call, cause ->
                    call.responseError(
                        HttpServerException(
                            cause.code,
                            cause.level.httpCode,
                            cause = cause,
                            message = cause.message,
                            level = cause.level,
                            data = cause.data
                        )
                    )
                }

                exception<HttpServerException> { call, cause ->
                    call.responseError(cause)
                }

                configure()
            }
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}