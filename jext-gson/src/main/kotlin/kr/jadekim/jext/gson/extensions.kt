package kr.jadekim.jext.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Reader

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(json: Reader): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(reader: JsonReader): T = fromJson<T>(reader, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(json: JsonElement): T = fromJson<T>(json, object : TypeToken<T>() {}.type)
