package kr.jadekim.jext.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.*

class YearTypeAdapter : TypeAdapter<Year>() {

    override fun write(out: JsonWriter, value: Year?) {
        out.value(value?.value)
    }

    override fun read(input: JsonReader): Year = Year.of(input.nextInt())
}

class MonthTypeAdapter : TypeAdapter<Month>() {

    override fun write(out: JsonWriter, value: Month?) {
        out.value(value?.value)
    }

    override fun read(input: JsonReader): Month = Month.of(input.nextInt())
}

class YearMonthTypeAdapter : TypeAdapter<YearMonth>() {

    override fun write(out: JsonWriter, value: YearMonth?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value("${value.year}-${value.monthValue}")
        }
    }

    override fun read(input: JsonReader): YearMonth = input.nextString().split("-").let {
        YearMonth.of(it[0].toInt(), it[1].toInt())
    }
}

class MonthDayTypeAdapter : TypeAdapter<MonthDay>() {

    override fun write(out: JsonWriter, value: MonthDay?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value("${value.monthValue}-${value.dayOfMonth}")
        }
    }

    override fun read(input: JsonReader): MonthDay = input.nextString().split("-").let {
        MonthDay.of(it[0].toInt(), it[1].toInt())
    }
}

class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {

    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value("${value.year}-${value.monthValue}-${value.dayOfMonth}")
        }
    }

    override fun read(input: JsonReader): LocalDate = input.nextString().split("-").let {
        LocalDate.of(it[0].toInt(), it[1].toInt(), it[2].toInt())
    }
}
