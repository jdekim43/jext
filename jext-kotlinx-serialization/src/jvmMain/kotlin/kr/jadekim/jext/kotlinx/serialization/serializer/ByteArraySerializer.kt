package kr.jadekim.jext.kotlinx.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kr.jadekim.common.encoder.Base64
import kr.jadekim.common.encoder.Hex
import kr.jadekim.common.encoder.decode
import kr.jadekim.common.encoder.encode

object HexByteArraySerializer : KSerializer<ByteArray> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HexByteArray", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ByteArray = decoder.decodeString().decode(Hex)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.encode(Hex))
    }
}

object Base64ByteArraySerializer : KSerializer<ByteArray> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Base64ByteArray", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ByteArray = decoder.decodeString().decode(Base64)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.encode(Base64))
    }
}
