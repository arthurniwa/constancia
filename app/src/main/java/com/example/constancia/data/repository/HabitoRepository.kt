package com.example.constancia.data.repository

import com.example.constancia.data.local.Habito
import com.example.constancia.data.local.HabitoDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository: a "porta de entrada" dos dados para os ViewModels.
 * O ViewModel não sabe se o dado vem do Room, da API ou de cache — só fala com o repositório.
 * ≈ a camada de Repository/Service que você usaria numa API .NET entre o Controller e o DbContext.
 *
 * `private val habitoDao` no construtor = construtor primário: declara o parâmetro E o campo
 * de uma vez (≈ primary constructor do C# 12).
 */
class HabitoRepository(private val habitoDao: HabitoDao) {

    fun observarDoUsuario(usuarioId: Long): Flow<List<Habito>> = habitoDao.observarDoUsuario(usuarioId)

    fun observarPorId(id: Long): Flow<Habito?> = habitoDao.observarPorId(id)

    suspend fun buscarPorId(id: Long): Habito? = habitoDao.buscarPorId(id)

    suspend fun inserir(habito: Habito): Long = habitoDao.inserir(habito)

    suspend fun atualizar(habito: Habito) = habitoDao.atualizar(habito)

    suspend fun excluir(habito: Habito) = habitoDao.excluir(habito)
}
