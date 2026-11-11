package com.example.constancia.ui.hoje

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.habitos.IconeHabito
import com.example.constancia.ui.habitos.MensagemVazia
import com.example.constancia.ui.util.formatoLongo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojeScreen(
    nomeUsuario: String,
    onAbrir: (Long) -> Unit,
    viewModel: HojeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    // substringBefore(" "): só o primeiro nome ("Arthur Niwa" → "Arthur").
                    Text("Olá, ${nomeUsuario.substringBefore(" ")}!")
                    Text(estado.data.formatoLongo(), style = MaterialTheme.typography.bodyMedium)
                }
            })
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding).fillMaxSize()
        when {
            estado.carregando -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            !estado.existeAlgumHabito -> MensagemVazia(
                "Nenhum hábito cadastrado.\nCrie o primeiro na aba Hábitos.", modifier
            )
            estado.itens.isEmpty() -> MensagemVazia("Nenhum hábito programado para hoje. Aproveite o descanso! 🌿", modifier)
            else -> LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // `item { }` = um elemento avulso no topo da lista; `items(...)` = a lista em si.
                estado.feriadoHoje?.let { nome ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Text(
                                "🎉 Hoje é feriado ($nome). Se descansar, sua sequência não quebra.",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                item {
                    Text(
                        "${estado.concluidos} de ${estado.itens.size} concluídos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(estado.itens, key = { it.habito.id }) { item ->
                    ItemHoje(
                        item = item,
                        onClick = { onAbrir(item.habito.id) },
                        onAlternar = { viewModel.alternar(item) },
                        onSomar = { delta -> viewModel.somar(item, delta) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemHoje(
    item: HabitoDoDia,
    onClick: () -> Unit,
    onAlternar: () -> Unit,
    onSomar: (Int) -> Unit
) {
    val habito = item.habito
    // Cartão fica levemente colorido quando o hábito está concluído.
    val cores = if (item.concluido)
        CardDefaults.cardColors(containerColor = Color(habito.cor).copy(alpha = 0.18f))
    else CardDefaults.cardColors()

    Card(colors = cores, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconeHabito(habito.icone, habito.cor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    habito.nome,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.concluido && habito.meta == null) TextDecoration.LineThrough else null
                )
                if (habito.meta != null) {
                    Text("${item.valor} de ${habito.meta}", style = MaterialTheme.typography.bodySmall)
                    // progress é uma lambda: o Compose só relê o valor na hora de desenhar.
                    LinearProgressIndicator(
                        progress = { (item.valor.toFloat() / habito.alvoDiario).coerceAtMost(1f) },
                        color = Color(habito.cor),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, end = 8.dp)
                    )
                }
            }
            if (habito.meta == null) {
                Checkbox(checked = item.concluido, onCheckedChange = { onAlternar() })
            } else {
                // O conjunto de ícones padrão não tem "menos", então usamos texto.
                FilledTonalIconButton(onClick = { onSomar(-1) }, enabled = item.valor > 0) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }
                FilledTonalIconButton(onClick = { onSomar(+1) }) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
