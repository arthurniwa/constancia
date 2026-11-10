package com.example.constancia.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * O SQLite só guarda tipos simples (INTEGER, TEXT, REAL, BLOB).
 * TypeConverters ensinam o Room a converter tipos "ricos" ↔ tipos do banco.
 * ≈ ValueConverter do Entity Framework (HasConversion).
 */
class Converters {

    // LocalDate ↔ Long (dias desde 01/01/1970). Número ordena e compara bem no SQL.
    @TypeConverter
    fun localDateParaLong(data: LocalDate?): Long? = data?.toEpochDay()
    // `data?.toEpochDay()`: safe call (≈ `data?.ToEpochDay()` do C#). Se data for null, resultado é null.

    @TypeConverter
    fun longParaLocalDate(valor: Long?): LocalDate? = valor?.let { LocalDate.ofEpochDay(it) }
    // `let { ... }` executa o bloco só se não for null; `it` é o valor (lambda com 1 parâmetro).

    // LocalTime ↔ String "HH:mm"
    @TypeConverter
    fun localTimeParaString(hora: LocalTime?): String? = hora?.toString()

    @TypeConverter
    fun stringParaLocalTime(texto: String?): LocalTime? = texto?.let { LocalTime.parse(it) }

    // Set<DayOfWeek> ↔ String "1,3,5" (1 = segunda ... 7 = domingo)
    @TypeConverter
    fun diasParaString(dias: Set<DayOfWeek>): String =
        dias.sorted().joinToString(",") { it.value.toString() }
    // joinToString ≈ string.Join(",", dias.Select(d => ...)) do C#

    @TypeConverter
    fun stringParaDias(texto: String): Set<DayOfWeek> =
        if (texto.isBlank()) emptySet()
        else texto.split(",").map { DayOfWeek.of(it.toInt()) }.toSet()
    // `if` em Kotlin é expressão (retorna valor) ≈ operador ternário `? :` do C#.
}
