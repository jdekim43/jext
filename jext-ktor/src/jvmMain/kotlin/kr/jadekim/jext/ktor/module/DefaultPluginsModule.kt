package kr.jadekim.jext.ktor.module

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.util.*
import io.ktor.util.date.*

private val ATTRIBUTE_CACHE_OPTION: AttributeKey<CachingOptions> = AttributeKey("CachingHeader.max_age")

fun ApplicationCall.cacheOption(option: CacheControl? = null, expires: GMTDate? = null) {
    attributes.put(ATTRIBUTE_CACHE_OPTION, CachingOptions(option, expires))
}

object DefaultFeatureModule : KtorModuleFactory<DefaultFeatureModule.Configuration> {

    class Configuration : KtorModuleConfiguration

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(XForwardedHeaders) {
            forHeaders += "CF-Connecting-IP"
        }

        install(DoubleReceive)

        install(AutoHeadResponse)

        install(CachingHeaders) {
            options { call, _ ->
                call.attributes.getOrNull(ATTRIBUTE_CACHE_OPTION)
            }
        }

        install(CallId) {
            retrieveFromHeader("traceparent")
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}