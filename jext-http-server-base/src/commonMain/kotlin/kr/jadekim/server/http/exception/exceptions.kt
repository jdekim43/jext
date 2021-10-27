package kr.jadekim.server.http.exception

import kr.jadekim.common.exception.ExceptionLevel
import kr.jadekim.common.exception.FriendlyException
import kr.jadekim.common.exception.Language
import kr.jadekim.server.protocol.ErrorResponse
import kr.jadekim.server.protocol.Response

open class HttpServerException(
    code: String,
    val httpStatus: Int,
    data: Any? = null,
    cause: Throwable? = null,
    message: String? = cause?.message,
    level: ExceptionLevel = ExceptionLevel.ERROR,
) : FriendlyException(code, data, cause, message, level) {

    constructor(exception: FriendlyException, httpStatus: Int = 500) : this(
        exception.code,
        httpStatus,
        exception.data,
        exception.cause,
        exception.message,
        exception.level,
    )

    open fun toResponse(
        language: Language? = null,
        meta: Response.Meta = Response.Meta(),
    ) = ErrorResponse(code, getFriendlyMessage(language), data, meta)
}

class NotFoundException(
    cause: Throwable? = null,
) : HttpServerException(
    code = "CAS-1",
    httpStatus = 404,
    cause = cause,
)

class UnauthorizedException(
    message: String,
    cause: Throwable? = null,
) : HttpServerException(
    code = "CAS-2",
    httpStatus = 401,
    message = message,
    cause = cause,
)

class MaintenanceException : HttpServerException(
    code = "CAS-3",
    httpStatus = 502,
    level = ExceptionLevel.DEBUG,
)

class MissingParameterException(
    message: String,
    cause: Throwable? = null
) : HttpServerException(
    code = "CAS-4",
    httpStatus = 400,
    message = message,
    cause = cause
)

class InvalidParameterException(
    message: String,
    cause: Throwable? = null
) : HttpServerException(
    code = "CAS-5",
    httpStatus = 400,
    message = message,
    cause = cause
)
