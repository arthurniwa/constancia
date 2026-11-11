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
 * Fábrica de ViewModels: ensina o Android a criar cada ViewModel com suas dependências
 * (o repositório). Sem ela, o Android só sabe criar ViewModels com construtor vazio.
 * ≈ o container de DI resolvendo o construtor de um Controller.
 */
object AppViewModelProvider {   // `object` = singleton (≈ classe static)
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

/**
 * Id do usuário logado. As telas de hábitos só existem depois do login
 * (ver RaizApp), então aqui ele nunca é null — `checkNotNull` deixa isso explícito.
 */
private fun CreationExtras.usuarioLogado(): Long =
    checkNotNull(app().container.sessaoRepository.usuarioId.value) { "Nenhum usuário logado" }

/** Pega a nossa classe Application de dentro do contexto de criação do ViewModel. */
private fun CreationExtras.app(): ConstanciaApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ConstanciaApplication
