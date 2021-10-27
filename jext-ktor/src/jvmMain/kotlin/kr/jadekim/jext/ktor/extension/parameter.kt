package kr.jadekim.jext.ktor.extension

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kr.jadekim.server.http.exception.MissingParameterException

inline val PipelineContext<*, ApplicationCall>.pathParam: Parameters get() = context.parameters

inline val PipelineContext<*, ApplicationCall>.queryParam: Parameters get() = context.request.queryParameters

suspend fun PipelineContext<*, ApplicationCall>.bodyParam(): Parameters? = context.receiveOrNull()

fun PipelineContext<*, ApplicationCall>.pathParamSafe(key: String, default: String? = null): String? {
    return pathParam[key] ?: default
}

fun PipelineContext<*, ApplicationCall>.pathParam(key: String, default: String? = null): String {
    return pathParamSafe(key, default) ?: throw MissingParameterException("required $key")
}

fun PipelineContext<*, ApplicationCall>.queryParamSafe(key: String, default: String? = null): String? {
    return queryParam[key] ?: default
}

fun PipelineContext<*, ApplicationCall>.queryParam(key: String, default: String? = null): String {
    return queryParamSafe(key, default) ?: throw MissingParameterException("required $key")
}

suspend fun PipelineContext<*, ApplicationCall>.bodyParamListSafe(key: String): List<String> {
    return bodyParam()?.getAll(key) ?: emptyList()
}

suspend fun PipelineContext<*, ApplicationCall>.bodyParamList(key: String): List<String> {
    val result = bodyParamListSafe(key)

    if (result.isEmpty()) {
        throw MissingParameterException("required $key")
    }

    return result
}

suspend fun PipelineContext<*, ApplicationCall>.bodyParamSafe(key: String, default: String? = null): String? {
    return bodyParam()?.get(key) ?: default
}

suspend fun PipelineContext<*, ApplicationCall>.bodyParam(key: String, default: String? = null): String {
    return bodyParamSafe(key, default) ?: throw MissingParameterException("required $key")
}

fun Parameters?.toSingleValueMap(): Map<String, String> {
    return this?.toMap()
        ?.mapValues { it.value.firstOrNull() }
        ?.filterValues { !it.isNullOrBlank() }
        ?.mapValues { it.value!! }
        ?: emptyMap()
}

val HttpMethod.canReadBody
    get() = when (this) {
        HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch -> true
        else -> false
    }
