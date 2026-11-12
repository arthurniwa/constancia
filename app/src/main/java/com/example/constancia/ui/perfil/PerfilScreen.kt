package com.example.constancia.ui.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.theme.EstiloNumero
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)   // TopAppBar ainda é experimental
@Composable
fun PerfilScreen(
    onSair: () -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s = viewModel.uiState
    var confirmandoSaida by rememberSaveable { mutableStateOf(false) }

    // Seletor de fotos do Android: não precisa pedir permissão.
    // Abre a galeria e devolve o endereço (Uri) da foto escolhida, ou null se cancelar.
    val escolherFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.trocarFoto(uri)
    }
    val abrirGaleria = {
        escolherFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Snackbar: aparece quando `mensagem` muda.
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(s.mensagem) {
        s.mensagem?.let {
            snackbar.showSnackbar(it)
            viewModel.mensagemExibida()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Perfil") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        if (s.carregando) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto: círculo clicável. Com foto → a imagem; sem foto → a inicial do nome.
            Box(
                Modifier.size(112.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { abrirGaleria() },
                contentAlignment = Alignment.Center
            ) {
                if (s.fotoCaminho != null) {
                    AsyncImage(                               // Coil: carrega a imagem do arquivo
                        model = File(s.fotoCaminho),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,     // preenche o círculo sem distorcer
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(s.nome.take(1).uppercase(), style = EstiloNumero.grande)
                }
            }
            TextButton(onClick = { abrirGaleria() }) { Text("Trocar foto") }

            OutlinedTextField(
                value = s.nome, onValueChange = viewModel::alterarNome,
                label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = s.erroNome != null, supportingText = s.erroNome?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = s.email, onValueChange = viewModel::alterarEmail,
                label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = s.erroEmail != null, supportingText = s.erroEmail?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
            )

            Button(
                onClick = viewModel::salvar,
                enabled = !s.salvando,                    // evita clique duplo
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (s.salvando) "Salvando..." else "Salvar") }

            OutlinedButton(
                onClick = { confirmandoSaida = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sair da conta") }
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
}
