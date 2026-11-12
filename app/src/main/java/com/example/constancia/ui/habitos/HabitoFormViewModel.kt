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

/** Estado do formulário. A meta fica como texto porque o campo da tela é texto. */
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
 * Serve para CRIAR (id = 0) e EDITAR (id > 0).
 * SavedStateHandle traz os argumentos da navegação (≈ os parâmetros de rota `{id}` do ASP.NET).
 */
class HabitoFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitoRepository,
    private val usuarioId: Long   // dono dos hábitos novos
) : ViewModel() {

    private val habitoId: Long = savedStateHandle["id"] ?: 0L
    private val sugestao: Int = savedStateHandle["sugestao"] ?: -1

    /**
     * `mutableStateOf` = estado observável do Compose: quando muda, a tela redesenha.
     * `by` delega o get/set (lê `uiState` em vez de `uiState.value`).
     * `private set`: a tela lê, mas só o ViewModel altera.
     * Como mora no ViewModel, o que o usuário digitou sobrevive à rotação.
     */
    var uiState by mutableStateOf(HabitoFormUiState(usuarioId = usuarioId))
        private set

    // `init` ≈ corpo do construtor. Roda uma vez só (não roda de novo ao girar a tela).
    init {
        // Veio de uma sugestão da tela Hoje? Já abre preenchido (o usuário ainda pode mudar tudo).
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

    // Eventos da tela. `copy(...)` cria uma cópia alterando só alguns campos (≈ `with { }` do record).
    fun alterarNome(valor: String) { uiState = uiState.copy(nome = valor, erroNome = null) }
    fun alterarIcone(valor: String) { uiState = uiState.copy(icone = valor) }
    fun alterarCor(valor: Int) { uiState = uiState.copy(cor = valor) }
    fun alterarMeta(valor: String) {
        // Aceita só dígitos (no máximo 4) — o filtro roda a cada tecla.
        if (valor.length <= 4 && valor.all { it.isDigit() }) {
            uiState = uiState.copy(metaTexto = valor, erroMeta = null)
        }
    }
    fun alternarDia(dia: DayOfWeek) {
        val novos = if (dia in uiState.dias) uiState.dias - dia else uiState.dias + dia
        uiState = uiState.copy(dias = novos, erroDias = null)
    }

    /** Valida os campos. Retorna true se está tudo certo. */
    private fun validar(): Boolean {
        val nome = uiState.nome.trim()
        val meta = uiState.metaTexto.toIntOrNull()   // toIntOrNull ≈ int.TryParse (null se falhar)

        val erroNome = when {                        // `when` ≈ switch expression do C#
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

    /**
     * CREATE ou UPDATE do CRUD.
     * `suspend` porque a tela espera terminar para voltar à lista.
     */
    suspend fun salvar(): Boolean {
        if (!validar()) return false
        val habito = uiState.paraHabito()
        if (uiState.ehEdicao) repository.atualizar(habito) else repository.inserir(habito)
        return true
    }
}

// Funções de extensão privadas para converter entre entidade e estado da tela (≈ um "mapper").
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
