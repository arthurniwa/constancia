package com.example.constancia.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Usuario
import com.example.constancia.data.repository.AuthRepository
import com.example.constancia.data.repository.ResultadoAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Os 3 momentos do app: ainda lendo a sessão, sem login, ou com alguém logado. */
sealed interface EstadoSessao {
    data object Carregando : EstadoSessao              // `data object` = singleton com toString bonito
    data object Deslogado : EstadoSessao
    data class Logado(val usuario: Usuario) : EstadoSessao
}

data class AuthUiState(
    val modoCadastro: Boolean = false,   // false = "Entrar", true = "Criar conta"
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val mostrarSenha: Boolean = false,
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val erroSenha: String? = null,
    val erroConfirmar: String? = null,
    val erroGeral: String? = null,       // ex.: "E-mail ou senha incorretos."
    val enviando: Boolean = false
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    /** Observado pela RaizApp para decidir: tela de login ou o app. */
    val sessao: StateFlow<EstadoSessao> = authRepository.usuarioLogado
        .map { usuario -> if (usuario == null) EstadoSessao.Deslogado else EstadoSessao.Logado(usuario) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSessao.Carregando)

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun alternarModo() {
        // Troca entre Entrar/Criar conta mantendo o e-mail digitado e limpando os erros.
        uiState = AuthUiState(modoCadastro = !uiState.modoCadastro, email = uiState.email)
    }

    fun alterarNome(v: String) { uiState = uiState.copy(nome = v, erroNome = null, erroGeral = null) }
    fun alterarEmail(v: String) { uiState = uiState.copy(email = v, erroEmail = null, erroGeral = null) }
    fun alterarSenha(v: String) { uiState = uiState.copy(senha = v, erroSenha = null, erroGeral = null) }
    fun alterarConfirmar(v: String) { uiState = uiState.copy(confirmarSenha = v, erroConfirmar = null, erroGeral = null) }
    fun alternarMostrarSenha() { uiState = uiState.copy(mostrarSenha = !uiState.mostrarSenha) }

    private fun validar(): Boolean {
        val s = uiState
        val erroNome = if (s.modoCadastro && s.nome.isBlank()) "Informe seu nome" else null
        val erroEmail = when {
            s.email.isBlank() -> "Informe o e-mail"
            // Patterns.EMAIL_ADDRESS: expressão regular pronta do Android para e-mails.
            !Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() -> "E-mail inválido"
            else -> null
        }
        val erroSenha = when {
            s.senha.isEmpty() -> "Informe a senha"
            s.modoCadastro && s.senha.length < 6 -> "Mínimo de 6 caracteres"
            else -> null
        }
        val erroConfirmar = if (s.modoCadastro && s.confirmarSenha != s.senha) "As senhas não conferem" else null
        uiState = s.copy(erroNome = erroNome, erroEmail = erroEmail, erroSenha = erroSenha, erroConfirmar = erroConfirmar)
        return listOf(erroNome, erroEmail, erroSenha, erroConfirmar).all { it == null }
    }

    fun enviar() {
        if (uiState.enviando || !validar()) return
        uiState = uiState.copy(enviando = true, erroGeral = null)
        viewModelScope.launch {
            val s = uiState
            val resultado = if (s.modoCadastro) authRepository.cadastrar(s.nome, s.email, s.senha)
                            else authRepository.entrar(s.email, s.senha)
            uiState = when (resultado) {
                // Deu certo: limpa o formulário (inclusive a senha). A RaizApp troca de tela sozinha
                // porque a sessão mudou.
                is ResultadoAuth.Sucesso -> AuthUiState()
                is ResultadoAuth.Erro -> uiState.copy(enviando = false, erroGeral = resultado.mensagem)
            }
        }
    }

    fun sair() = authRepository.sair()
}
