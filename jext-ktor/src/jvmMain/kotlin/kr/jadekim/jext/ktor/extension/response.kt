package kr.jadekim.jext.ktor.extension

import io.ktor.server.response.*
import io.ktor.server.routing.*
import kr.jadekim.common.exception.ErrorCode
import kr.jadekim.server.http.exception.HttpServerException
import kr.jadekim.server.protocol.ErrorResponse
import kr.jadekim.server.protocol.Response
import kr.jadekim.server.protocol.SuccessResponse
import java.util.*

suspend inline fun <reified T> RoutingContext.success(
    body: T? = null,
    meta: Response.Meta = Response.Meta(),
) = call.respond(SuccessResponse(body, meta))

suspend inline fun <reified E> RoutingContext.error(
    code: ErrorCode,
    message: String,
    data: E? = null,
    meta: Response.Meta = Response.Meta(),
) = call.respond(ErrorResponse(code, message, data, meta))

fun HttpServerException.toResponse(
    locale: Locale? = null,
    meta: Response.Meta = Response.Meta(),
) = toResponse(locale?.language, meta)
