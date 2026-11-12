package com.example.constancia.ui.habitos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.util.OPCOES_COR
import com.example.constancia.ui.util.OPCOES_ICONE
import com.example.constancia.ui.util.OPCOES_UNIDADE
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.example.constancia.ui.util.nomeCurto
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/** Mesma tela para criar e editar hábito. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitoFormScreen(
    onVoltar: () -> Unit,
    viewModel: HabitoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.uiState
    // Para chamar salvar() (suspend) a partir do botão.
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.ehEdicao) "Editar hábito" else "Novo hábito") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (estado.carregando) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold   // sai só desta lambda
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()                          // sobe o conteúdo quando o teclado abre
                .verticalScroll(rememberScrollState()) // rola em telas pequenas/paisagem
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.nome,
                onValueChange = viewModel::alterarNome,  // cada tecla vai para o ViewModel
                label = { Text("Nome do hábito") },
                isError = estado.erroNome != null,
                supportingText = estado.erroNome?.let { { Text(it) } },  // erro embaixo do campo
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Secao("Ícone") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OPCOES_ICONE.forEach { icone ->
                        val selecionado = icone == estado.icone
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(
                                    BorderStroke(
                                        if (selecionado) 2.dp else 1.dp,
                                        if (selecionado) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    CircleShape
                                )
                                .clickable { viewModel.alterarIcone(icone) },
                            contentAlignment = Alignment.Center
                        ) { Text(icone, style = MaterialTheme.typography.titleLarge) }
                    }
                }
            }

            Secao("Cor") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OPCOES_COR.forEach { cor ->
                        val selecionada = cor == estado.cor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(cor), CircleShape)
                                .clickable { viewModel.alterarCor(cor) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selecionada) Icon(Icons.Default.Check, contentDescription = "Selecionada", tint = Color.White)
                        }
                    }
                }
            }

            Secao("Dias da semana") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DayOfWeek.entries.forEach { dia ->
                        FilterChip(
                            selected = dia in estado.dias,
                            onClick = { viewModel.alternarDia(dia) },
                            label = { Text(dia.nomeCurto()) },
                            // ✓ nos dias marcados, para não depender só da cor
                            leadingIcon = if (dia in estado.dias) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
                estado.erroDias?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Secao("Como medir") {
                // Dois botões lado a lado: só um fica marcado (≈ radio button).
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !estado.medirQuantidade,
                        onClick = { viewModel.alterarMedirQuantidade(false) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Feito / não feito") }
                    SegmentedButton(
                        selected = estado.medirQuantidade,
                        onClick = { viewModel.alterarMedirQuantidade(true) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("Quantidade") }
                }

                // Só aparece no modo "Quantidade".
                if (estado.medirQuantidade) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = estado.metaTexto,
                            onValueChange = viewModel::alterarMeta,
                            label = { Text("Meta por dia") },
                            placeholder = { Text("Ex.: 8") },
                            isError = estado.erroMeta != null,
                            supportingText = estado.erroMeta?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = estado.unidade,
                            onValueChange = viewModel::alterarUnidade,
                            label = { Text("Unidade") },
                            placeholder = { Text("Ex.: copos") },
                            isError = estado.erroUnidade != null,
                            supportingText = estado.erroUnidade?.let { { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Atalhos: um toque preenche a unidade.
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OPCOES_UNIDADE.forEach { u ->
                            FilterChip(
                                selected = estado.unidade.equals(u, ignoreCase = true),
                                onClick = { viewModel.alterarUnidade(u) },
                                label = { Text(u) }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    // Salva; se passou na validação, volta.
                    scope.launch { if (viewModel.salvar()) onVoltar() }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar") }
        }
    }
}

/** Título + conteúdo; uso: `Secao("x") { ... }`. */
@Composable
private fun Secao(titulo: String, conteudo: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        conteudo()
    }
}
