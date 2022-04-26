package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.UUID

class UUIDTypeAdapter : TypeAdapter<UUID>() {

    override fun write(out: JsonWriter, value: UUID?) {
        out.value(value?.toString())
    }

    override fun read(input: JsonReader): UUID = UUID.fromString(input.nextString())
}