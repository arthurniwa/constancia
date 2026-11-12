package com.example.constancia.ui.habitos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Habito
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.ui.util.OPCOES_COR
import com.example.constancia.ui.util.OPCOES_ICONE
import com.example.constancia.ui.util.SUGESTOES
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** Estado do formulário (meta como texto, igual ao campo). */
data class HabitoFormUiState(
    val id: Long = 0,
    val usuarioId: Long = 0,
    val nome: String = "",
    val icone: String = OPCOES_ICONE.first(),
    val cor: Int = OPCOES_COR.first(),
    val dias: Set<DayOfWeek> = DayOfWeek.entries.toSet(),   // padrão: todos os dias
    val metaTexto: String = "",
    val horarioLembrete: LocalTime? = null,
    val dataCriacao: LocalDate = LocalDate.now(),
    val erroNome: String? = null,
    val erroDias: String? = null,
    val erroMeta: String? = null,
    val carregando: Boolean = false
) {
    val ehEdicao: Boolean get() = id != 0L
}

/**
 * Mesmo form para criar (id = 0) e editar (id > 0).
 * SavedStateHandle traz os argumentos da rota (id e sugestao).
 */
class HabitoFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitoRepository,
    private val usuarioId: Long   // dono dos hábitos novos
) : ViewModel() {

    private val habitoId: Long = savedStateHandle["id"] ?: 0L
    private val sugestao: Int = savedStateHandle["sugestao"] ?: -1

    /**
     * mutableStateOf: quando muda, a tela redesenha. Só o ViewModel altera (private set).
     * Fica no ViewModel, então o que foi digitado sobrevive à rotação.
     */
    var uiState by mutableStateOf(HabitoFormUiState(usuarioId = usuarioId))
        private set

    // Roda uma vez (não repete ao girar a tela).
    init {
        // Veio de uma sugestão da tela Hoje? Abre o form preenchido.
        SUGESTOES.getOrNull(sugestao)?.let { s ->
            if (habitoId == 0L) {
                uiState = uiState.copy(nome = s.nome, icone = s.icone, cor = s.cor, dias = s.dias, metaTexto = s.meta?.toString() ?: "")
            }
        }
        if (habitoId != 0L) {
            uiState = uiState.copy(carregando = true)
            viewModelScope.launch {
                val habito = repository.buscarPorId(habitoId)
                uiState = habito?.paraFormState() ?: HabitoFormUiState(usuarioId = usuarioId)
            }
        }
    }

    // Eventos da tela. copy() = cópia mudando só alguns campos.
    fun alterarNome(valor: String) { uiState = uiState.copy(nome = valor, erroNome = null) }
    fun alterarIcone(valor: String) { uiState = uiState.copy(icone = valor) }
    fun alterarCor(valor: Int) { uiState = uiState.copy(cor = valor) }
    fun alterarMeta(valor: String) {
        // Só dígitos, no máximo 4.
        if (valor.length <= 4 && valor.all { it.isDigit() }) {
            uiState = uiState.copy(metaTexto = valor, erroMeta = null)
        }
    }
    fun alternarDia(dia: DayOfWeek) {
        val novos = if (dia in uiState.dias) uiState.dias - dia else uiState.dias + dia
        uiState = uiState.copy(dias = novos, erroDias = null)
    }

    /** Validação fica no ViewModel. Retorna true se está tudo certo. */
    private fun validar(): Boolean {
        val nome = uiState.nome.trim()
        val meta = uiState.metaTexto.toIntOrNull()   // null se não for número

        val erroNome = when {
            nome.isEmpty() -> "Informe um nome"
            nome.length > 40 -> "Máximo de 40 caracteres"
            else -> null
        }
        val erroDias = if (uiState.dias.isEmpty()) "Escolha pelo menos um dia" else null
        val erroMeta = if (uiState.metaTexto.isNotEmpty() && (meta == null || meta <= 0))
            "A meta deve ser maior que zero" else null

        uiState = uiState.copy(erroNome = erroNome, erroDias = erroDias, erroMeta = erroMeta)
        return erroNome == null && erroDias == null && erroMeta == null
    }

    /** CREATE ou UPDATE. `suspend`: a tela espera salvar antes de voltar. */
    suspend fun salvar(): Boolean {
        if (!validar()) return false
        val habito = uiState.paraHabito()
        if (uiState.ehEdicao) repository.atualizar(habito) else repository.inserir(habito)
        return true
    }
}

// Converte entre entidade e estado da tela.
private fun Habito.paraFormState() = HabitoFormUiState(
    id = id,
    usuarioId = usuarioId,
    nome = nome,
    icone = icone,
    cor = cor,
    dias = diasDaSemana,
    metaTexto = meta?.toString() ?: "",
    horarioLembrete = horarioLembrete,
    dataCriacao = dataCriacao
)

private fun HabitoFormUiState.paraHabito() = Habito(
    id = id,
    usuarioId = usuarioId,
    nome = nome.trim(),
    icone = icone,
    cor = cor,
    diasDaSemana = dias,
    meta = metaTexto.toIntOrNull(),
    horarioLembrete = horarioLembrete,
    dataCriacao = dataCriacao
)
