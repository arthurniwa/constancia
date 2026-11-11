package com.example.constancia.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lembra QUEM está logado, mesmo depois de fechar o app (≈ um cookie de autenticação).
 * SharedPreferences = arquivo chave/valor privado do app (≈ um pequeno appsettings que o app grava).
 * Guardamos só o id do usuário — nunca a senha.
 */
class SessaoRepository(context: Context) {

    private val prefs = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    // -1 = ninguém logado. `takeIf { }` devolve o valor se a condição for verdadeira, senão null.
    private val _usuarioId = MutableStateFlow(prefs.getLong(CHAVE, -1L).takeIf { it > 0 })
    val usuarioId: StateFlow<Long?> = _usuarioId.asStateFlow()

    fun iniciar(usuarioId: Long) {
        prefs.edit { putLong(CHAVE, usuarioId) }   // grava em disco
        _usuarioId.value = usuarioId                // avisa quem estiver observando (a tela troca sozinha)
    }

    fun encerrar() {
        prefs.edit { remove(CHAVE) }
        _usuarioId.value = null
    }

    private companion object {
        const val CHAVE = "usuario_id"
    }
}
