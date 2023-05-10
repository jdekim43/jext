package kr.jadekim.jext.gson.adapter

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class KotlinTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!type.rawType.declaredAnnotations.hasKotlinMetadata()) {
            return null
        }

        return KotlinTypeAdapter(gson.getDelegateAdapter(this, type), type)
    }

    private fun Array<Annotation>.hasKotlinMetadata() = any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
}

class KotlinTypeAdapter<T : Any>(private val delegate: TypeAdapter<T>, val type: TypeToken<T>) : TypeAdapter<T>() {

    override fun write(out: JsonWriter?, value: T?) = delegate.write(out, value)

    override fun read(input: JsonReader?): T? {
        return delegate.read(input)?.also { value ->
            (Reflection.getOrCreateKotlinClass(type.rawType) as KClass<Any>).memberProperties.forEach {
                if (!it.returnType.isMarkedNullable && it.get(value) == null) {
                    throw JsonParseException("${it.name} field of ${type.rawType.canonicalName} class cannot be null")
                }
            }
        }
    }
}
