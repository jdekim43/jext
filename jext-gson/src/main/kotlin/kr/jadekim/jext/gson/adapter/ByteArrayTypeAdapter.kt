 package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kr.jadekim.common.encoder.Encoder

class ByteArrayTypeAdapter(val mode: Mode = Mode.BASE64) : TypeAdapter<ByteArray>() {

    enum class Mode(val encoder: Encoder) {
        BASE64(kr.jadekim.common.encoder.BASE64),
        HEX(kr.jadekim.common.encoder.HEX),
    }

    override fun write(writer: JsonWriter, value: ByteArray?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.value(mode.encoder.encodeToString(value))
    }

    override fun read(reader: JsonReader): ByteArray? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        return mode.encoder.decode(reader.nextString())
    }
}