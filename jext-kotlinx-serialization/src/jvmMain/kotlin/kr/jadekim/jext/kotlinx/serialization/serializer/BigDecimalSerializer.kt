package kr.jadekim.jext.kotlinx.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.math.BigDecimal
import java.math.BigInteger

object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "BigDecimal",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal = decoder.decodeString().toBigDecimal()
}

object BigDecimalObjectSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BigDecimalObject") {
        element<Int>("scale")
        element<String>("value")
    }

    override fun deserialize(decoder: Decoder): BigDecimal = decoder.decodeStructure(descriptor) {
        var scale: Int? = null
        var value: String? = null
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> scale = decodeIntElement(descriptor, 0)
                1 -> value = decodeStringElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        BigDecimal(BigInteger(value!!), scale!!)
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.scale())
        encodeStringElement(descriptor, 1, value.unscaledValue().toString())
    }
}