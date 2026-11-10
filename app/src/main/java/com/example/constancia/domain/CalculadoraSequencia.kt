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

/**
 * Regra de negócio pura (sem Android, sem banco): recebe dados e devolve números.
 * Por isso dá para testar com um teste unitário simples (ver CalculadoraSequenciaTest).
 * `object` = singleton sem estado (≈ uma `static class` do C#).
 *
 * Como cada dia é classificado:
 *  1. Concluído (valor ≥ meta)                → soma +1 na sequência
 *  2. Não programado (ex.: sábado num hábito de seg–sex) → neutro: não soma, não quebra
 *  3. Feriado nacional (BrasilAPI)             → neutro: "dia de descanso automático"
 *  4. Hoje, ainda não feito                    → neutro: o dia ainda não acabou
 *  5. Qualquer outro dia não feito             → QUEBRA a sequência
 */
object CalculadoraSequencia {

    private enum class TipoDia { CONCLUIDO, NEUTRO, FALHA }

    fun calcular(
        habito: Habito,
        registros: List<RegistroDiario>,
        feriados: Set<LocalDate> = emptySet(),
        hoje: LocalDate = LocalDate.now()
    ): Estatisticas {
        // Map data → valor (≈ ToDictionary). Busca O(1) para cada dia.
        val valorPorDia: Map<LocalDate, Int> = registros.associate { it.data to it.valor }
        // `to` cria um par (Pair) — ≈ uma tupla (data, valor) do C#.

        fun tipo(dia: LocalDate): TipoDia {   // função local (≈ local function do C#)
            val valor = valorPorDia[dia] ?: 0
            return when {
                valor >= habito.alvoDiario -> TipoDia.CONCLUIDO
                !habito.programadoPara(dia) -> TipoDia.NEUTRO
                dia in feriados -> TipoDia.NEUTRO
                dia == hoje -> TipoDia.NEUTRO
                else -> TipoDia.FALHA
            }
        }

        // Começa na criação do hábito, ou antes se o usuário registrou dias anteriores.
        // minOrNull: menor data da lista, ou null se a lista estiver vazia.
        val inicio = minOf(habito.dataCriacao, registros.minOfOrNull { it.data } ?: habito.dataCriacao)
        if (inicio.isAfter(hoje)) return Estatisticas(0, 0, 0)

        // Sequência ATUAL: anda de hoje para trás até encontrar uma falha.
        var atual = 0
        var dia = hoje
        while (!dia.isBefore(inicio)) {
            when (tipo(dia)) {
                TipoDia.CONCLUIDO -> atual++
                TipoDia.NEUTRO -> Unit
                TipoDia.FALHA -> break
            }
            dia = dia.minusDays(1)
        }

        // RECORDE: anda do início até hoje guardando a maior sequência.
        var recorde = 0
        var corrida = 0
        dia = inicio
        while (!dia.isAfter(hoje)) {
            when (tipo(dia)) {
                TipoDia.CONCLUIDO -> { corrida++; recorde = maxOf(recorde, corrida) }
                TipoDia.NEUTRO -> Unit
                TipoDia.FALHA -> corrida = 0
            }
            dia = dia.plusDays(1)
        }

        val total = valorPorDia.count { (data, valor) -> valor >= habito.alvoDiario && !data.isAfter(hoje) }
        // `(data, valor) ->` desestrutura a entrada do Map (≈ deconstruction de tupla do C#).

        return Estatisticas(sequenciaAtual = atual, recorde = maxOf(recorde, atual), totalConcluidos = total)
    }
}
