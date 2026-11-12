package com.example.constancia.ui.util

import java.time.DayOfWeek

/** Emojis disponíveis para o ícone do hábito. */
val OPCOES_ICONE = listOf("💧", "🏃", "📚", "🧘", "🍎", "💤", "✍️", "💪", "🎸", "🧹", "💊", "🌱")

/** Paleta de cores (ARGB). */
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

/** Atalhos de unidade no formulário (o usuário também pode digitar outra). */
val OPCOES_UNIDADE = listOf("copos", "km", "páginas", "minutos", "vezes", "litros", "passos")

/** Sugere a unidade pelo nome do hábito ("Correr" → "km"); null se não reconhecer. */
fun unidadeSugerida(nome: String): String? {
    val n = nome.lowercase()
    return when {
        listOf("água", "agua").any { it in n } -> "copos"
        listOf("corr", "caminh", "pedal", "bike", "nad").any { it in n } -> "km"
        listOf("ler", "leitura", "livro").any { it in n } -> "páginas"
        listOf("medit", "estud", "along", "yoga").any { it in n } -> "minutos"
        "passo" in n -> "passos"
        else -> null
    }
}

private val NOMES_CURTOS = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

/** Função de extensão: "Seg", "Ter"... (value 1 = segunda). */
fun DayOfWeek.nomeCurto(): String = NOMES_CURTOS[value - 1]

/** Ex.: "Todos os dias", "Seg, Qua, Sex". */
fun Set<DayOfWeek>.resumo(): String =
    if (size == 7) "Todos os dias"
    else sorted().joinToString(", ") { it.nomeCurto() }
