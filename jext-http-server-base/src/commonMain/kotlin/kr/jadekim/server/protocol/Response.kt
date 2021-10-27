package kr.jadekim.server.protocol

import kotlinx.serialization.Serializable
import kr.jadekim.common.exception.ErrorCode
import kr.jadekim.common.util.currentTimeMillis

@Serializable
open class Response<T, E>(
    val isSuccess: Boolean,
    val body: T? = null,
    val errorBody: ErrorBody<E>? = null,
    val meta: Meta = Meta(),
) {

    @Serializable
    open class ErrorBody<D>(
        val errorCode: ErrorCode,
        val errorMessage: String,
        val data: D? = null,
    )

    @Serializable
    open class Meta(
        val timestamp: Long = currentTimeMillis(),
    )
}

@Suppress("FunctionName")
fun <T> SuccessResponse(
    body: T? = null,
    meta: Response.Meta = Response.Meta(),
) = Response<T, Unit>(true, body = body, meta = meta)

@Suppress("FunctionName")
fun <E> ErrorResponse(
    code: ErrorCode,
    message: String,
    data: E? = null,
    meta: Response.Meta = Response.Meta(),
) = Response<Unit, E>(false, errorBody = Response.ErrorBody(code, message, data), meta = meta)
