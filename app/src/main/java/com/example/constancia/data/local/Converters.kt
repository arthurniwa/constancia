package com.example.constancia.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** Ensina o Room a salvar tipos que o SQLite não conhece (datas, horas, dias da semana). */
class Converters {

    // LocalDate ↔ Long (dias desde 1970)
    @TypeConverter
    fun localDateParaLong(data: LocalDate?): Long? = data?.toEpochDay()

    @TypeConverter
    fun longParaLocalDate(valor: Long?): LocalDate? = valor?.let { LocalDate.ofEpochDay(it) }

    // LocalTime ↔ String "HH:mm"
    @TypeConverter
    fun localTimeParaString(hora: LocalTime?): String? = hora?.toString()

    @TypeConverter
    fun stringParaLocalTime(texto: String?): LocalTime? = texto?.let { LocalTime.parse(it) }

    // Set<DayOfWeek> ↔ String "1,3,5" (1 = segunda ... 7 = domingo)
    @TypeConverter
    fun diasParaString(dias: Set<DayOfWeek>): String =
        dias.sorted().joinToString(",") { it.value.toString() }

    @TypeConverter
    fun stringParaDias(texto: String): Set<DayOfWeek> =
        if (texto.isBlank()) emptySet()
        else texto.split(",").map { DayOfWeek.of(it.toInt()) }.toSet()
}
