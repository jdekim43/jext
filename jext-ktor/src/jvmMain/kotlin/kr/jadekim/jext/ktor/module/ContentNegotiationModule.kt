package kr.jadekim.jext.ktor.module

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

object ContentNegotiationModule : KtorModuleFactory<ContentNegotiationModule.Configuration> {

    class Configuration : KtorModuleConfiguration {

        var configure: ContentNegotiationConfig.() -> Unit = {
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