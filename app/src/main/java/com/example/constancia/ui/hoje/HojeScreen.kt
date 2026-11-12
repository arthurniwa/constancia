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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.componentes.AnelProgresso
import com.example.constancia.ui.componentes.TituloSecao
import com.example.constancia.ui.habitos.MensagemVazia
import com.example.constancia.ui.theme.EstiloNumero
import com.example.constancia.ui.theme.EstiloRotulo
import com.example.constancia.ui.theme.Verde
import com.example.constancia.ui.util.formatoLongo

/**
 * Tela "Hoje" no estilo painel esportivo:
 * um medidor grande com o progresso do dia e, abaixo, um cartão por hábito com anel de progresso.
 */
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
            TopAppBar(
                title = {
                    Column {
                        // substringBefore(" "): só o primeiro nome ("Arthur Niwa" → "Arthur").
                        Text("Olá, ${nomeUsuario.substringBefore(" ")}!", style = MaterialTheme.typography.titleLarge)
                        Text(
                            estado.data.formatoLongo(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
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
                contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                estado.feriadoHoje?.let { nome ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Text(
                                "🎉 Hoje é feriado ($nome). Se descansar, sua sequência não quebra.",
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                item { ResumoDoDia(concluidos = estado.concluidos, total = estado.itens.size) }
                item { TituloSecao("Hábitos de hoje", Modifier.padding(top = 8.dp, start = 4.dp)) }
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

/** Cartão de destaque: medidor de 270° com "3/5" no centro e uma frase de status. */
@Composable
private fun ResumoDoDia(concluidos: Int, total: Int) {
    val completo = concluidos == total
    val cor = if (completo) Verde else MaterialTheme.colorScheme.primary
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            AnelProgresso(
                progresso = if (total == 0) 0f else concluidos.toFloat() / total,
                cor = cor,
                espessura = 12.dp,
                arco = 270f,
                modifier = Modifier.size(128.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$concluidos/$total", style = EstiloNumero.gigante)
                    Text("hábitos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PROGRESSO DO DIA", style = EstiloRotulo, color = cor)
                Text(
                    when {
                        completo -> "Dia completo! 🎉"
                        total - concluidos == 1 -> "Falta 1 hábito para fechar o dia."
                        else -> "Faltam ${total - concluidos} hábitos para fechar o dia."
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${(concluidos * 100) / maxOf(total, 1)}% concluído",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    val cor = Color(habito.cor)

    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Anel na cor do hábito, com o emoji no centro: enche conforme o valor do dia.
            AnelProgresso(
                progresso = item.valor.toFloat() / habito.alvoDiario,
                cor = cor,
                espessura = 5.dp,
                modifier = Modifier.size(54.dp)
            ) {
                Text(habito.icone, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(habito.nome, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val (texto, corTexto) = when {
                    item.concluido -> "Concluído" to Verde
                    habito.meta != null -> "${item.valor} de ${habito.meta}" to MaterialTheme.colorScheme.onSurfaceVariant
                    else -> "Pendente" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(texto, style = MaterialTheme.typography.bodySmall, color = corTexto)
            }
            if (habito.meta == null) {
                // Botão redondo que "acende" na cor do hábito quando marcado.
                FilledIconToggleButton(
                    checked = item.concluido,
                    onCheckedChange = { onAlternar() },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = cor,
                        checkedContentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = if (item.concluido) "Desmarcar" else "Marcar como feito")
                }
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
