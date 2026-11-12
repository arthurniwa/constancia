package com.example.constancia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema único, escuro, com cores próprias (sem as cores automáticas do papel de parede do Android).
 * O MaterialTheme espalha estas cores para todos os componentes: Card, Button, NavigationBar...
 * ≈ variáveis CSS globais (:root { --primary: ... }) num site.
 */
private val Esquema = darkColorScheme(
    primary = Azul,
    onPrimary = Color(0xFF00141F),
    primaryContainer = Color(0xFF0B3A50),
    onPrimaryContainer = Color(0xFFBFE9FF),
    secondary = Verde,
    onSecondary = Color(0xFF07210A),
    secondaryContainer = Color(0xFF1F3A24),   // fundo do item selecionado na barra inferior
    onSecondaryContainer = Color(0xFFCFF5D3),
    tertiary = Ambar,
    onTertiary = Color(0xFF2A1D00),
    tertiaryContainer = Color(0xFF3A2C0A),    // cartões de feriado
    onTertiaryContainer = Color(0xFFFFE3A6),
    error = Vermelho,
    errorContainer = Color(0xFF4A1518),
    onErrorContainer = Color(0xFFFFD6D8),
    background = Fundo,
    onBackground = TextoPrincipal,
    surface = Fundo,
    onSurface = TextoPrincipal,
    // Atenção: surfaceVariant precisa ser DIFERENTE de Cartao. O Material escolhe a cor do texto
    // procurando a cor do fundo nesta lista; se Cartao também fosse surfaceVariant, o texto dos
    // cartões sairia cinza (onSurfaceVariant) em vez de branco (onSurface).
    surfaceVariant = CartaoAlto,
    onSurfaceVariant = TextoSecundario,
    // "Containers" de superfície: o Material 3 usa estes tons em Card, NavigationBar, diálogos...
    surfaceContainerLowest = Fundo,
    surfaceContainerLow = Superficie,
    surfaceContainer = Superficie,
    surfaceContainerHigh = Cartao,
    surfaceContainerHighest = Cartao,
    outline = Contorno,
    outlineVariant = Trilha
)

@Composable
fun ConstanciaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, typography = Typography, content = content)
}
