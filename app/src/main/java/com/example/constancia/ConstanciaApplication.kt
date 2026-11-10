package com.example.constancia

import android.app.Application
import com.example.constancia.data.AppContainer

/**
 * Classe que representa o app inteiro: é criada uma vez, antes de qualquer tela,
 * e vive enquanto o processo do app viver. Registrada no AndroidManifest (android:name).
 * ≈ o Program.cs / a raiz de composição onde montamos o container de DI.
 */
class ConstanciaApplication : Application() {

    // `lateinit var`: "prometo inicializar antes de usar" (não precisa ser nullable).
    lateinit var container: AppContainer
        private set   // só esta classe pode atribuir (≈ `{ get; private set; }`)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
