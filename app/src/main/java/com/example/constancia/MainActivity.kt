package com.example.constancia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.constancia.ui.navigation.ConstanciaApp
import com.example.constancia.ui.theme.ConstanciaTheme

/**
 * Única Activity do app: só liga o Compose e entrega o controle para ConstanciaApp().
 * As "telas" são funções @Composable trocadas pelo Navigation (≈ SPA do Blazor).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // desenha atrás das barras do sistema; o Scaffold cuida dos espaçamentos
        setContent {
            ConstanciaTheme {
                ConstanciaApp()
            }
        }
    }
}
