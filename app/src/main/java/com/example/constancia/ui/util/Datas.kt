package com.example.constancia.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

private val FORMATO_LONGO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR)
private val FORMATO_CURTO = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR)

/** "Terça-feira, 10 de novembro" */
fun LocalDate.formatoLongo(): String =
    format(FORMATO_LONGO).replaceFirstChar { it.titlecase(PT_BR) }

/** "Novembro de 2026" */
fun LocalDate.mesAno(): String =
    format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT_BR)).replaceFirstChar { it.titlecase(PT_BR) }

/** "Ter, 10/11/2026" */
fun LocalDate.formatoCurto(): String = "${dayOfWeek.nomeCurto()}, ${format(FORMATO_CURTO)}"
