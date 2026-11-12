package com.example.constancia.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.constancia.ui.AppViewModelProvider
import com.example.constancia.ui.auth.AuthScreen
import com.example.constancia.ui.auth.AuthViewModel
import com.example.constancia.ui.auth.EstadoSessao

/**
 * Porta de entrada: mostra o login ou o app, conforme a sessão.
 * Quando a sessão muda, a tela se redesenha sozinha (sem navegar).
 */
@Composable
fun RaizApp(authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val sessao by authViewModel.sessao.collectAsState()

    // sealed interface: o compilador exige tratar os 3 casos.
    when (val s = sessao) {
        EstadoSessao.Carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        EstadoSessao.Deslogado -> AuthScreen(authViewModel)
        is EstadoSessao.Logado ->
            // key(id): outro usuário = navegação e ViewModels recriados do zero.
            key(s.usuario.id) {
                ConstanciaApp(nomeUsuario = s.usuario.nome, onSair = authViewModel::sair)
            }
    }
}
