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
import com.example.constancia.ui.util.nomeCurto
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/** Tela de criar/editar hábito (CREATE e UPDATE da tabela Habito). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitoFormScreen(
    onVoltar: () -> Unit,
    viewModel: HabitoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.uiState
    // Escopo de coroutine ligado a esta tela, para chamar a função `suspend fun salvar()`.
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
            return@Scaffold   // "return" só desta lambda (≈ sair cedo do bloco)
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()                          // sobe o conteúdo quando o teclado abre
                .verticalScroll(rememberScrollState()) // rola em telas pequenas / modo paisagem
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.nome,
                onValueChange = viewModel::alterarNome,  // a cada tecla, avisa o ViewModel
                label = { Text("Nome do hábito") },
                isError = estado.erroNome != null,
                supportingText = estado.erroNome?.let { { Text(it) } },  // mostra o erro embaixo do campo
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

            OutlinedTextField(
                value = estado.metaTexto,
                onValueChange = viewModel::alterarMeta,
                label = { Text("Meta diária (opcional)") },
                placeholder = { Text("Ex.: 8 (copos de água)") },
                isError = estado.erroMeta != null,
                supportingText = {
                    Text(estado.erroMeta ?: "Deixe vazio para hábitos do tipo feito/não feito")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // teclado numérico
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // launch: inicia a coroutine; dentro dela podemos chamar funções suspend.
                    scope.launch { if (viewModel.salvar()) onVoltar() }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar") }
        }
    }
}

/**
 * Título + conteúdo. O último parâmetro é uma lambda `@Composable`, então quem chama
 * pode usar a sintaxe `Secao("x") { ... }` (≈ RenderFragment ChildContent do Blazor).
 */
@Composable
private fun Secao(titulo: String, conteudo: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        conteudo()
    }
}
