package com.example.constancia.data

import android.content.Context
import com.example.constancia.data.local.ConstanciaDatabase
import com.example.constancia.data.remote.BrasilApiService
import com.example.constancia.data.repository.AuthRepository
import com.example.constancia.data.repository.FeriadoRepository
import com.example.constancia.data.repository.HabitoRepository
import com.example.constancia.data.repository.RegistroRepository
import com.example.constancia.data.repository.SessaoRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Injeção de dependência "feita à mão".
 * ≈ o `builder.Services.AddSingleton<...>()` do Program.cs, só que sem framework:
 * um objeto que cria e guarda as dependências do app inteiro.
 * (Em projetos maiores usaríamos Hilt, que é o "container de DI" do Android.)
 */
class AppContainer(context: Context) {

    // `by lazy` ≈ Lazy<T> do C#: só cria na primeira vez que alguém acessar.
    private val database by lazy { ConstanciaDatabase.obter(context) }

    // Retrofit configurado uma vez (≈ registrar um HttpClient com BaseAddress no DI).
    private val brasilApi: BrasilApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://brasilapi.com.br/")                 // precisa terminar com "/"
            .addConverterFactory(GsonConverterFactory.create())   // JSON ↔ objetos Kotlin
            .build()
            .create(BrasilApiService::class.java)                 // gera a implementação da interface
    }

    val sessaoRepository by lazy { SessaoRepository(context) }
    val authRepository by lazy { AuthRepository(database.usuarioDao(), sessaoRepository) }
    val habitoRepository by lazy { HabitoRepository(database.habitoDao()) }
    val registroRepository by lazy { RegistroRepository(database.registroDiarioDao()) }
    val feriadoRepository by lazy { FeriadoRepository(brasilApi) }
}
