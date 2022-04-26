package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.*
import java.util.*

class DateAsLongTypeAdapter : TypeAdapter<Date>() {

    override fun write(out: JsonWriter, value: Date?) {
        out.value(value?.time)
    }

    override fun read(input: JsonReader): Date = Date(input.nextLong())
}

class LocalDateTimeAsLongTypeAdapter : TypeAdapter<LocalDateTime>() {

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        out.value(value?.toInstant(ZoneOffset.UTC)?.toEpochMilli())
    }

    override fun read(input: JsonReader): LocalDateTime {
        val value = input.nextLong()

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)
    }
}

class OffsetDateTimeAsLongTypeAdapter : TypeAdapter<OffsetDateTime>() {

    override fun write(out: JsonWriter, value: OffsetDateTime?) {
        out.value(value?.atZoneSameInstant(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
    }

    override fun read(input: JsonReader): OffsetDateTime {
        val value = input.nextLong()

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)
    }
}

class ZonedDateTimeAsLongTypeAdapter : TypeAdapter<ZonedDateTime>() {

    override fun write(out: JsonWriter, value: ZonedDateTime?) {
        out.value(value?.toInstant()?.toEpochMilli())
    }

    override fun read(input: JsonReader): ZonedDateTime {
        val value = input.nextLong()

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)
    }
}

class DurationAsLongTypeAdapter : TypeAdapter<Duration>() {

    override fun write(out: JsonWriter, value: Duration?) {
        out.value(value?.toMillis())
    }

    override fun read(input: JsonReader): Duration {
        return Duration.ofMillis(input.nextLong())
    }
}