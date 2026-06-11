package kr.jadekim.jext.ktor.module

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.util.*
import io.ktor.util.date.*

private val ATTRIBUTE_CACHE_OPTION: AttributeKey<CachingOptions> = AttributeKey("CachingHeader.options")

fun ApplicationCall.cacheOption(option: CacheControl? = null, expires: GMTDate? = null) {
    attributes.put(ATTRIBUTE_CACHE_OPTION, CachingOptions(option, expires))
}

object DefaultFeatureModule : KtorModuleDefinition<DefaultFeatureModule.Configuration> {

    class Configuration : KtorModuleConfiguration {
        var xForwardedHeaders: List<String> = listOf(
            "CF-Connecting-IP",
            "CLIENT_IP",
            HttpHeaders.XForwardedFor,
        )

        var cachingOptions: ((ApplicationCall, OutgoingContent) -> CachingOptions?)? = null

        var enableCompression: Boolean = true
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller = KtorApplicationModuleInstaller {
        if (config.xForwardedHeaders.isNotEmpty()) {
            install(XForwardedHeaders) {
                forHeaders.clear()
                forHeaders.addAll(config.xForwardedHeaders)
            }
        }

        install(DoubleReceive)

        install(AutoHeadResponse)

        install(CachingHeaders) {
            options { call, content ->
                call.attributes.getOrNull(ATTRIBUTE_CACHE_OPTION) ?: config.cachingOptions?.invoke(call, content)
            }
        }

        if (config.enableCompression) {
            install(Compression)
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}