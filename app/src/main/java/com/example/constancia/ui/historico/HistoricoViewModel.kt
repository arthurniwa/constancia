package com.example.constancia.ui.historico

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.RegistroDiario
import com.example.constancia.data.repository.FeriadoRepository
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.data.repository.RegistroRepository
import com.example.constancia.data.repository.ResultadoFeriados
import com.example.constancia.ui.util.formatoCurto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Linha do histórico: registro OU feriado sem registro (sealed = só esses dois tipos). */
sealed interface ItemHistorico {
    val data: LocalDate
    data class Registro(val registro: RegistroDiario, val nomeFeriado: String?) : ItemHistorico {
        override val data: LocalDate get() = registro.data
    }
    data class Feriado(override val data: LocalDate, val nome: String) : ItemHistorico
}

data class HistoricoUiState(
    val carregando: Boolean = true,
    val habito: Habito? = null,
    val itens: List<ItemHistorico> = emptyList(),
    val carregandoFeriados: Boolean = true,
    val erroFeriados: String? = null
)

/** Cópia do registro em edição; só grava ao salvar. */
data class EdicaoRegistro(val original: RegistroDiario, val data: LocalDate, val valor: Int)

/** Para que o calendário foi aberto. */
enum class ModoCalendario { FECHADO, ADICIONAR, EDITAR }

class HistoricoViewModel(
    savedStateHandle: SavedStateHandle,
    habitoRepository: HabitoRepository,
    private val registroRepository: RegistroRepository,
    private val feriadoRepository: FeriadoRepository
) : ViewModel() {

    private val habitoId: Long = checkNotNull(savedStateHandle["habitoId"])

    private val hoje = LocalDate.now()
    private val feriados = MutableStateFlow<ResultadoFeriados?>(null)   // null = carregando

    init { carregarFeriados() }

    fun carregarFeriados() {
        viewModelScope.launch {
            feriados.value = null
            feriados.value = feriadoRepository.buscar(setOf(hoje.year - 1, hoje.year))
        }
    }

    /** READ: combine recalcula quando hábito, registros ou feriados mudam. */
    val uiState: StateFlow<HistoricoUiState> = combine(
        habitoRepository.observarPorId(habitoId),
        registroRepository.observarPorHabito(habitoId),
        feriados
    ) { habito, registros, resultado ->
        val mapa = (resultado as? ResultadoFeriados.Sucesso)?.feriados.orEmpty()
        HistoricoUiState(
            carregando = false,
            habito = habito,
            itens = if (habito == null) emptyList() else montarItens(habito, registros, mapa),
            carregandoFeriados = resultado == null,
            erroFeriados = (resultado as? ResultadoFeriados.Erro)?.mensagem
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoricoUiState())

    /** Junta registros e feriados numa lista, do mais recente ao mais antigo. */
    private fun montarItens(
        habito: Habito,
        registros: List<RegistroDiario>,
        feriados: Map<LocalDate, String>
    ): List<ItemHistorico> {
        val inicio = minOf(habito.dataCriacao, registros.minOfOrNull { it.data } ?: habito.dataCriacao)
        val diasComRegistro = registros.map { it.data }.toSet()
        val doRegistro = registros.map { ItemHistorico.Registro(it, feriados[it.data]) }
        val soFeriado = feriados
            .filter { (data, _) -> !data.isBefore(inicio) && !data.isAfter(hoje) && data !in diasComRegistro }
            .map { (data, nome) -> ItemHistorico.Feriado(data, nome) }
        return (doRegistro + soFeriado).sortedByDescending { it.data }
    }

    // Estado dos diálogos no ViewModel: sobrevive à rotação.
    private val _edicao = MutableStateFlow<EdicaoRegistro?>(null)
    val edicao = _edicao.asStateFlow()

    private val _calendario = MutableStateFlow(ModoCalendario.FECHADO)
    val calendario = _calendario.asStateFlow()

    private val _paraExcluir = MutableStateFlow<RegistroDiario?>(null)
    val paraExcluir = _paraExcluir.asStateFlow()

    // Mensagem do Snackbar; null = nada a mostrar.
    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()
    fun mensagemExibida() { _mensagem.value = null }

    // ---------- CREATE (dia anterior esquecido) ----------
    fun abrirCalendarioParaAdicionar() { _calendario.value = ModoCalendario.ADICIONAR }
    fun fecharCalendario() { _calendario.value = ModoCalendario.FECHADO }

    fun dataEscolhida(data: LocalDate) {
        when (_calendario.value) {
            ModoCalendario.ADICIONAR -> adicionar(data)
            ModoCalendario.EDITAR -> _edicao.value = _edicao.value?.copy(data = data)
            ModoCalendario.FECHADO -> Unit
        }
        _calendario.value = ModoCalendario.FECHADO
    }

    private fun adicionar(data: LocalDate) {
        val habito = uiState.value.habito ?: return
        viewModelScope.launch {
            if (registroRepository.buscar(habitoId, data) != null) {
                _mensagem.value = "Já existe registro em ${data.formatoCurto()}. Edite o existente."
            } else {
                // Já entra como concluído.
                registroRepository.inserir(RegistroDiario(habitoId = habitoId, data = data, valor = habito.alvoDiario))
                _mensagem.value = "Registro adicionado"
            }
        }
    }

    // ---------- UPDATE ----------
    fun editar(registro: RegistroDiario) { _edicao.value = EdicaoRegistro(registro, registro.data, registro.valor) }
    fun cancelarEdicao() { _edicao.value = null }
    fun abrirCalendarioParaEditar() { _calendario.value = ModoCalendario.EDITAR }
    fun alterarValorEdicao(delta: Int) {
        _edicao.value = _edicao.value?.let { it.copy(valor = (it.valor + delta).coerceIn(1, 9999)) }
    }

    fun salvarEdicao() {
        val e = _edicao.value ?: return
        viewModelScope.launch {
            // Bloqueia data que já tem registro (índice único hábito+data).
            val conflito = e.data != e.original.data && registroRepository.buscar(habitoId, e.data) != null
            if (conflito) {
                _mensagem.value = "Já existe registro em ${e.data.formatoCurto()}"
                return@launch
            }
            registroRepository.atualizar(e.original.copy(data = e.data, valor = e.valor))
            _edicao.value = null
            _mensagem.value = "Registro atualizado"
        }
    }

    // ---------- DELETE ----------
    fun pedirExclusao(registro: RegistroDiario) { _paraExcluir.value = registro }
    fun cancelarExclusao() { _paraExcluir.value = null }
    fun confirmarExclusao() {
        val r = _paraExcluir.value ?: return
        _paraExcluir.value = null
        viewModelScope.launch {
            registroRepository.excluir(r)
            _mensagem.value = "Registro excluído"
        }
    }
}
