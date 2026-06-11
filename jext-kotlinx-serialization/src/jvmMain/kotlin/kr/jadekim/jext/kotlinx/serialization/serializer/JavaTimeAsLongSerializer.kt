package kr.jadekim.jext.kotlinx.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.*
import java.util.*

val JavaTimeAsLongSerializerModule = SerializersModule {
    contextual(JavaDateAsLongSerializer)
    contextual(JavaInstantAsLongSerializer)
    contextual(JavaLocalDateTimeAsLongSerializer)
    contextual(JavaOffsetDateTimeAsLongSerializer)
    contextual(JavaZonedDateTimeAsLongSerializer)
    contextual(JavaDurationAsLongSerializer)
}

object JavaDateAsLongSerializer : KSerializer<Date> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)

    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}

object JavaLocalDateTimeAsLongSerializer : KSerializer<LocalDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTimeAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeLong(value.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val value = decoder.decodeLong()

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)
    }
}

object JavaOffsetDateTimeAsLongSerializer : KSerializer<OffsetDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTimeAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeLong(value.atZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val value = decoder.decodeLong()

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)
    }
}

object JavaZonedDateTimeAsLongSerializer : KSerializer<ZonedDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTimeAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeLong(value.withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val value = decoder.decodeLong()

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)
    }
}

object JavaInstantAsLongSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val value = decoder.decodeLong()

        return Instant.ofEpochMilli(value)
    }
}

object JavaDurationAsLongSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DurationAsLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeLong(value.toMillis())
    }

    override fun deserialize(decoder: Decoder): Duration {
        return Duration.ofMillis(decoder.decodeLong())
    }
}