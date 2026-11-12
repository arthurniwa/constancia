package com.example.constancia.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.constancia.data.local.Usuario
import com.example.constancia.data.local.UsuarioDao
import com.example.constancia.domain.SenhaHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Base64

sealed interface ResultadoAuth {
    data class Sucesso(val usuario: Usuario) : ResultadoAuth
    data class Erro(val mensagem: String) : ResultadoAuth
}

/** Cadastro, login e logout: junta o banco (UsuarioDao) com a sessão. */
class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val sessao: SessaoRepository
) {

    /** Usuário logado agora (ou null). Se ele não existe mais no banco, encerra a sessão. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioLogado: Flow<Usuario?> = sessao.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else usuarioDao.observarPorId(id).map { usuario ->
            if (usuario == null) sessao.encerrar()
            usuario
        }
    }

    suspend fun cadastrar(nome: String, email: String, senha: String): ResultadoAuth {
        val emailNormalizado = email.trim().lowercase()
        // PBKDF2 é lento de propósito: roda fora da thread da tela.
        val usuario = withContext(Dispatchers.Default) {
            val salt = SenhaHasher.gerarSalt()
            Usuario(
                nome = nome.trim(),
                email = emailNormalizado,
                senhaHash = Base64.getEncoder().encodeToString(SenhaHasher.hash(senha, salt)),
                salt = Base64.getEncoder().encodeToString(salt)
            )
        }
        return try {
            val id = usuarioDao.inserir(usuario)
            sessao.iniciar(id)                     // já entra logado após o cadastro
            ResultadoAuth.Sucesso(usuario.copy(id = id))
        } catch (e: SQLiteConstraintException) {
            // O índice único de e-mail barrou o INSERT.
            ResultadoAuth.Erro("Este e-mail já está cadastrado. Tente entrar.")
        }
    }

    suspend fun entrar(email: String, senha: String): ResultadoAuth {
        val usuario = usuarioDao.buscarPorEmail(email.trim().lowercase())
        val senhaOk = usuario != null && withContext(Dispatchers.Default) {
            SenhaHasher.confere(
                senha,
                Base64.getDecoder().decode(usuario.salt),
                Base64.getDecoder().decode(usuario.senhaHash)
            )
        }
        // Mesma mensagem para e-mail inexistente e senha errada (não revela quem tem conta).
        if (usuario == null || !senhaOk) return ResultadoAuth.Erro("E-mail ou senha incorretos.")
        sessao.iniciar(usuario.id)
        return ResultadoAuth.Sucesso(usuario)
    }

    fun sair() = sessao.encerrar()
}
