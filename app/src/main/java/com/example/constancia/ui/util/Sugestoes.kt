package com.example.constancia.ui.util

import java.time.DayOfWeek

/** Dica mostrada no card "Dica do dia". */
data class Dica(val emoji: String, val titulo: String, val texto: String)

/** Dicas fixas de bem-estar geral (não é orientação médica). */
val DICAS = listOf(
    Dica("💧", "Comece o dia com água", "Deixe um copo ou garrafa perto da cama e beba logo ao acordar. Amarrar o hábito a algo que você já faz facilita lembrar."),
    Dica("🚶", "Caminhe depois das refeições", "Uma caminhada leve de 10 a 15 minutos depois do almoço ou do jantar ajuda a se mexer mais sem precisar de academia."),
    Dica("💤", "Horário fixo para dormir", "Deitar e acordar no mesmo horário, inclusive no fim de semana, costuma melhorar a qualidade do sono."),
    Dica("📵", "Tela longe antes de dormir", "Tente deixar o celular de lado 30 minutos antes de deitar. Um livro ou música calma ajudam na transição."),
    Dica("🍎", "Fruta à vista", "Deixe frutas num lugar visível da cozinha. O que está à mão vira o lanche mais fácil."),
    Dica("🧘", "Respire por 2 minutos", "Pausas curtas de respiração lenta durante o dia ajudam a baixar o ritmo. Comece pequeno: 2 minutos já contam."),
    Dica("🎯", "Comece pequeno", "Um hábito minúsculo feito todo dia vale mais que um grande abandonado. Ler 1 página é melhor que planejar ler 50."),
    Dica("🔗", "Empilhe hábitos", "Prenda o hábito novo a um antigo: \"depois de escovar os dentes, eu alongo por 1 minuto\"."),
    Dica("🪑", "Levante a cada hora", "Se você passa muito tempo sentado, levante, alongue e beba água a cada hora."),
    Dica("☀️", "Luz natural pela manhã", "Alguns minutos de luz do dia logo cedo ajudam o corpo a entender que o dia começou."),
    Dica("📝", "Planeje o amanhã hoje", "Antes de dormir, anote as 3 coisas mais importantes do dia seguinte. A manhã começa com menos decisões."),
    Dica("🥗", "Metade do prato com vegetais", "Uma regra simples para as refeições: encher metade do prato com legumes e verduras."),
    Dica("🔥", "Não quebre a corrente", "Se falhar um dia, volte no dia seguinte. Uma falha não apaga o progresso; duas seguidas começam um novo hábito ruim.")
)

/** Hábito pronto: abre o formulário já preenchido. */
data class Sugestao(
    val nome: String,
    val icone: String,
    val cor: Int,
    val meta: Int? = null,
    val unidade: String = "",
    val dias: Set<DayOfWeek> = DayOfWeek.entries.toSet()
)

private val DIAS_UTEIS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

val SUGESTOES = listOf(
    Sugestao("Beber água", "💧", OPCOES_COR[1], meta = 8, unidade = "copos"),
    Sugestao("Correr", "🏃", OPCOES_COR[0], meta = 5, unidade = "km"),
    Sugestao("Ler", "📚", OPCOES_COR[2], meta = 10, unidade = "páginas"),
    Sugestao("Meditar", "🧘", OPCOES_COR[4], meta = 10, unidade = "minutos"),
    Sugestao("Dormir antes das 23h", "💤", OPCOES_COR[5]),
    Sugestao("Comer uma fruta", "🍎", OPCOES_COR[6], meta = 2, unidade = "frutas"),
    Sugestao("Treinar", "💪", OPCOES_COR[3], dias = DIAS_UTEIS)
)
