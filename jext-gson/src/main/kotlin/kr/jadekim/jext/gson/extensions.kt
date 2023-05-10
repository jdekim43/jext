package kr.jadekim.jext.gson

import com.google.gson.Gson
 import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Reader

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(json: Reader): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(reader: JsonReader): T = fromJson<T>(reader, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(json: JsonElement): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

fun JsonElement.sorted(): JsonElement = when (this) {
    is JsonObject -> sorted()
    is JsonArray -> sorted()
    else -> this
}

fun JsonObject.sorted(): JsonObject {
    val entries = entrySet().sortedBy { it.key }
    val json = JsonObject()

    for (entry in entries) {
        json.add(entry.key, entry.value.sorted())
    }

    return json
}

fun JsonArray.sorted(): JsonArray {
    val json = JsonArray()
    for (item in this) {
        val value = when (item) {
            is JsonObject -> item.sorted()
            is JsonArray -> item.sorted()
            else -> item
        }

        json.add(value)
    }

    return json
}
