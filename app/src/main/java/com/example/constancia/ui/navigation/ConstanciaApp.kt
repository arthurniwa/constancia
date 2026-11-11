package com.example.constancia.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.constancia.ui.detalhes.DetalhesScreen
import com.example.constancia.ui.habitos.HabitoFormScreen
import com.example.constancia.ui.habitos.HabitosScreen
import com.example.constancia.ui.historico.HistoricoScreen
import com.example.constancia.ui.hoje.HojeScreen

/**
 * Rotas do app (≈ os `@page "/habitos/{id}"` do Blazor).
 * `const val` = constante em tempo de compilação (≈ `const` do C#).
 */
object Rotas {
    const val HOJE = "hoje"
    const val HABITOS = "habitos"
    const val HABITO_FORM = "habito_form?id={id}"          // id opcional: sem id = criar
    fun habitoForm(id: Long = 0L) = "habito_form?id=$id"   // "$id" = interpolação (≈ $"...{id}")
    const val HISTORICO = "historico/{habitoId}"           // id obrigatório (faz parte do caminho)
    fun historico(habitoId: Long) = "historico/$habitoId"
    const val DETALHES = "detalhes/{habitoId}"
    fun detalhes(habitoId: Long) = "detalhes/$habitoId"
}

/** Item da barra inferior. */
private data class Aba(val rota: String, val titulo: String, val icone: ImageVector)

private val ABAS = listOf(
    Aba(Rotas.HOJE, "Hoje", Icons.Default.Home),
    Aba(Rotas.HABITOS, "Hábitos", Icons.AutoMirrored.Filled.List)
)

@Composable
fun ConstanciaApp(nomeUsuario: String, onSair: () -> Unit) {
    // NavController ≈ NavigationManager do Blazor: guarda a pilha de telas e navega.
    // `remember` mantém o mesmo objeto entre recomposições.
    val navController = rememberNavController()
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route

    Scaffold(
        bottomBar = {
            // Barra inferior só nas telas principais (no formulário ela some).
            if (ABAS.any { it.rota == rotaAtual }) {
                NavigationBar {
                    ABAS.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = {
                                navController.navigate(aba.rota) {
                                    // Evita empilhar várias cópias da mesma aba ao tocar repetidas vezes.
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = null) },
                            label = { Text(aba.titulo) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // NavHost = a "área" onde as telas aparecem (≈ <Router> + @Body do Blazor).
        NavHost(
            navController = navController,
            startDestination = Rotas.HOJE,
            // padding + consumeWindowInsets: as telas internas não somam de novo o espaço
            // da barra de status/navegação (evita espaço duplicado ou conteúdo escondido).
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            composable(Rotas.HOJE) {
                HojeScreen(
                    nomeUsuario = nomeUsuario,
                    onAbrir = { id -> navController.navigate(Rotas.detalhes(id)) }
                )
            }

            composable(Rotas.HABITOS) {
                HabitosScreen(
                    onSair = onSair,
                    onNovo = { navController.navigate(Rotas.habitoForm()) },
                    onAbrir = { id -> navController.navigate(Rotas.detalhes(id)) },
                    onEditar = { id -> navController.navigate(Rotas.habitoForm(id)) }
                )
            }

            composable(
                route = Rotas.HABITO_FORM,
                // Declara o argumento {id}: tipo Long, padrão 0 (= novo hábito).
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })
            ) {
                HabitoFormScreen(onVoltar = { navController.popBackStack() })  // volta para a tela anterior
            }

            composable(
                route = Rotas.HISTORICO,
                arguments = listOf(navArgument("habitoId") { type = NavType.LongType })
            ) {
                HistoricoScreen(onVoltar = { navController.popBackStack() })
            }

            composable(
                route = Rotas.DETALHES,
                arguments = listOf(navArgument("habitoId") { type = NavType.LongType })
            ) {
                DetalhesScreen(
                    onVoltar = { navController.popBackStack() },
                    onEditar = { id -> navController.navigate(Rotas.habitoForm(id)) },
                    onHistorico = { id -> navController.navigate(Rotas.historico(id)) }
                )
            }
        }
    }
}
