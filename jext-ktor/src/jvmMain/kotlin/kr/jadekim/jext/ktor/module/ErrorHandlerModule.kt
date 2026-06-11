package kr.jadekim.jext.ktor.module

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kr.jadekim.common.exception.ExceptionLevel
import kr.jadekim.common.exception.FriendlyException
import kr.jadekim.jext.ktor.extension.locale
import kr.jadekim.logger.JLog
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.LogLevel
import kr.jadekim.logger.coroutine.sLog
import kr.jadekim.server.http.exception.HttpServerException
import kr.jadekim.server.protocol.ErrorResponse
import java.util.*

object ErrorHandlerModule : KtorModuleDefinition<ErrorHandlerModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var logger: JLogger = JLog.get("ErrorLogger")
        var supportLocale = listOf(Locale.getDefault())
        var respondError: suspend ApplicationCall.(Throwable) -> Unit = {
            when (it) {
                is HttpServerException -> {
                    logger.sLog(it.level.toLogLevel(), it.message ?: "Unknown http server error (${it.httpStatus})", it)
                    respond(HttpStatusCode.fromValue(it.httpStatus), it.toResponse(locale(supportLocale).language))
                }

                is FriendlyException -> {
                    logger.sLog(it.level.toLogLevel(), it.message ?: "Unknown friendly error", it)
                    respond(
                        HttpStatusCode.fromValue(500),
                        ErrorResponse(it.code, it.getFriendlyMessage(locale(supportLocale).language), it.data)
                    )
                }

                else -> {
                    logger.sLog(LogLevel.ERROR, it.message ?: "Unknown error", it)
                    respond(HttpStatusCode.fromValue(500), ErrorResponse("UKN-1", "Unknown error", null))
                }
            }
        }
        var configure: StatusPagesConfig.() -> Unit = {}

        private fun ExceptionLevel.toLogLevel() = when (this) {
            ExceptionLevel.FETAL -> LogLevel.FETAL
            ExceptionLevel.ERROR -> LogLevel.ERROR
            ExceptionLevel.WARNING -> LogLevel.WARNING
            ExceptionLevel.DEBUG -> LogLevel.DEBUG
        }
    }

    private val ExceptionLevel.httpCode
        get() = when (this) {
            ExceptionLevel.ERROR, ExceptionLevel.FETAL -> HttpStatusCode.InternalServerError.value
            else -> HttpStatusCode.BadRequest.value
        }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller =
        KtorApplicationModuleInstaller {
            with(config) {
                install(StatusPages) {
                    exception<Throwable> { call, cause ->
                        call.respondError(cause)
                    }

                    configure()
                }
            }
        }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}