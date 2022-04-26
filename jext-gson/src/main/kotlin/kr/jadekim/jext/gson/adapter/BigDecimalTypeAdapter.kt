package kr.jadekim.jext.gson.adapter

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal
import java.math.BigInteger

class BigDecimalTypeAdapter : TypeAdapter<BigDecimal>() {

    override fun write(out: JsonWriter, value: BigDecimal?) {
        out.value(value?.toPlainString())
    }

    override fun read(input: JsonReader): BigDecimal = BigDecimal(input.nextString())
}

class BigDecimalObjectTypeAdapter : TypeAdapter<BigDecimal>() {

    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.beginObject()
            out.name("scale").value(value.scale())
            out.name("value").value(value.unscaledValue().toString())
            out.endObject()
        }
    }

    override fun read(input: JsonReader): BigDecimal {
        var scale: Int? = null
        var value: String? = null

        input.beginObject()
        while (input.hasNext()) {
            when (input.nextName()) {
                "scale" -> scale = input.nextInt()
                "value" -> value = input.nextString()
            }
        }
        input.endObject()

        if (scale == null || value == null) {
            throw JsonParseException("scale and value field of BigDecimalObjectTypeAdapter cannot be null")
        }

        return BigDecimal(BigInteger(value), scale)
    }
}