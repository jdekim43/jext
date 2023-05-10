package kr.jadekim.jext.gson.adapter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SealedClassName(val name: String)

fun KClass<*>.getSealedClassName(): String = findAnnotation<SealedClassName>()?.name ?: jvmName

sealed interface TypeAwareMode {

    object KeyValue : TypeAwareMode

//    data class Property(val typeKey: String = "type", val valueKey: String = "value") : TypeAwareMode
}

class SealedClassTypeAdapterFactory(
    val mode: TypeAwareMode,
    val alternatives: List<TypeAdapterFactory> = emptyList(),
) : TypeAdapterFactory {

    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val klass = Reflection.getOrCreateKotlinClass(type.rawType)

        if (!klass.isSealed) {
            return null
        }

        return SealedParentClassKeyValueTypeAdapter(klass, gson)
    }
}

class SealedParentClassKeyValueTypeAdapter<T : Any>(
    val klass: KClass<Any>,
    private val gson: Gson,
    private val alternatives: List<TypeAdapterFactory> = emptyList(),
) : TypeAdapter<T>() {

    override fun write(writer: JsonWriter, value: T?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        val alternative = alternatives.firstNotNullOfOrNull { it.create(gson, TypeToken.get(value.javaClass)) }
            ?: gson.getAdapter(value.javaClass)

        writer.beginObject()
        writer.name(value::class.getSealedClassName())
        alternative.write(writer, value)
        writer.endObject()
    }

    override fun read(reader: JsonReader): T? {
        if (reader.peek() == JsonToken.NULL) {
            return null
        }

        reader.beginObject()
        val name = reader.nextName()

        @Suppress("UNCHECKED_CAST")
        val innerKlass = klass.sealedSubclasses.firstOrNull { it.getSealedClassName() == name } as? KClass<T>
            ?: throw Exception("$name is not found to be a data class of the sealed class ${klass.jvmName}")

        val value = gson.getAdapter(innerKlass.java).read(reader)
        reader.endObject()

        return innerKlass.objectInstance ?: value
    }
}
