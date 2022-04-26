package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.Locale

class LocaleTypeAdapter : TypeAdapter<Locale>() {

    override fun write(out: JsonWriter, value: Locale?) {
        out.value(value?.toLanguageTag())
    }

    override fun read(input: JsonReader): Locale = Locale.forLanguageTag(input.nextString())
}