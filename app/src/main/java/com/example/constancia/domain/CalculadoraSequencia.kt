package com.example.constancia.domain

import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.RegistroDiario
import java.time.LocalDate

/** Resultado do cálculo. */
data class Estatisticas(
    val sequenciaAtual: Int,
    val recorde: Int,
    val totalConcluidos: Int
)

/** Situação de um dia para um hábito. Usada na sequência E nos gráficos (mesma regra nos dois). */
enum class EstadoDia {
    CONCLUIDO,        // valor ≥ meta → soma na sequência
    PARCIAL,          // fez um pouco, mas não bateu a meta (dia passado) → quebra
    FALHA,            // dia programado, passado, sem nada → quebra
    HOJE_PENDENTE,    // hoje ainda não concluído → neutro (o dia não acabou)
    FERIADO,          // feriado nacional (BrasilAPI) → neutro
    NAO_PROGRAMADO,   // ex.: sábado num hábito de seg–sex → neutro
    FORA_DO_PERIODO   // antes do início do hábito ou no futuro → neutro
}

/** Um dia já classificado, pronto para desenhar (fracao = quanto do anel/barra preencher, 0..1). */
data class DiaResumo(val data: LocalDate, val valor: Int, val estado: EstadoDia, val fracao: Float)

/**
 * Regra de negócio pura (sem Android, sem banco): recebe dados e devolve números.
 * Por isso dá para testar com um teste unitário simples (ver CalculadoraSequenciaTest).
 * `object` = singleton sem estado (≈ uma `static class` do C#).
 *
 * Como cada dia conta para a sequência:
 *  - CONCLUIDO                                   → soma +1
 *  - HOJE_PENDENTE, FERIADO, NAO_PROGRAMADO, FORA → neutro: não soma, não quebra
 *  - PARCIAL, FALHA                              → QUEBRA a sequência
 */
object CalculadoraSequencia {

    /** Primeiro dia que conta: criação do hábito, ou antes se o usuário registrou dias anteriores. */
    fun inicio(habito: Habito, registros: List<RegistroDiario>): LocalDate =
        minOf(habito.dataCriacao, registros.minOfOrNull { it.data } ?: habito.dataCriacao)

    /** Classifica um dia. A ORDEM dos testes importa: concluído vale mesmo em feriado ou fim de semana. */
    fun estadoDoDia(
        habito: Habito,
        valor: Int,
        dia: LocalDate,
        feriados: Set<LocalDate>,
        hoje: LocalDate,
        inicio: LocalDate
    ): EstadoDia = when {
        valor >= habito.alvoDiario -> EstadoDia.CONCLUIDO
        dia.isAfter(hoje) || dia.isBefore(inicio) -> EstadoDia.FORA_DO_PERIODO
        !habito.programadoPara(dia) -> EstadoDia.NAO_PROGRAMADO
        dia in feriados -> EstadoDia.FERIADO
        dia == hoje -> EstadoDia.HOJE_PENDENTE
        valor > 0 -> EstadoDia.PARCIAL
        else -> EstadoDia.FALHA
    }

    /** Lista de dias [de, ate] já classificados — alimenta o gráfico semanal e o mapa do mês. */
    fun resumir(
        habito: Habito,
        registros: List<RegistroDiario>,
        de: LocalDate,
        ate: LocalDate,
        feriados: Set<LocalDate> = emptySet(),
        hoje: LocalDate = LocalDate.now()
    ): List<DiaResumo> {
        val valorPorDia = registros.associate { it.data to it.valor }
        val inicio = inicio(habito, registros)
        // generateSequence ≈ um `yield return` em loop: de, de+1, de+2... até passar de `ate`.
        return generateSequence(de) { it.plusDays(1) }
            .takeWhile { !it.isAfter(ate) }
            .map { dia ->
                val valor = valorPorDia[dia] ?: 0
                DiaResumo(
                    data = dia,
                    valor = valor,
                    estado = estadoDoDia(habito, valor, dia, feriados, hoje, inicio),
                    fracao = (valor.toFloat() / habito.alvoDiario).coerceIn(0f, 1f)
                )
            }
            .toList()
    }

    fun calcular(
        habito: Habito,
        registros: List<RegistroDiario>,
        feriados: Set<LocalDate> = emptySet(),
        hoje: LocalDate = LocalDate.now()
    ): Estatisticas {
        // Map data → valor (≈ ToDictionary). Busca O(1) para cada dia.
        val valorPorDia: Map<LocalDate, Int> = registros.associate { it.data to it.valor }
        // `to` cria um par (Pair) — ≈ uma tupla (data, valor) do C#.

        val inicio = inicio(habito, registros)
        if (inicio.isAfter(hoje)) return Estatisticas(0, 0, 0)

        fun estado(dia: LocalDate) = estadoDoDia(habito, valorPorDia[dia] ?: 0, dia, feriados, hoje, inicio)
        fun quebra(e: EstadoDia) = e == EstadoDia.FALHA || e == EstadoDia.PARCIAL

        // Sequência ATUAL: anda de hoje para trás até encontrar uma falha.
        var atual = 0
        var dia = hoje
        while (!dia.isBefore(inicio)) {
            val e = estado(dia)
            if (quebra(e)) break
            if (e == EstadoDia.CONCLUIDO) atual++
            dia = dia.minusDays(1)
        }

        // RECORDE: anda do início até hoje guardando a maior sequência.
        var recorde = 0
        var corrida = 0
        dia = inicio
        while (!dia.isAfter(hoje)) {
            val e = estado(dia)
            when {
                e == EstadoDia.CONCLUIDO -> { corrida++; recorde = maxOf(recorde, corrida) }
                quebra(e) -> corrida = 0
            }
            dia = dia.plusDays(1)
        }

        val total = valorPorDia.count { (data, valor) -> valor >= habito.alvoDiario && !data.isAfter(hoje) }
        // `(data, valor) ->` desestrutura a entrada do Map (≈ deconstruction de tupla do C#).

        return Estatisticas(sequenciaAtual = atual, recorde = maxOf(recorde, atual), totalConcluidos = total)
    }
}
