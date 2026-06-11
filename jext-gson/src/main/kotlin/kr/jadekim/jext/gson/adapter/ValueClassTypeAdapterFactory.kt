package kr.jadekim.jext.gson.adapter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.jvm.internal.Reflection
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Value

class ValueClassTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!type.rawType.isAnnotationPresent(ValueClass::class.java)) {
            return null
        }

        return object : TypeAdapter<T>() {
            val field = Reflection.getOrCreateKotlinClass(type.rawType).memberProperties.find { it.hasAnnotation<Value>() }
                ?: throw IllegalArgumentException("Required Value annotation")

            override fun write(writer: JsonWriter, value: T) {
                when (field.returnType) {
                    typeOf<String>() -> writer.value(field.get(value) as String)
                    typeOf<Long>() -> writer.value(field.get(value) as Long)
                    typeOf<Int>() -> writer.value(field.get(value) as Int)
                    typeOf<Boolean>() -> writer.value(field.get(value) as Boolean)
                    typeOf<Float>() -> writer.value(field.get(value) as Float)
                    typeOf<Double>() -> writer.value(field.get(value) as Double)
                    else -> throw IllegalArgumentException("Not support type")
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun read(reader: JsonReader): T {
                return when (field.returnType) {
                    typeOf<String>() -> type.rawType.getConstructor(String::class.java).newInstance(reader.nextString()) as T
                    typeOf<Long>() -> type.rawType.getConstructor(Long::class.java).newInstance(reader.nextLong()) as T
                    typeOf<Int>() -> type.rawType.getConstructor(Int::class.java).newInstance(reader.nextInt()) as T
                    typeOf<Boolean>() -> type.rawType.getConstructor(Boolean::class.java).newInstance(reader.nextBoolean()) as T
                    typeOf<Float>() -> type.rawType.getConstructor(Float::class.java).newInstance(reader.nextDouble().toFloat()) as T
                    typeOf<Double>() -> type.rawType.getConstructor(Double::class.java).newInstance(reader.nextDouble()) as T
                    else -> throw IllegalArgumentException("Not support type")
                }
            }
        }
    }
}