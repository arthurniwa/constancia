package com.example.constancia.ui.historico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.RegistroDiario
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.habitos.MensagemVazia
import com.example.constancia.ui.util.CarregandoFeriados
import com.example.constancia.ui.util.ErroFeriados
import com.example.constancia.ui.util.formatoCurto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Histórico de um hábito: CRUD completo da tabela RegistroDiario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    onVoltar: () -> Unit,
    viewModel: HistoricoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()
    val edicao by viewModel.edicao.collectAsState()
    val calendario by viewModel.calendario.collectAsState()
    val paraExcluir by viewModel.paraExcluir.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    // LaunchedEffect: mostra o Snackbar quando `mensagem` muda.
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbar.showSnackbar(it)
            viewModel.mensagemExibida()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico · ${estado.habito?.nome ?: ""}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (estado.habito != null) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::abrirCalendarioParaAdicionar,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Registrar dia") }
                )
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding).fillMaxSize()
        val habito = estado.habito
        when {
            estado.carregando -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            habito == null -> MensagemVazia("Hábito não encontrado.", modifier)
            else -> LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Feriados da API: carregando / erro ("Tentar novamente").
                when {
                    estado.carregandoFeriados -> item { CarregandoFeriados(Modifier.padding(vertical = 4.dp)) }
                    estado.erroFeriados != null -> item { ErroFeriados(estado.erroFeriados!!, viewModel::carregarFeriados) }
                }
                if (estado.itens.isEmpty()) {
                    item {
                        MensagemVazia("Nenhum registro ainda.\nMarque o hábito na tela Hoje ou toque em \"Registrar dia\".")
                    }
                }
                // Chave única: "r" + id (registro) ou "f" + data (feriado).
                items(estado.itens, key = { item ->
                    when (item) {
                        is ItemHistorico.Registro -> "r${item.registro.id}"
                        is ItemHistorico.Feriado -> "f${item.data}"
                    }
                }) { item ->
                    // Registro ou feriado (sealed interface).
                    when (item) {
                        is ItemHistorico.Registro -> ItemRegistro(
                            registro = item.registro,
                            nomeFeriado = item.nomeFeriado,
                            habito = habito,
                            onEditar = { viewModel.editar(item.registro) },
                            onExcluir = { viewModel.pedirExclusao(item.registro) }
                        )
                        is ItemHistorico.Feriado -> ItemFeriado(item)
                    }
                }
            }
        }
    }

    // ----- Diálogos (estado no ViewModel: sobrevive à rotação) -----

    if (calendario != ModoCalendario.FECHADO) {
        CalendarioDialog(
            dataInicial = if (calendario == ModoCalendario.EDITAR) edicao?.data else LocalDate.now(),
            onConfirmar = viewModel::dataEscolhida,
            onCancelar = viewModel::fecharCalendario
        )
    }

    // Cópia local para o Kotlin saber que não é null dentro do if.
    val e = edicao
    if (e != null && calendario == ModoCalendario.FECHADO) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEdicao,
            title = { Text("Editar registro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = viewModel::abrirCalendarioParaEditar, modifier = Modifier.fillMaxWidth()) {
                        Text("Data: ${e.data.formatoCurto()}")
                    }
                    if (estado.habito?.meta != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalIconButton(onClick = { viewModel.alterarValorEdicao(-(estado.habito?.passo ?: 1)) }) { Text("−") }
                            Text("${e.valor} de ${estado.habito?.let { h -> h.meta?.let { h.comUnidade(it) } }}", style = MaterialTheme.typography.titleMedium)
                            FilledTonalIconButton(onClick = { viewModel.alterarValorEdicao(estado.habito?.passo ?: 1) }) { Text("+") }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::salvarEdicao) { Text("Salvar") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarEdicao) { Text("Cancelar") } }
        )
    }

    paraExcluir?.let { r ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,
            title = { Text("Excluir registro?") },
            text = { Text("O registro de ${r.data.formatoCurto()} será apagado.") },
            confirmButton = { TextButton(onClick = viewModel::confirmarExclusao) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarExclusao) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ItemRegistro(
    registro: RegistroDiario,
    nomeFeriado: String?,
    habito: Habito,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(registro.data.formatoCurto(), style = MaterialTheme.typography.titleMedium)
                val texto = if (habito.meta == null) "Feito ✓"
                else "${registro.valor} de ${habito.comUnidade(habito.meta)}" + if (registro.valor >= habito.alvoDiario) " ✓" else ""
                Text(texto, style = MaterialTheme.typography.bodyMedium)
                if (nomeFeriado != null) {
                    Text("🎉 $nomeFeriado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, "Editar registro") }
            IconButton(onClick = onExcluir) { Icon(Icons.Default.Delete, "Excluir registro") }
        }
    }
}

/** Feriado sem registro: destacado, só informativo. */
@Composable
private fun ItemFeriado(item: ItemHistorico.Feriado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.data.formatoCurto(), style = MaterialTheme.typography.titleMedium)
            Text(
                "🎉 ${item.nome} · descanso automático",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/** Calendário. O DatePicker usa millis em UTC, então convertemos com ZoneOffset.UTC. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarioDialog(
    dataInicial: LocalDate?,
    onConfirmar: (LocalDate) -> Unit,
    onCancelar: () -> Unit
) {
    val hojeMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = dataInicial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        // Bloqueia datas futuras.
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hojeMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = {
                    estado.selectedDateMillis?.let {
                        onConfirmar(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = estado.selectedDateMillis != null
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    ) {
        DatePicker(state = estado)
    }
}
