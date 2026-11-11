package com.example.constancia.ui.hoje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.RegistroDiario
import com.example.constancia.data.repository.FeriadoRepository
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.data.repository.RegistroRepository
import com.example.constancia.data.repository.ResultadoFeriados
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
    val feriadoHoje: String? = null   // nome do feriado, se hoje for feriado nacional
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

    // Se a API falhar aqui, simplesmente não mostramos o aviso (o erro aparece em Detalhes/Histórico).
    private val feriadoHoje = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val r = feriadoRepository.buscar(setOf(hoje.year))
            if (r is ResultadoFeriados.Sucesso) feriadoHoje.value = r.feriados[hoje]   // `is` faz smart cast
        }
    }

    /**
     * `combine` junta dois Flows: sempre que QUALQUER um emitir (mudou um hábito OU um registro),
     * recalcula a lista. ≈ CombineLatest do Rx.
     */
    val uiState: StateFlow<HojeUiState> = combine(
        habitoRepository.observarDoUsuario(usuarioId),
        registroRepository.observarPorData(hoje),
        feriadoHoje
    ) { habitos, registros, feriado ->
        // associateBy cria um Map habitoId → registro (≈ ToDictionary do LINQ) para busca rápida.
        val registroPorHabito = registros.associateBy { it.habitoId }
        HojeUiState(
            carregando = false,
            data = hoje,
            existeAlgumHabito = habitos.isNotEmpty(),
            itens = habitos
                .filter { it.programadoPara(hoje) }                       // ≈ .Where(...)
                .map { HabitoDoDia(it, registroPorHabito[it.id]) },       // ≈ .Select(...)
            feriadoHoje = feriado
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HojeUiState())

    /** Hábito sem meta: marca/desmarca (CREATE/DELETE de RegistroDiario). */
    fun alternar(item: HabitoDoDia) {
        viewModelScope.launch { registroRepository.alternarFeito(item.habito.id, hoje) }
    }

    /** Hábito com meta: +1 / -1 (CREATE/UPDATE/DELETE de RegistroDiario). */
    fun somar(item: HabitoDoDia, delta: Int) {
        viewModelScope.launch { registroRepository.ajustarValor(item.habito.id, hoje, delta) }
    }
}
