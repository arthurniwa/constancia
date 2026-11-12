package com.example.constancia.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta "esportiva escura": fundo quase preto, cartões grafite em camadas
 * e cores fortes reservadas para dados (progresso, sequência, feriados).
 * `0xFF...` = ARGB: FF é a opacidade (totalmente opaco), depois RR GG BB.
 */

// Fundos em camadas: quanto "mais alto" o elemento, mais claro o cinza.
val Fundo = Color(0xFF0A0C0F)
val Superficie = Color(0xFF12151A)
val Cartao = Color(0xFF1A1E24)
val CartaoAlto = Color(0xFF222731)
val Trilha = Color(0xFF2A3039)          // fundo dos anéis e barras (a parte "vazia")

// Texto
val TextoPrincipal = Color(0xFFECEFF3)
val TextoSecundario = Color(0xFF98A2AE)
val Contorno = Color(0xFF3A424D)

// Cores de destaque (dados)
val Azul = Color(0xFF14A9E6)            // cor principal do app (botões, aba selecionada)
val Verde = Color(0xFF5FD068)           // concluído
val Laranja = Color(0xFFFF8A3D)         // sequência (🔥)
val Ambar = Color(0xFFFFC23D)           // recorde e feriados
val Vermelho = Color(0xFFFF5C64)        // erros
