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
 * Tela "Hábitos" (lista). `@Composable` ≈ um componente .razor: uma função que descreve a UI
 * a partir do estado. Quando o estado muda, o Compose chama a função de novo ("recomposição").
 *
 * Os parâmetros `onNovo`/`onAbrir` são lambdas (≈ EventCallback do Blazor): a tela não sabe
 * navegar, ela só avisa "o usuário quer criar/abrir" e quem chamou decide o que fazer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitosScreen(
    onNovo: () -> Unit,
    onAbrir: (Long) -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: HabitosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // collectAsState: transforma o StateFlow em estado do Compose (a tela redesenha quando muda).
    val uiState by viewModel.uiState.collectAsState()
    val habitoParaExcluir by viewModel.habitoParaExcluir.collectAsState()

    Scaffold(   // Scaffold = "esqueleto" Material: barra de topo, FAB, conteúdo — sem sobreposição.
        topBar = { TopAppBar(title = { Text("Meus hábitos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovo) {
                Icon(Icons.Default.Add, contentDescription = "Novo hábito")
            }
        }
    ) { innerPadding ->   // innerPadding: espaço ocupado pelas barras, para o conteúdo não ficar embaixo delas
        val modifier = Modifier.padding(innerPadding).fillMaxSize()
        when {
            uiState.carregando -> Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.habitos.isEmpty() -> MensagemVazia(
                texto = "Você ainda não tem hábitos.\nToque em + para criar o primeiro.",
                modifier = modifier
            )
            // LazyColumn ≈ lista virtualizada (<Virtualize> do Blazor): só desenha o que está visível.
            else -> LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp), // 88dp: espaço para o FAB não cobrir o último item
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // `key` ajuda o Compose a saber qual item é qual quando a lista muda.
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

    // Diálogo de confirmação: aparece sempre que houver um hábito marcado para exclusão.
    // `?.let` ≈ "se não for null, faça".
    habitoParaExcluir?.let { habito ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,   // `::` = referência a função (≈ method group do C#)
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
            // weight(1f) = ocupa o espaço que sobrar (≈ flex: 1 no CSS), empurrando os botões para a direita.
            Column(Modifier.weight(1f)) {
                Text(
                    habito.nome,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis   // nome longo vira "..." em vez de sobrepor os botões
                )
                val detalhe = habito.diasDaSemana.resumo() +
                    (habito.meta?.let { " · meta $it/dia" } ?: "")
                Text(detalhe, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onExcluir) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
        }
    }
}

/** Círculo colorido com o emoji do hábito. Reaproveitado em outras telas. */
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

/** Mensagem centralizada para listas vazias. Reaproveitada em outras telas. */
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
