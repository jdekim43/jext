package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kr.jadekim.common.encoder.Base64
import kr.jadekim.common.encoder.Encoder

class ByteArrayTypeAdapter(val encoder: Encoder<ByteArray, String> = Base64) : TypeAdapter<ByteArray>() {

    override fun write(writer: JsonWriter, value: ByteArray?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.value(encoder.encode(value))
    }

    override fun read(reader: JsonReader): ByteArray? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        return encoder.decode(reader.nextString())
    }
}