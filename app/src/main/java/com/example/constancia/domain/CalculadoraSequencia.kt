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

/** Situação de um dia. Mesma regra para a sequência e para os gráficos. */
enum class EstadoDia {
    CONCLUIDO,        // bateu a meta → soma
    PARCIAL,          // não bateu a meta → quebra
    FALHA,            // dia programado sem nada → quebra
    HOJE_PENDENTE,    // neutro (o dia não acabou)
    FERIADO,          // neutro
    NAO_PROGRAMADO,   // neutro (ex.: sábado num hábito seg–sex)
    FORA_DO_PERIODO   // neutro (antes do início ou futuro)
}

/** Um dia já classificado, pronto para desenhar (fracao = 0..1 preenchido). */
data class DiaResumo(val data: LocalDate, val valor: Int, val estado: EstadoDia, val fracao: Float)

/**
 * Regra de negócio pura (sem Android, sem banco), por isso tem teste unitário.
 * CONCLUIDO soma, PARCIAL/FALHA quebram, o resto é neutro.
 */
object CalculadoraSequencia {

    /** Primeiro dia que conta: criação do hábito ou o registro mais antigo. */
    fun inicio(habito: Habito, registros: List<RegistroDiario>): LocalDate =
        minOf(habito.dataCriacao, registros.minOfOrNull { it.data } ?: habito.dataCriacao)

    /** Classifica um dia. A ordem importa: concluído vale até em feriado/fim de semana. */
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

    /** Dias de [de] até [ate] classificados, para os gráficos. */
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
        val valorPorDia: Map<LocalDate, Int> = registros.associate { it.data to it.valor }

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

        return Estatisticas(sequenciaAtual = atual, recorde = maxOf(recorde, atual), totalConcluidos = total)
    }
}
