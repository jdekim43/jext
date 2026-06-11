package kr.jadekim.jext.ktor.module

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

object ContentNegotiationModule : KtorModuleDefinition<ContentNegotiationModule.Configuration> {

    class Configuration : KtorModuleConfiguration {

        var configure: ContentNegotiationConfig.() -> Unit = {}

        internal val converters: MutableList<Pair<ContentType, ContentConverter>> = mutableListOf()

        fun json(json: Json) {
            converters += ContentType.Application.Json to KotlinxSerializationConverter(json)
        }

        fun jackson(mapper: ObjectMapper) {
            converters += ContentType.Application.Json to JacksonConverter(mapper)
        }

        fun gson(gson: Gson) {
            converters += ContentType.Application.Json to GsonConverter(gson)
        }

        fun register(contentType: ContentType, converter: ContentConverter) {
            converters += contentType to converter
        }
    }

    override fun createApplicationModule(config: Configuration): KtorApplicationModuleInstaller = KtorApplicationModuleInstaller {
        install(ContentNegotiation) {
            config.converters.forEach { register(it.first, it.second) }
            config.configure(this)
        }
    }

    override fun createDefaultConfiguration(): Configuration = Configuration()
}