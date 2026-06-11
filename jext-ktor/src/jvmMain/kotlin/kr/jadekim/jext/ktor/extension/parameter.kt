package kr.jadekim.jext.ktor.extension

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kr.jadekim.server.http.exception.MissingParameterException

inline val RoutingContext.pathParam: Parameters get() = call.parameters

inline val RoutingContext.queryParam: Parameters get() = call.request.queryParameters

suspend fun RoutingContext.bodyParam(): Parameters? =
    runCatching { call.receiveNullable<Parameters>() }.getOrNull()

fun RoutingContext.pathParamSafe(key: String, default: String? = null): String? {
    return pathParam[key] ?: default
}

fun RoutingContext.pathParam(key: String, default: String? = null): String {
    return pathParamSafe(key, default) ?: throw MissingParameterException("required $key")
}

fun RoutingContext.queryParamSafe(key: String, default: String? = null): String? {
    return queryParam[key] ?: default
}

fun RoutingContext.queryParam(key: String, default: String? = null): String {
    return queryParamSafe(key, default) ?: throw MissingParameterException("required $key")
}

suspend fun RoutingContext.bodyParamListSafe(key: String): List<String> {
    return bodyParam()?.getAll(key) ?: emptyList()
}

suspend fun RoutingContext.bodyParamList(key: String): List<String> {
    val result = bodyParamListSafe(key)

    if (result.isEmpty()) {
        throw MissingParameterException("required $key")
    }

    return result
}

suspend fun RoutingContext.bodyParamSafe(key: String, default: String? = null): String? {
    return bodyParam()?.get(key) ?: default
}

suspend fun RoutingContext.bodyParam(key: String, default: String? = null): String {
    return bodyParamSafe(key, default) ?: throw MissingParameterException("required $key")
}

suspend inline fun <reified T : Any> RoutingContext.body(): T = call.receive()

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
