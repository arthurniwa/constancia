package com.example.constancia.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import com.example.constancia.data.local.Usuario
import com.example.constancia.data.local.UsuarioDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

/** Perfil do usuário: editar nome/e-mail e trocar a foto. */
class PerfilRepository(
    private val usuarioDao: UsuarioDao,
    private val context: Context
) {
    fun observar(usuarioId: Long): Flow<Usuario?> = usuarioDao.observarPorId(usuarioId)

    // Salva nome e e-mail. O índice único barra e-mail de outra conta.
    suspend fun salvar(usuario: Usuario, nome: String, email: String): ResultadoAuth =
        try {
            val atualizado = usuario.copy(nome = nome.trim(), email = email.trim().lowercase())
            usuarioDao.atualizar(atualizado)
            ResultadoAuth.Sucesso(atualizado)
        } catch (e: SQLiteConstraintException) {
            ResultadoAuth.Erro("Este e-mail já está em uso por outra conta.")
        }

    // Copia a foto escolhida para a pasta privada do app e salva o caminho no banco.
    // withContext(IO): ler/gravar arquivo fora da thread da tela (≈ Task.Run no C#).
    suspend fun trocarFoto(usuario: Usuario, uri: Uri): String = withContext(Dispatchers.IO) {
        // Nome único a cada troca: o Coil não mostra a foto velha do cache.
        val arquivo = File(context.filesDir, "perfil_${usuario.id}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            arquivo.outputStream().use { saida -> entrada.copyTo(saida) }
        }
        usuario.fotoCaminho?.let { File(it).delete() }          // apaga a foto anterior
        usuarioDao.atualizar(usuario.copy(fotoCaminho = arquivo.absolutePath))
        arquivo.absolutePath
    }
}
