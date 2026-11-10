package com.example.constancia.ui.habitos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Habito
import com.example.constancia.data.repository.HabitoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tudo que a tela de lista precisa para se desenhar. */
data class HabitosUiState(
    val carregando: Boolean = true,
    val habitos: List<Habito> = emptyList()
)

/**
 * ViewModel: guarda o estado da tela e sobrevive à rotação
 * (a Activity é destruída e recriada ao girar; o ViewModel não).
 * ≈ o @code de um componente Blazor, mas separado da UI e com vida mais longa.
 */
class HabitosViewModel(private val repository: HabitoRepository) : ViewModel() {

    /**
     * READ do CRUD: transforma o Flow do Room em StateFlow (um Flow que sempre tem um "valor atual",
     * ≈ um BehaviorSubject do Rx). Toda vez que a tabela mudar, a lista na tela muda sozinha.
     * - viewModelScope: coroutines que são canceladas quando o ViewModel morre.
     * - WhileSubscribed(5000): para de escutar o banco 5s depois que a tela some
     *   (os 5s evitam recomeçar a consulta durante uma rotação).
     */
    val uiState: StateFlow<HabitosUiState> = repository.observarTodos()
        .map { lista -> HabitosUiState(carregando = false, habitos = lista) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitosUiState(carregando = true)
        )

    // Hábito aguardando confirmação de exclusão (o diálogo fica aberto mesmo se girar a tela).
    // Padrão "backing property": _privado mutável, público só leitura.
    private val _habitoParaExcluir = MutableStateFlow<Habito?>(null)
    val habitoParaExcluir: StateFlow<Habito?> = _habitoParaExcluir.asStateFlow()

    fun pedirExclusao(habito: Habito) { _habitoParaExcluir.value = habito }

    fun cancelarExclusao() { _habitoParaExcluir.value = null }

    /** DELETE do CRUD. */
    fun confirmarExclusao() {
        val habito = _habitoParaExcluir.value ?: return   // nada para excluir → sai
        _habitoParaExcluir.value = null
        // launch ≈ disparar uma Task (fire-and-forget seguro, preso ao ciclo de vida do ViewModel).
        viewModelScope.launch { repository.excluir(habito) }
    }
}
