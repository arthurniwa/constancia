package com.example.constancia

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.constancia.ui.navigation.RaizApp
import com.example.constancia.ui.theme.ConstanciaTheme

/** Única Activity do app: liga o Compose e abre RaizApp() (login → app). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tela inteira, com ícones claros nas barras (o app é sempre escuro).
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
