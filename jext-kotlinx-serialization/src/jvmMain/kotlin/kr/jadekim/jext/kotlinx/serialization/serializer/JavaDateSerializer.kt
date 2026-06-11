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

val LocalDateDefaultFormatSerializerModule = SerializersModule {
    contextual(LocalDateSerializer)
    contextual(YearSerializer)
    contextual(MonthSerializer)
    contextual(YearMonthSerializer)
    contextual(MonthDaySerializer)
}

object YearSerializer : KSerializer<Year> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YearAsNumber", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Year) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): Year = Year.of(decoder.decodeInt())
}

object MonthSerializer : KSerializer<Month> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MonthAsNumber", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Month) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): Month = Month.of(decoder.decodeInt())
}

object YearMonthSerializer : KSerializer<YearMonth> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YearMonthFormatted", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: YearMonth) {
        encoder.encodeString("${value.year}-${value.monthValue}")
    }

    override fun deserialize(decoder: Decoder): YearMonth = decoder.decodeString().split("-").let {
        YearMonth.of(it[0].toInt(), it[1].toInt())
    }
}

object MonthDaySerializer : KSerializer<MonthDay> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MonthDayFormatted", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MonthDay) {
        encoder.encodeString("${value.monthValue}-${value.dayOfMonth}")
    }

    override fun deserialize(decoder: Decoder): MonthDay = decoder.decodeString().split("-").let {
        MonthDay.of(it[0].toInt(), it[1].toInt())
    }
}

object LocalDateSerializer : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateFormatted", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString("${value.year}-${value.monthValue}-${value.dayOfMonth}")
    }

    override fun deserialize(decoder: Decoder): LocalDate = decoder.decodeString().split("-").let {
        LocalDate.of(it[0].toInt(), it[1].toInt(), it[2].toInt())
    }
}