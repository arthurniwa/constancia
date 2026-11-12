package com.example.constancia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.ui.text.font.Typeface as ComposeTypeface

/**
 * Fonte condensada que já vem no Android ("sans-serif-condensed"): números altos e estreitos,
 * típicos de apps de esporte. Não precisa baixar nada.
 * `import ... as ...` renomeia na importação (≈ `using Alias = Namespace.Tipo;` do C#).
 */
val FonteNumeros = FontFamily(ComposeTypeface(AndroidTypeface.create("sans-serif-condensed", AndroidTypeface.BOLD)))

/** Estilos extras para métricas (o Material não tem um "número gigante"). */
object EstiloNumero {
    val gigante = TextStyle(fontFamily = FonteNumeros, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 46.sp)
    val grande = TextStyle(fontFamily = FonteNumeros, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 34.sp)
    val medio = TextStyle(fontFamily = FonteNumeros, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 22.sp)
}

/** Rótulos pequenos em maiúsculas acima dos números ("SEQUÊNCIA", "RECORDE"). */
val EstiloRotulo = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.2.sp)

val Typography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp)
)
