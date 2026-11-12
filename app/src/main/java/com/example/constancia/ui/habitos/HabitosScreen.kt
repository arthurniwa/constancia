package com.example.constancia.ui.habitos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.data.local.Habito
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.util.resumo

/**
 * Lista de hábitos. @Composable = função que desenha a UI (como componente .razor);
 * redesenha quando o estado muda. Os `on...` só avisam; quem chama navega.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitosScreen(
    onSair: () -> Unit,
    onNovo: () -> Unit,
    onAbrir: (Long) -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: HabitosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // collectAsState: a tela redesenha quando o StateFlow muda.
    val uiState by viewModel.uiState.collectAsState()
    val habitoParaExcluir by viewModel.habitoParaExcluir.collectAsState()
    // rememberSaveable: diálogo de sair continua aberto após girar a tela.
    var confirmandoSaida by rememberSaveable { mutableStateOf(false) }

    Scaffold(   // Esqueleto da tela: barra de topo, FAB e conteúdo.
        topBar = {
            TopAppBar(
                title = { Text("Meus hábitos") },
                actions = {
                    IconButton(onClick = { confirmandoSaida = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair da conta")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovo) {
                Icon(Icons.Default.Add, contentDescription = "Novo hábito")
            }
        }
    ) { innerPadding ->   // innerPadding: conteúdo não fica embaixo das barras
        val modifier = Modifier.padding(innerPadding).fillMaxSize()
        when {
            uiState.carregando -> Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.habitos.isEmpty() -> MensagemVazia(
                texto = "Você ainda não tem hábitos.\nToque em + para criar o primeiro.",
                modifier = modifier
            )
            // LazyColumn: só desenha os itens visíveis.
            else -> LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp), // 88dp: FAB não cobre o último item
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.habitos, key = { it.id }) { habito ->
                    HabitoItem(
                        habito = habito,
                        onClick = { onAbrir(habito.id) },
                        onEditar = { onEditar(habito.id) },
                        onExcluir = { viewModel.pedirExclusao(habito) }
                    )
                }
            }
        }
    }

    if (confirmandoSaida) {
        AlertDialog(
            onDismissRequest = { confirmandoSaida = false },
            title = { Text("Sair da conta?") },
            text = { Text("Seus hábitos continuam salvos. Basta entrar de novo.") },
            confirmButton = { TextButton(onClick = { confirmandoSaida = false; onSair() }) { Text("Sair") } },
            dismissButton = { TextButton(onClick = { confirmandoSaida = false }) { Text("Cancelar") } }
        )
    }

    // Confirmação de exclusão: aparece se houver hábito marcado.
    habitoParaExcluir?.let { habito ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,
            title = { Text("Excluir hábito?") },
            text = { Text("\"${habito.nome}\" e todo o histórico dele serão apagados.") },
            confirmButton = { TextButton(onClick = viewModel::confirmarExclusao) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarExclusao) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun HabitoItem(
    habito: Habito,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconeHabito(habito.icone, habito.cor)
            Spacer(Modifier.width(12.dp))
            // weight(1f): ocupa o espaço que sobra.
            Column(Modifier.weight(1f)) {
                Text(
                    habito.nome,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis   // nome longo vira "..."
                )
                val detalhe = habito.diasDaSemana.resumo() +
                    (habito.meta?.let { " · ${habito.comUnidade(it)}/dia" } ?: "")
                Text(detalhe, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onExcluir) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
        }
    }
}

/** Círculo colorido com o emoji do hábito. */
@Composable
fun IconeHabito(icone: String, cor: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(Color(cor).copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(icone, style = MaterialTheme.typography.titleLarge)
    }
}

/** Mensagem para listas vazias. */
@Composable
fun MensagemVazia(texto: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            texto,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
