package kr.jadekim.jext.ktor.module

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*

object ContentNegotiationFeatureModule : KtorModuleFactory<ContentNegotiationFeatureModule.Configuration> {

    class Configuration : KtorModuleConfiguration {

        var configure: ContentNegotiation.Configuration.() -> Unit = {
            register(ContentType.Application.Json, GsonConverter())
        }

        fun gson(gson: Gson) {
            configure = {
                register(ContentType.Application.Json, GsonConverter(gson))
            }
        }
    }

    override fun create(config: Configuration): KtorModule = ktorModule {
        install(ContentNegotiation, config.configure)
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}