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
import com.example.constancia.ui.theme.EstiloNumero
import com.example.constancia.ui.theme.EstiloRotulo
import com.example.constancia.ui.theme.Verde
import com.example.constancia.ui.util.formatoLongo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import com.example.constancia.ui.componentes.GraficoSemana
import com.example.constancia.ui.theme.Ambar
import com.example.constancia.ui.util.Dica
import com.example.constancia.ui.util.SUGESTOES
import com.example.constancia.ui.util.Sugestao
import com.example.constancia.ui.util.resumo
import com.example.constancia.data.local.Habito

/** Tela "Hoje": medidor do dia, gráfico da semana, hábitos, dica e sugestões. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojeScreen(
    nomeUsuario: String,
    onAbrir: (Long) -> Unit,
    onCriarHabito: (sugestao: Int?) -> Unit,   // null = formulário vazio; número = sugestão pré-preenchida
    viewModel: HojeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // Só o primeiro nome.
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
            // O painel aparece sempre, mesmo sem hábitos.
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
                item {
                    ResumoDoDia(
                        concluidos = estado.concluidos,
                        total = estado.itens.size,
                        temHabitos = estado.existeAlgumHabito,
                        onCriar = { onCriarHabito(null) }
                    )
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            TituloSecao("Sua semana · % de hábitos concluídos")
                            GraficoSemana(estado.semana, MaterialTheme.colorScheme.primary, estado.data)
                        }
                    }
                }
                if (estado.itens.isNotEmpty()) {
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
                // Hábitos que existem mas não valem hoje: mostra para o usuário não achar que sumiram.
                if (estado.foraDeHoje.isNotEmpty()) {
                    item { TituloSecao("Fora de hoje", Modifier.padding(top = 8.dp, start = 4.dp)) }
                    items(estado.foraDeHoje, key = { "fora_${it.id}" }) { habito ->
                        ItemForaDeHoje(habito, onClick = { onAbrir(habito.id) })
                    }
                }
                item { CartaoDica(estado.dica, onOutra = viewModel::proximaDica) }
                if (estado.sugestoes.isNotEmpty()) {
                    item { TituloSecao("Sugestões para começar", Modifier.padding(top = 8.dp, start = 4.dp)) }
                    item {
                        // LazyRow = lista horizontal (carrossel).
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(estado.sugestoes) { indice ->
                                CartaoSugestao(SUGESTOES[indice], onClick = { onCriarHabito(indice) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Medidor de 270° com "3/5" no centro e uma frase de status. */
@Composable
private fun ResumoDoDia(concluidos: Int, total: Int, temHabitos: Boolean, onCriar: () -> Unit) {
    val completo = total > 0 && concluidos == total
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
                        !temHabitos -> "Seu painel começa aqui."
                        total == 0 -> "Nenhum hábito hoje. Dia de descanso 🌿"
                        completo -> "Dia completo! 🎉"
                        total - concluidos == 1 -> "Falta 1 hábito para fechar o dia."
                        else -> "Faltam ${total - concluidos} hábitos para fechar o dia."
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                if (temHabitos) {
                    Text(
                        "${(concluidos * 100) / maxOf(total, 1)}% concluído",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Crie um hábito ou escolha uma sugestão abaixo. O medidor e o gráfico enchem conforme você marca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onCriar) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Criar hábito")
                    }
                }
            }
        }
    }
}

/** Linha simples, sem botão de marcar: o hábito não vale hoje. */
@Composable
private fun ItemForaDeHoje(habito: Habito, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(habito.icone, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(habito.nome, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "Programado para: ${habito.diasDaSemana.resumo()}",
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
            // Anel na cor do hábito, com o emoji no centro.
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
                    habito.meta != null -> "${item.valor} de ${habito.comUnidade(habito.meta)}" to MaterialTheme.colorScheme.onSurfaceVariant
                    else -> "Pendente" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(texto, style = MaterialTheme.typography.bodySmall, color = corTexto)
            }
            if (habito.meta == null) {
                // Acende na cor do hábito quando marcado.
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
                // Passo proporcional à meta: com meta 30, cada toque soma 3.
                val passo = habito.passo
                val sufixo = if (passo > 1) "$passo" else ""
                // Não há ícone de "menos" padrão: usamos texto.
                FilledTonalIconButton(onClick = { onSomar(-passo) }, enabled = item.valor > 0) {
                    Text("−$sufixo", style = if (passo > 1) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleLarge)
                }
                FilledTonalIconButton(onClick = { onSomar(+passo) }) {
                    Text("+$sufixo", style = if (passo > 1) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

/** Dica do dia; o botão "Outra dica" troca. */
@Composable
private fun CartaoDica(dica: Dica, onOutra: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DICA DO DIA", style = EstiloRotulo, color = Ambar)
            Row(verticalAlignment = Alignment.Top) {
                Text(dica.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(dica.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(dica.texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = onOutra, modifier = Modifier.align(Alignment.End)) { Text("Outra dica") }
        }
    }
}

/** Cartão de sugestão no carrossel. */
@Composable
private fun CartaoSugestao(sugestao: Sugestao, onClick: () -> Unit) {
    Card(Modifier.width(150.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AnelProgresso(progresso = 0f, cor = Color(sugestao.cor), espessura = 4.dp, modifier = Modifier.size(44.dp)) {
                Text(sugestao.icone, style = MaterialTheme.typography.titleMedium)
            }
            Text(sugestao.nome, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                sugestao.meta?.let { "$it ${sugestao.unidade} por dia" } ?: "Feito / não feito",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("+ Adicionar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
