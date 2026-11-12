package com.example.constancia.ui.detalhes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Habito
import com.example.constancia.data.repository.FeriadoRepository
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.data.repository.RegistroRepository
import com.example.constancia.data.repository.ResultadoFeriados
import com.example.constancia.domain.CalculadoraSequencia
import com.example.constancia.domain.DiaResumo
import com.example.constancia.domain.Estatisticas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DetalhesUiState(
    val carregando: Boolean = true,
    val habito: Habito? = null,
    val estatisticas: Estatisticas = Estatisticas(0, 0, 0),
    val carregandoFeriados: Boolean = true,
    val erroFeriados: String? = null,
    val proximoFeriado: Pair<LocalDate, String>? = null,   // Pair ≈ tupla (data, nome)
    val semana: List<DiaResumo> = emptyList(),   // últimos 7 dias (gráfico de barras)
    val mes: List<DiaResumo> = emptyList()       // todos os dias do mês atual (mapa)
)

class DetalhesViewModel(
    savedStateHandle: SavedStateHandle,
    private val habitoRepository: HabitoRepository,
    registroRepository: RegistroRepository,
    private val feriadoRepository: FeriadoRepository
) : ViewModel() {

    private val habitoId: Long = checkNotNull(savedStateHandle["habitoId"])
    private val hoje = LocalDate.now()

    // null = carregando; Sucesso/Erro = resposta da API.
    private val feriados = MutableStateFlow<ResultadoFeriados?>(null)

    init { carregarFeriados() }

    /** Busca na BrasilAPI o ano anterior, o atual e o próximo. Também usado pelo botão "Tentar novamente". */
    fun carregarFeriados() {
        viewModelScope.launch {
            feriados.value = null
            feriados.value = feriadoRepository.buscar(setOf(hoje.year - 1, hoje.year, hoje.year + 1))
        }
    }

    /** Hábito + registros + feriados → estatísticas recalculadas sempre que qualquer um mudar. */
    val uiState: StateFlow<DetalhesUiState> = combine(
        habitoRepository.observarPorId(habitoId),
        registroRepository.observarPorHabito(habitoId),
        feriados
    ) { habito, registros, resultado ->
        // `as?` = cast seguro (≈ `as` do C#): vira null se não for Sucesso.
        val mapa = (resultado as? ResultadoFeriados.Sucesso)?.feriados.orEmpty()
        DetalhesUiState(
            carregando = false,
            habito = habito,
            estatisticas = habito?.let { CalculadoraSequencia.calcular(it, registros, mapa.keys, hoje) }
                ?: Estatisticas(0, 0, 0),
            carregandoFeriados = resultado == null,
            erroFeriados = (resultado as? ResultadoFeriados.Erro)?.mensagem,
            proximoFeriado = mapa.filterKeys { it.isAfter(hoje) }.minByOrNull { it.key }?.toPair(),
            // Mesma regra da sequência, dia a dia, para os gráficos.
            semana = habito?.let {
                CalculadoraSequencia.resumir(it, registros, hoje.minusDays(6), hoje, mapa.keys, hoje)
            }.orEmpty(),
            mes = habito?.let {
                CalculadoraSequencia.resumir(
                    it, registros, hoje.withDayOfMonth(1), hoje.withDayOfMonth(hoje.lengthOfMonth()), mapa.keys, hoje
                )
            }.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalhesUiState())

    private val _confirmandoExclusao = MutableStateFlow(false)
    val confirmandoExclusao = _confirmandoExclusao.asStateFlow()
    fun pedirExclusao() { _confirmandoExclusao.value = true }
    fun cancelarExclusao() { _confirmandoExclusao.value = false }

    /** DELETE do hábito (os registros vão junto pelo CASCADE). A tela volta depois que terminar. */
    suspend fun excluir() {
        _confirmandoExclusao.value = false
        uiState.value.habito?.let { habitoRepository.excluir(it) }
    }
}

/** Texto enviado para o WhatsApp/outro app. Fica fora da tela para ser fácil de testar e explicar. */
fun textoCompartilhamento(habito: Habito, e: Estatisticas): String {
    fun dias(n: Int) = if (n == 1) "1 dia" else "$n dias"
    return buildString {   // ≈ StringBuilder do C#
        appendLine("${habito.icone} Hábito: ${habito.nome}")
        appendLine("🔥 Sequência atual: ${dias(e.sequenciaAtual)}")
        appendLine("🏆 Recorde: ${dias(e.recorde)}")
        append("Acompanhando minha rotina com o app Constância!")
    }
}
