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

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val delegate = gson.getDelegateAdapter(this, type)

        if (!type.rawType.declaredAnnotations.hasKotlinMetadata()) {
            return null
        }

        return object : TypeAdapter<T>() {

            override fun write(out: JsonWriter?, value: T) = delegate.write(out, value)

            override fun read(input: JsonReader?): T? {
                return delegate.read(input)?.also { value ->
                    (Reflection.createKotlinClass(type.rawType) as KClass<Any>).memberProperties.forEach {
                        if (!it.returnType.isMarkedNullable && it.get(value) == null) {
                            throw JsonParseException("${it.name} field of ${type.rawType.canonicalName} class cannot be null")
                        }
                    }
                }
            }
        }
    }

    private fun Array<Annotation>.hasKotlinMetadata() = any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
}