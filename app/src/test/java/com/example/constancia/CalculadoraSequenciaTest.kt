package com.example.constancia

import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.RegistroDiario
import com.example.constancia.domain.CalculadoraSequencia
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Testes unitários (rodam no computador, sem emulador) — ≈ xUnit/NUnit.
 * Rodar: botão ▶ ao lado da classe, ou `./gradlew test`.
 * "Hoje" fixo em terça, 10/11/2026, para o resultado não depender do dia em que rodar.
 */
class CalculadoraSequenciaTest {

    private val hoje = LocalDate.of(2026, 11, 10)
    private fun dia(d: Int) = LocalDate.of(2026, 11, d)

    private val todosOsDias = Habito(
        id = 1, usuarioId = 1, nome = "Ler", icone = "📚", cor = 0,
        diasDaSemana = DayOfWeek.entries.toSet(),
        dataCriacao = dia(1)
    )

    // `vararg` ≈ `params int[]` do C#
    private fun feitos(vararg dias: Int, valor: Int = 1) =
        dias.map { RegistroDiario(habitoId = 1, data = dia(it), valor = valor) }

    @Test
    fun `tres dias seguidos ate hoje`() {   // nomes com crase permitem espaços (só em testes)
        val r = CalculadoraSequencia.calcular(todosOsDias, feitos(8, 9, 10), hoje = hoje)
        assertEquals(3, r.sequenciaAtual)
        assertEquals(3, r.recorde)
    }

    @Test
    fun `hoje ainda nao feito nao quebra a sequencia`() {
        val r = CalculadoraSequencia.calcular(todosOsDias, feitos(8, 9), hoje = hoje)
        assertEquals(2, r.sequenciaAtual)
    }

    @Test
    fun `dia esquecido quebra a sequencia mas o recorde fica`() {
        val r = CalculadoraSequencia.calcular(todosOsDias, feitos(5, 6, 7, 9, 10), hoje = hoje)
        assertEquals(2, r.sequenciaAtual)   // 9 e 10
        assertEquals(3, r.recorde)          // 5, 6, 7
    }

    @Test
    fun `feriado nao quebra a sequencia`() {
        val r = CalculadoraSequencia.calcular(
            todosOsDias, feitos(5, 6, 7, 9, 10), feriados = setOf(dia(8)), hoje = hoje
        )
        assertEquals(5, r.sequenciaAtual)
    }

    @Test
    fun `fim de semana nao conta para habito de segunda a sexta`() {
        val diasUteis = todosOsDias.copy(
            diasDaSemana = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            dataCriacao = dia(6)
        )
        // sex 6, (sáb 7 e dom 8 neutros), seg 9, ter 10
        val r = CalculadoraSequencia.calcular(diasUteis, feitos(6, 9, 10), hoje = hoje)
        assertEquals(3, r.sequenciaAtual)
    }

    @Test
    fun `meta nao atingida nao conta como feito`() {
        val agua = todosOsDias.copy(meta = 8)
        val r = CalculadoraSequencia.calcular(agua, feitos(9, valor = 3), hoje = hoje)
        assertEquals(0, r.sequenciaAtual)
        assertEquals(0, r.totalConcluidos)
    }
}
