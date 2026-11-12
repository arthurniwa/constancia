package com.example.constancia.ui.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.constancia.domain.DiaResumo
import com.example.constancia.domain.EstadoDia
import com.example.constancia.ui.theme.Ambar
import com.example.constancia.ui.theme.EstiloNumero
import com.example.constancia.ui.theme.EstiloRotulo
import com.example.constancia.ui.theme.Trilha
import com.example.constancia.ui.util.nomeCurto
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Anel de progresso desenhado com Canvas + drawArc (≈ <canvas> do HTML).
 * animateFloatAsState faz o anel encher suavemente. arco 270 = medidor aberto.
 */
@Composable
fun AnelProgresso(
    progresso: Float,
    cor: Color,
    modifier: Modifier = Modifier,
    espessura: Dp = 6.dp,
    arco: Float = 360f,
    conteudo: @Composable BoxScope.() -> Unit = {}
) {
    // Valor animado (0,6 s) até o progresso novo.
    val animado by animateFloatAsState(progresso.coerceIn(0f, 1f), tween(600), label = "anel")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val traco = espessura.toPx()
            val lado = size.minDimension - traco                     // não corta a borda
            val canto = Offset((size.width - lado) / 2, (size.height - lado) / 2)
            // Ângulos: 0° = 3 horas; -90° = topo.
            val inicio = if (arco >= 360f) -90f else 90f + (360f - arco) / 2
            val estilo = Stroke(width = traco, cap = StrokeCap.Round)
            drawArc(Trilha, inicio, arco, useCenter = false, topLeft = canto, size = Size(lado, lado), style = estilo)
            if (animado > 0f) {
                drawArc(cor, inicio, arco * animado, useCenter = false, topLeft = canto, size = Size(lado, lado), style = estilo)
            }
        }
        conteudo()
    }
}

/** Cartão de métrica: rótulo, número grande e unidade. */
@Composable
fun CartaoMetrica(
    rotulo: String,
    valor: String,
    unidade: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(rotulo.uppercase(), style = EstiloRotulo, color = cor, maxLines = 1)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(valor, style = EstiloNumero.grande, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(4.dp))
                Text(
                    unidade, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }
    }
}

/** Título de seção em maiúsculas. */
@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Text(texto.uppercase(), style = EstiloRotulo, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
}

/** Barras dos últimos 7 dias, só com Box: altura = fillMaxHeight(fração). */
@Composable
fun GraficoSemana(dias: List<DiaResumo>, cor: Color, hoje: LocalDate, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dias.forEach { dia ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val trilha = when (dia.estado) {
                    EstadoDia.FERIADO -> Ambar.copy(alpha = 0.30f)
                    EstadoDia.NAO_PROGRAMADO, EstadoDia.FORA_DO_PERIODO -> Trilha.copy(alpha = 0.35f)
                    else -> Trilha
                }
                Box(
                    Modifier.fillMaxWidth().height(96.dp).background(trilha, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (dia.fracao > 0f) {
                        val preenchimento = if (dia.estado == EstadoDia.CONCLUIDO) cor else cor.copy(alpha = 0.5f)
                        Box(
                            Modifier.fillMaxWidth().fillMaxHeight(dia.fracao)
                                .background(preenchimento, RoundedCornerShape(6.dp))
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                val ehHoje = dia.data == hoje
                Text(
                    if (ehHoje) "Hoje" else dia.data.dayOfWeek.nomeCurto(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (ehHoje) FontWeight.Bold else FontWeight.Normal,
                    color = if (ehHoje) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/** Mapa do mês: grade de 7 colunas; a cor segue a regra da sequência. */
@Composable
fun MapaMes(dias: List<DiaResumo>, cor: Color, hoje: LocalDate, modifier: Modifier = Modifier) {
    if (dias.isEmpty()) return
    // Casas vazias antes do dia 1 (segunda = 1).
    val vazios = dias.first().data.dayOfWeek.value - 1
    val casas: List<DiaResumo?> = List(vazios) { null } + dias
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach {
                Text(
                    it.nomeCurto().take(1), Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // chunked(7): quebra a lista em semanas.
        casas.chunked(7).forEach { semana ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until 7).forEach { i ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        semana.getOrNull(i)?.let { BolinhaDia(it, cor, it.data == hoje) }
                    }
                }
            }
        }
        LegendaMapa(cor)
    }
}

@Composable
private fun BolinhaDia(dia: DiaResumo, cor: Color, ehHoje: Boolean) {
    val (fundo, textoCor) = when (dia.estado) {
        EstadoDia.CONCLUIDO -> cor to Color.Black
        EstadoDia.PARCIAL -> cor.copy(alpha = 0.35f) to MaterialTheme.colorScheme.onSurface
        EstadoDia.FALHA -> Trilha to MaterialTheme.colorScheme.onSurfaceVariant
        EstadoDia.FERIADO -> Ambar.copy(alpha = 0.25f) to Ambar
        EstadoDia.HOJE_PENDENTE -> Color.Transparent to MaterialTheme.colorScheme.onSurface
        EstadoDia.NAO_PROGRAMADO, EstadoDia.FORA_DO_PERIODO ->
            Color.Transparent to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    var m = Modifier.fillMaxWidth().aspectRatio(1f).background(fundo, CircleShape)
    if (ehHoje) m = m.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    Box(m, contentAlignment = Alignment.Center) {
        Text("${dia.data.dayOfMonth}", style = MaterialTheme.typography.labelSmall, color = textoCor)
    }
}

@Composable
private fun LegendaMapa(cor: Color) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        ItemLegenda(cor, "Feito")
        ItemLegenda(Trilha, "Não feito")
        ItemLegenda(Ambar.copy(alpha = 0.5f), "Feriado")
    }
}

@Composable
private fun ItemLegenda(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(cor, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
