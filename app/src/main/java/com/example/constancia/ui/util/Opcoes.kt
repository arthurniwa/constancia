package com.example.constancia.ui.util

import java.time.DayOfWeek

/** Emojis que o usuário pode escolher como ícone do hábito. */
val OPCOES_ICONE = listOf("💧", "🏃", "📚", "🧘", "🍎", "💤", "✍️", "💪", "🎸", "🧹", "💊", "🌱")

/** Paleta de cores (ARGB). `0xFF...toInt()` porque o literal hexadecimal é Long em Kotlin. */
val OPCOES_COR = listOf(
    0xFF4CAF50.toInt(), // verde
    0xFF2196F3.toInt(), // azul
    0xFFFF9800.toInt(), // laranja
    0xFFE91E63.toInt(), // rosa
    0xFF9C27B0.toInt(), // roxo
    0xFF009688.toInt(), // verde-azulado
    0xFFF44336.toInt(), // vermelho
    0xFF795548.toInt()  // marrom
)

private val NOMES_CURTOS = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

/**
 * Função de extensão: "adiciona" um método a um tipo que não é seu
 * (≈ extension method `static string NomeCurto(this DayOfWeek d)` do C#).
 * DayOfWeek.value vai de 1 (segunda) a 7 (domingo).
 */
fun DayOfWeek.nomeCurto(): String = NOMES_CURTOS[value - 1]

/** Ex.: "Todos os dias", "Seg, Qua, Sex". */
fun Set<DayOfWeek>.resumo(): String =
    if (size == 7) "Todos os dias"
    else sorted().joinToString(", ") { it.nomeCurto() }
