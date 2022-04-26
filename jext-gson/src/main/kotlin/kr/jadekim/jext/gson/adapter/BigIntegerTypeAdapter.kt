package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.math.BigInteger

class BigIntegerTypeAdapter : TypeAdapter<BigInteger>() {

    override fun write(out: JsonWriter, value: BigInteger?) {
        out.value(value?.toString())
    }

    override fun read(input: JsonReader): BigInteger = BigInteger(input.nextString())
}
