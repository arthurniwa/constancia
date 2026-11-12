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

/** Estado da tela de lista. */
data class HabitosUiState(
    val carregando: Boolean = true,
    val habitos: List<Habito> = emptyList()
)

/** ViewModel: guarda o estado da tela e sobrevive à rotação (a Activity não). */
class HabitosViewModel(
    private val repository: HabitoRepository,
    usuarioId: Long   // só os hábitos do usuário logado
) : ViewModel() {

    /**
     * READ: lista do banco como StateFlow; a tela atualiza sozinha quando a tabela muda.
     * WhileSubscribed(5000): para de escutar 5s após a tela sumir (sobrevive à rotação).
     */
    val uiState: StateFlow<HabitosUiState> = repository.observarDoUsuario(usuarioId)
        .map { lista -> HabitosUiState(carregando = false, habitos = lista) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitosUiState(carregando = true)
        )

    // Hábito aguardando confirmação de exclusão. _privado mutável, público só leitura.
    private val _habitoParaExcluir = MutableStateFlow<Habito?>(null)
    val habitoParaExcluir: StateFlow<Habito?> = _habitoParaExcluir.asStateFlow()

    fun pedirExclusao(habito: Habito) { _habitoParaExcluir.value = habito }

    fun cancelarExclusao() { _habitoParaExcluir.value = null }

    /** DELETE do CRUD. */
    fun confirmarExclusao() {
        val habito = _habitoParaExcluir.value ?: return
        _habitoParaExcluir.value = null
        // Roda em segundo plano; cancela se o ViewModel morrer.
        viewModelScope.launch { repository.excluir(habito) }
    }
}
