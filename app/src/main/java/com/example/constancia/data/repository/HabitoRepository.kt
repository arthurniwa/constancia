package com.example.constancia.data.repository

import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.HabitoDao
import kotlinx.coroutines.flow.Flow

/** Repository: os ViewModels pedem dados aqui, sem saber que vêm do Room. */
class HabitoRepository(private val habitoDao: HabitoDao) {

    fun observarDoUsuario(usuarioId: Long): Flow<List<Habito>> = habitoDao.observarDoUsuario(usuarioId)

    fun observarPorId(id: Long): Flow<Habito?> = habitoDao.observarPorId(id)

    suspend fun buscarPorId(id: Long): Habito? = habitoDao.buscarPorId(id)

    suspend fun inserir(habito: Habito): Long = habitoDao.inserir(habito)

    suspend fun atualizar(habito: Habito) = habitoDao.atualizar(habito)

    suspend fun excluir(habito: Habito) = habitoDao.excluir(habito)
}
