package com.example.constancia

import android.app.Application
import com.example.constancia.data.AppContainer

/** Criada uma vez quando o app abre; monta o AppContainer (DI). Registrada no Manifest. */
class ConstanciaApplication : Application() {

    // lateinit: inicializada no onCreate, antes de qualquer uso.
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
