package com.example.constancia.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Tela de login e cadastro. */
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val s = viewModel.uiState
    // Esconde a senha, a menos que toque em "Mostrar".
    val transformacaoSenha = if (s.mostrarSenha) VisualTransformation.None else PasswordVisualTransformation()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()                          // sobe o formulário quando o teclado abre
                .verticalScroll(rememberScrollState()) // rola em telas pequenas/paisagem
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text("🌱", style = MaterialTheme.typography.displayMedium)
            Text("Constância", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Construa hábitos, um dia de cada vez.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (s.modoCadastro) "Criar conta" else "Entrar",
                style = MaterialTheme.typography.titleLarge
            )

            // Limita a largura em paisagem/tablet.
            val campo = Modifier.fillMaxWidth().widthIn(max = 480.dp)

            if (s.modoCadastro) {
                OutlinedTextField(
                    value = s.nome, onValueChange = viewModel::alterarNome,
                    label = { Text("Nome") }, singleLine = true, modifier = campo,
                    isError = s.erroNome != null, supportingText = s.erroNome?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            OutlinedTextField(
                value = s.email, onValueChange = viewModel::alterarEmail,
                label = { Text("E-mail") }, singleLine = true, modifier = campo,
                isError = s.erroEmail != null, supportingText = s.erroEmail?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = s.senha, onValueChange = viewModel::alterarSenha,
                label = { Text("Senha") }, singleLine = true, modifier = campo,
                isError = s.erroSenha != null, supportingText = s.erroSenha?.let { { Text(it) } },
                visualTransformation = transformacaoSenha,
                trailingIcon = {
                    TextButton(onClick = viewModel::alternarMostrarSenha) {
                        Text(if (s.mostrarSenha) "Ocultar" else "Mostrar")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (s.modoCadastro) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.enviar() })
            )
            if (s.modoCadastro) {
                OutlinedTextField(
                    value = s.confirmarSenha, onValueChange = viewModel::alterarConfirmar,
                    label = { Text("Confirmar senha") }, singleLine = true, modifier = campo,
                    isError = s.erroConfirmar != null, supportingText = s.erroConfirmar?.let { { Text(it) } },
                    visualTransformation = transformacaoSenha,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.enviar() })
                )
            }

            s.erroGeral?.let {
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }

            Button(onClick = viewModel::enviar, enabled = !s.enviando, modifier = campo) {
                if (s.enviando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(if (s.modoCadastro) "Criar conta" else "Entrar")
            }
            TextButton(onClick = viewModel::alternarModo) {
                Text(if (s.modoCadastro) "Já tenho conta. Entrar" else "Primeira vez aqui? Criar conta")
            }
        }
    }
}
