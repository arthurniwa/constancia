package com.example.constancia

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.constancia.ui.navigation.RaizApp
import com.example.constancia.ui.theme.ConstanciaTheme

/**
 * Única Activity do app: só liga o Compose e entrega o controle para RaizApp() (login → app).
 * As "telas" são funções @Composable trocadas pelo Navigation (≈ SPA do Blazor).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Desenha atrás das barras do sistema (o Scaffold cuida dos espaçamentos).
        // SystemBarStyle.dark: ícones claros na barra de status, porque o app é sempre escuro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            ConstanciaTheme {
                RaizApp()
            }
        }
    }
}
