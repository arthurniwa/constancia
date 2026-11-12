package com.example.constancia.ui.perfil

import android.net.Uri
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.constancia.data.local.Usuario
import com.example.constancia.data.repository.PerfilRepository
import com.example.constancia.data.repository.ResultadoAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PerfilUiState(
    val carregando: Boolean = true,
    val nome: String = "",
    val email: String = "",
    val fotoCaminho: String? = null,
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val mensagem: String? = null,     // texto do Snackbar
    val salvando: Boolean = false
)

class PerfilViewModel(
    private val repository: PerfilRepository,
    private val usuarioId: Long
) : ViewModel() {

    var uiState by mutableStateOf(PerfilUiState())
        private set

    private var usuario: Usuario? = null   // a versão salva no banco

    init {
        viewModelScope.launch {
            // first(): pega o valor atual do Flow uma vez (o form não deve mudar sozinho enquanto digita).
            val u = repository.observar(usuarioId).first() ?: return@launch
            usuario = u
            uiState = uiState.copy(carregando = false, nome = u.nome, email = u.email, fotoCaminho = u.fotoCaminho)
        }
    }

    fun alterarNome(v: String) { uiState = uiState.copy(nome = v, erroNome = null) }
    fun alterarEmail(v: String) { uiState = uiState.copy(email = v, erroEmail = null) }

    private fun validar(): Boolean {
        val s = uiState
        val erroNome = if (s.nome.isBlank()) "Informe seu nome" else null
        val erroEmail = when {
            s.email.isBlank() -> "Informe o e-mail"
            !Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() -> "E-mail inválido"
            else -> null
        }
        uiState = s.copy(erroNome = erroNome, erroEmail = erroEmail)
        return erroNome == null && erroEmail == null
    }

    fun salvar() {
        val u = usuario ?: return
        if (uiState.salvando || !validar()) return
        uiState = uiState.copy(salvando = true)
        viewModelScope.launch {
            when (val r = repository.salvar(u, uiState.nome, uiState.email)) {
                is ResultadoAuth.Sucesso -> {
                    usuario = r.usuario
                    uiState = uiState.copy(salvando = false, email = r.usuario.email, mensagem = "Perfil salvo")
                }
                is ResultadoAuth.Erro -> uiState = uiState.copy(salvando = false, erroEmail = r.mensagem)
            }
        }
    }

    fun trocarFoto(uri: Uri) {
        val u = usuario ?: return
        viewModelScope.launch {
            val caminho = repository.trocarFoto(u, uri)
            usuario = u.copy(fotoCaminho = caminho)
            uiState = uiState.copy(fotoCaminho = caminho, mensagem = "Foto atualizada")
        }
    }

    fun mensagemExibida() { uiState = uiState.copy(mensagem = null) }
}
