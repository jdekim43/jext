package kr.jadekim.jext.kotlinx.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

val KotlinTimeAsLongSerializerModule = SerializersModule {
    contextual(InstantAsLongSerializer)
    contextual(DurationAsLongSerializer)
}

object InstantAsLongSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val value = decoder.decodeLong()

        return Instant.fromEpochMilliseconds(value)
    }
}

object DurationAsLongSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DurationAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeLong(value.toLong(DurationUnit.MILLISECONDS))
    }

    override fun deserialize(decoder: Decoder): Duration {
        return decoder.decodeLong().toDuration(DurationUnit.MILLISECONDS)
    }
}