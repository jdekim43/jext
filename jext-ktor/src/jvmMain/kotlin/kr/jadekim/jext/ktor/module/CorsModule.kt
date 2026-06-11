package kr.jadekim.jext.ktor.module

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS
import kotlin.collections.Set
import kotlin.collections.emptySet
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.setOf
import kotlin.collections.toSet
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object CorsModule : KtorModuleDefinition<CorsModule.Configuration> {

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
        var maxAge: Duration = 3600.toDuration(DurationUnit.SECONDS)

        var configure: CORSConfig.() -> Unit = {}
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller = KtorApplicationModuleInstaller {
        install(CORS) {
            if (config.hosts.isEmpty()) {
                anyHost()
            } else {
                config.hosts.forEach { allowHost(it, listOf("https", "http")) }
            }

            methods.addAll(config.methods)
            headers.addAll(config.headers)

            maxAgeDuration = config.maxAge

            config.configure(this)
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}