package com.example.constancia.data

import android.content.Context
import com.example.constancia.data.local.ConstanciaDatabase
import com.example.constancia.data.remote.BrasilApiService
import com.example.constancia.data.repository.AuthRepository
import com.example.constancia.data.repository.FeriadoRepository
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.data.repository.PerfilRepository
import com.example.constancia.data.repository.RegistroRepository
import com.example.constancia.data.repository.SessaoRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Injeção de dependência manual: cria e guarda banco, API e repositórios do app. */
class AppContainer(context: Context) {

    // by lazy: só cria no primeiro uso.
    private val database by lazy { ConstanciaDatabase.obter(context) }

    private val brasilApi: BrasilApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://brasilapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())   // JSON ↔ objetos Kotlin
            .build()
            .create(BrasilApiService::class.java)                 // gera a implementação da interface
    }

    val sessaoRepository by lazy { SessaoRepository(context) }
    val authRepository by lazy { AuthRepository(database.usuarioDao(), sessaoRepository) }
    val habitoRepository by lazy { HabitoRepository(database.habitoDao()) }
    val registroRepository by lazy { RegistroRepository(database.registroDiarioDao()) }
    val feriadoRepository by lazy { FeriadoRepository(brasilApi) }

    val perfilRepository by lazy { PerfilRepository(database.usuarioDao(), context) }
}
