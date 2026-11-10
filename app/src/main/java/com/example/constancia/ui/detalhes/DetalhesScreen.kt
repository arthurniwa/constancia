package com.example.constancia.ui.detalhes

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.habitos.IconeHabito
import com.example.constancia.ui.habitos.MensagemVazia
import com.example.constancia.ui.util.CarregandoFeriados
import com.example.constancia.ui.util.ErroFeriados
import com.example.constancia.ui.util.formatoCurto
import com.example.constancia.ui.util.resumo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesScreen(
    onVoltar: () -> Unit,
    onEditar: (Long) -> Unit,
    onHistorico: (Long) -> Unit,
    viewModel: DetalhesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()
    val confirmandoExclusao by viewModel.confirmandoExclusao.collectAsState()
    val scope = rememberCoroutineScope()
    // Context = "acesso ao sistema Android" (abrir outras telas/apps, recursos...).
    val context = LocalContext.current
    val habito = estado.habito

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habito?.nome ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                actions = {   // ícones no canto direito da barra
                    if (habito != null) {
                        IconButton(onClick = { onEditar(habito.id) }) { Icon(Icons.Default.Edit, "Editar hábito") }
                        IconButton(onClick = viewModel::pedirExclusao) { Icon(Icons.Default.Delete, "Excluir hábito") }
                    }
                }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding).fillMaxSize()
        when {
            estado.carregando -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            habito == null -> MensagemVazia("Hábito não encontrado.", modifier)
            else -> Column(
                modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconeHabito(habito.icone, habito.cor, Modifier.size(56.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(habito.diasDaSemana.resumo(), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            habito.meta?.let { "Meta: $it por dia" } ?: "Hábito do tipo feito/não feito",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val e = estado.estatisticas
                CartaoNumero("🔥", "Sequência atual", e.sequenciaAtual)
                CartaoNumero("🏆", "Recorde", e.recorde)
                CartaoNumero("✅", "Total de dias concluídos", e.totalConcluidos)

                Text(
                    "Dias não programados e feriados nacionais não quebram a sequência.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ---------- Dados da BrasilAPI (requisito 4): os 3 estados possíveis ----------
                when {
                    estado.carregandoFeriados -> CarregandoFeriados()
                    estado.erroFeriados != null -> ErroFeriados(estado.erroFeriados!!, viewModel::carregarFeriados)
                    // `!!` = "garanto que não é null" (lança exceção se for). Aqui é seguro: checamos na linha acima.
                    else -> estado.proximoFeriado?.let { (data, nome) ->   // desestrutura o Pair
                        CartaoTexto("🎉", "Próximo feriado (descanso automático)", "$nome · ${data.formatoCurto()}")
                    }
                }

                // ---------- Compartilhamento com outro app (requisito 5) ----------
                Button(
                    onClick = {
                        // Intent = "mensagem" pedindo ao Android que alguém execute uma ação.
                        // ACTION_SEND = "enviar dados"; o Android lista os apps capazes (WhatsApp, Gmail...).
                        val enviar = Intent(Intent.ACTION_SEND).apply {   // `apply` configura o objeto e o devolve
                            type = "text/plain"                           // tipo do conteúdo (MIME)
                            putExtra(Intent.EXTRA_TEXT, textoCompartilhamento(habito, e))
                        }
                        // createChooser força a janela "Compartilhar com..." (sempre deixa o usuário escolher).
                        context.startActivity(Intent.createChooser(enviar, "Compartilhar progresso"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Compartilhar progresso")
                }

                OutlinedButton(onClick = { onHistorico(habito.id) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver histórico")
                }
            }
        }
    }

    if (confirmandoExclusao && habito != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,
            title = { Text("Excluir hábito?") },
            text = { Text("\"${habito.nome}\" e todo o histórico dele serão apagados.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.excluir()
                        onVoltar()   // só volta depois de excluir
                    }
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = viewModel::cancelarExclusao) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CartaoTexto(emoji: String, titulo: String, texto: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(texto, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun CartaoNumero(emoji: String, titulo: String, valor: Int) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (valor == 1) "1 dia" else "$valor dias",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
