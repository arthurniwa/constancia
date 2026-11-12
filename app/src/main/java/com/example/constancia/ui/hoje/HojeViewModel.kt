package com.example.constancia.ui.hoje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.RegistroDiario
import com.example.constancia.data.repository.FeriadoRepository
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.data.repository.RegistroRepository
import com.example.constancia.data.repository.ResultadoFeriados
import com.example.constancia.domain.CalculadoraSequencia
import com.example.constancia.domain.DiaResumo
import com.example.constancia.domain.EstadoDia
import com.example.constancia.ui.util.DICAS
import com.example.constancia.ui.util.Dica
import com.example.constancia.ui.util.SUGESTOES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Um hábito + o registro dele no dia (null = ainda não marcado). */
data class HabitoDoDia(val habito: Habito, val registro: RegistroDiario?) {
    val valor: Int get() = registro?.valor ?: 0
    val concluido: Boolean get() = valor >= habito.alvoDiario
}

data class HojeUiState(
    val carregando: Boolean = true,
    val data: LocalDate = LocalDate.now(),
    val existeAlgumHabito: Boolean = false,   // diferencia "não criou nada" de "nada para hoje"
    val itens: List<HabitoDoDia> = emptyList(),
    val feriadoHoje: String? = null,          // nome do feriado, se hoje for feriado nacional
    val semana: List<DiaResumo> = emptyList(),// % concluído em cada um dos últimos 7 dias
    val dica: Dica = DICAS.first(),
    val sugestoes: List<Int> = emptyList()    // índices em SUGESTOES ainda não criados
) {
    val concluidos: Int get() = itens.count { it.concluido }
}

class HojeViewModel(
    private val habitoRepository: HabitoRepository,
    private val registroRepository: RegistroRepository,
    feriadoRepository: FeriadoRepository,
    usuarioId: Long
) : ViewModel() {

    private val hoje: LocalDate = LocalDate.now()
    private val inicioSemana: LocalDate = hoje.minusDays(6)

    // Se a API falhar, só não mostramos o aviso de feriado.
    private val feriadoHoje = MutableStateFlow<String?>(null)

    // Dica escolhida pelo dia do ano; fica no ViewModel para sobreviver à rotação.
    private val indiceDica = MutableStateFlow(hoje.dayOfYear % DICAS.size)

    init {
        viewModelScope.launch {
            val r = feriadoRepository.buscar(setOf(hoje.year))
            if (r is ResultadoFeriados.Sucesso) feriadoHoje.value = r.feriados[hoje]
        }
    }

    /** combine: recalcula a tela quando qualquer Flow muda (≈ CombineLatest). */
    val uiState: StateFlow<HojeUiState> = combine(
        habitoRepository.observarDoUsuario(usuarioId),
        registroRepository.observarDoUsuarioEntre(usuarioId, inicioSemana, hoje),   // consulta INNER JOIN
        feriadoHoje,
        indiceDica
    ) { habitos, registrosSemana, feriado, dica ->
        // Map habitoId → registro de hoje.
        val registroDeHojePorHabito = registrosSemana.filter { it.data == hoje }.associateBy { it.habitoId }
        val nomesCriados = habitos.map { it.nome.lowercase() }.toSet()
        HojeUiState(
            carregando = false,
            data = hoje,
            existeAlgumHabito = habitos.isNotEmpty(),
            itens = habitos
                .filter { it.programadoPara(hoje) }
                .map { HabitoDoDia(it, registroDeHojePorHabito[it.id]) },
            feriadoHoje = feriado,
            semana = resumoDaSemana(habitos, registrosSemana),
            dica = DICAS[dica],
            // Esconde sugestões que o usuário já criou (mesmo nome).
            sugestoes = SUGESTOES.indices.filter { SUGESTOES[it].nome.lowercase() !in nomesCriados }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HojeUiState())

    /** % concluído nos últimos 7 dias, com a mesma regra da sequência (estadoDoDia). */
    private fun resumoDaSemana(habitos: List<Habito>, registros: List<RegistroDiario>): List<DiaResumo> {
        val porHabito = registros.groupBy { it.habitoId }
        return generateSequence(inicioSemana) { it.plusDays(1) }.takeWhile { !it.isAfter(hoje) }.map { dia ->
            val estados = habitos.map { h ->
                val regs = porHabito[h.id].orEmpty()
                val valor = regs.firstOrNull { it.data == dia }?.valor ?: 0
                CalculadoraSequencia.estadoDoDia(h, valor, dia, emptySet(), hoje, CalculadoraSequencia.inicio(h, regs))
            }
            // Conta só os hábitos que valiam naquele dia.
            val contam = estados.filter {
                it == EstadoDia.CONCLUIDO || it == EstadoDia.FALHA || it == EstadoDia.PARCIAL || it == EstadoDia.HOJE_PENDENTE
            }
            val feitos = contam.count { it == EstadoDia.CONCLUIDO }
            DiaResumo(
                data = dia,
                valor = feitos,
                estado = when {
                    contam.isEmpty() -> EstadoDia.FORA_DO_PERIODO
                    feitos == contam.size -> EstadoDia.CONCLUIDO
                    else -> EstadoDia.PARCIAL
                },
                fracao = if (contam.isEmpty()) 0f else feitos.toFloat() / contam.size
            )
        }.toList()
    }

    fun proximaDica() { indiceDica.value = (indiceDica.value + 1) % DICAS.size }

    /** Hábito sem meta: marca/desmarca (CREATE/DELETE de RegistroDiario). */
    fun alternar(item: HabitoDoDia) {
        viewModelScope.launch { registroRepository.alternarFeito(item.habito.id, hoje) }
    }

    /** Hábito com meta: +1 / -1 (CREATE/UPDATE/DELETE de RegistroDiario). */
    fun somar(item: HabitoDoDia, delta: Int) {
        viewModelScope.launch { registroRepository.ajustarValor(item.habito.id, hoje, delta) }
    }
}
