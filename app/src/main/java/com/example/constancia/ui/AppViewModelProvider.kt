package com.example.constancia.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.constancia.ConstanciaApplication
import com.example.constancia.ui.auth.AuthViewModel
import com.example.constancia.ui.detalhes.DetalhesViewModel
import com.example.constancia.ui.habitos.HabitoFormViewModel
import com.example.constancia.ui.habitos.HabitosViewModel
import com.example.constancia.ui.historico.HistoricoViewModel
import com.example.constancia.ui.hoje.HojeViewModel

/**
 * Fábrica de ViewModels: cria cada um passando os repositórios (como DI no .NET).
 * Sem ela o Android só cria ViewModels com construtor vazio.
 */
object AppViewModelProvider {   // `object` = singleton
    val Factory = viewModelFactory {
        initializer { AuthViewModel(app().container.authRepository) }
        initializer { HabitosViewModel(app().container.habitoRepository, usuarioLogado()) }
        initializer {
            HabitoFormViewModel(
                savedStateHandle = createSavedStateHandle(),   // traz o {id} da rota
                repository = app().container.habitoRepository,
                usuarioId = usuarioLogado()
            )
        }
        initializer {
            HojeViewModel(
                app().container.habitoRepository,
                app().container.registroRepository,
                app().container.feriadoRepository,
                usuarioLogado()
            )
        }
        initializer {
            HistoricoViewModel(
                savedStateHandle = createSavedStateHandle(),   // traz o {habitoId} da rota
                habitoRepository = app().container.habitoRepository,
                registroRepository = app().container.registroRepository,
                feriadoRepository = app().container.feriadoRepository
            )
        }
        initializer {
            DetalhesViewModel(
                savedStateHandle = createSavedStateHandle(),
                habitoRepository = app().container.habitoRepository,
                registroRepository = app().container.registroRepository,
                feriadoRepository = app().container.feriadoRepository
            )
        }
    }
}

/** Id do usuário logado; nunca é null aqui, pois essas telas só existem após o login. */
private fun CreationExtras.usuarioLogado(): Long =
    checkNotNull(app().container.sessaoRepository.usuarioId.value) { "Nenhum usuário logado" }

/** Pega a nossa Application (onde fica o container de repositórios). */
private fun CreationExtras.app(): ConstanciaApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ConstanciaApplication
