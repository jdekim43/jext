package kr.jadekim.jext.ktor.module

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object CorsModule : KtorModuleFactory<CorsModule.Configuration> {

    val DEFAULT_ALLOW_HEADERS = setOf(
        HttpHeaders.Accept,
        HttpHeaders.AcceptLanguage,
        HttpHeaders.Authorization,
        HttpHeaders.ContentType,
        HttpHeaders.ContentLanguage,
    )

    class Configuration : KtorModuleConfiguration {
        var hosts: Set<String> = emptySet()
        var methods: Set<HttpMethod> = HttpMethod.DefaultMethods.toSet()
        var headers: Set<String> = DEFAULT_ALLOW_HEADERS
        var headerPredicates: ((String) -> Boolean)? = null
        var exposeHeaders: Set<String> = emptySet()
        var allowCredentials: Boolean = true
        var maxAge: Duration = 3600.toDuration(DurationUnit.SECONDS)
    }

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(CORS) {
            if (config.hosts.isEmpty()) {
                anyHost()
            } else {
                config.hosts.forEach { allowHost(it, listOf("https", "http")) }
            }

            config.methods.forEach { allowMethod(it) }
            config.headers.forEach { allowHeader(it) }
            config.headerPredicates?.let { headerPredicates.add(it) }
            config.exposeHeaders.forEach { exposeHeader(it) }

            allowCredentials = config.allowCredentials
            maxAgeDuration = config.maxAge
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}