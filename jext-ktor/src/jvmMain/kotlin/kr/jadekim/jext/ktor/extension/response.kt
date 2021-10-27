package kr.jadekim.jext.ktor.extension

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kr.jadekim.common.exception.ErrorCode
import kr.jadekim.server.http.exception.HttpServerException
import kr.jadekim.server.protocol.ErrorResponse
import kr.jadekim.server.protocol.Response
import kr.jadekim.server.protocol.SuccessResponse
import java.util.*

suspend fun PipelineContext<*, ApplicationCall>.success(
    body: Any? = null,
    meta: Response.Meta = Response.Meta(),
) = context.respond(SuccessResponse(body, meta))

suspend fun PipelineContext<*, ApplicationCall>.error(
    code: ErrorCode,
    message: String,
    data: Any? = null,
    meta: Response.Meta = Response.Meta(),
) = context.respond(ErrorResponse(code, message, data, meta))

fun HttpServerException.toResponse(
    locale: Locale? = null,
    meta: Response.Meta = Response.Meta(),
) = toResponse(locale?.language , meta)
