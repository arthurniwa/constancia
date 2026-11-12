package com.example.constancia.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sessão: guarda o id do usuário logado em SharedPreferences (sobrevive ao fechar o app). */
class SessaoRepository(context: Context) {

    private val prefs = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    // -1 = ninguém logado → vira null.
    private val _usuarioId = MutableStateFlow(prefs.getLong(CHAVE, -1L).takeIf { it > 0 })
    val usuarioId: StateFlow<Long?> = _usuarioId.asStateFlow()

    fun iniciar(usuarioId: Long) {
        prefs.edit { putLong(CHAVE, usuarioId) }   // grava em disco
        _usuarioId.value = usuarioId                // a tela troca sozinha
    }

    fun encerrar() {
        prefs.edit { remove(CHAVE) }
        _usuarioId.value = null
    }

    private companion object {
        const val CHAVE = "usuario_id"
    }
}
