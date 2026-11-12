package com.example.constancia.data.repository

import com.example.constancia.data.local.RegistroDiario
import com.example.constancia.data.local.RegistroDiarioDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class RegistroRepository(private val registroDao: RegistroDiarioDao) {

    fun observarPorHabito(habitoId: Long): Flow<List<RegistroDiario>> =
        registroDao.observarPorHabito(habitoId)

    fun observarPorData(data: LocalDate): Flow<List<RegistroDiario>> =
        registroDao.observarPorData(data)

    fun observarDoUsuarioEntre(usuarioId: Long, de: LocalDate, ate: LocalDate): Flow<List<RegistroDiario>> =
        registroDao.observarDoUsuarioEntre(usuarioId, de, ate)

    suspend fun buscar(habitoId: Long, data: LocalDate): RegistroDiario? =
        registroDao.buscar(habitoId, data)

    suspend fun inserir(registro: RegistroDiario): Long = registroDao.inserir(registro)

    suspend fun atualizar(registro: RegistroDiario) = registroDao.atualizar(registro)

    suspend fun excluir(registro: RegistroDiario) = registroDao.excluir(registro)

    /**
     * Soma `delta` (+1 ou -1) ao valor do dia, usando o CRUD:
     * - não existe registro e delta > 0  → CREATE (valor 1)
     * - existe e o novo valor > 0         → UPDATE
     * - o novo valor chega a 0            → DELETE (dia volta a "não feito")
     * Lê o valor atual do banco (e não da tela) para não usar um dado desatualizado.
     */
    suspend fun ajustarValor(habitoId: Long, data: LocalDate, delta: Int) {
        val atual = registroDao.buscar(habitoId, data)
        val novoValor = (atual?.valor ?: 0) + delta
        when {
            atual == null && novoValor > 0 ->
                registroDao.inserir(RegistroDiario(habitoId = habitoId, data = data, valor = novoValor))
            atual != null && novoValor > 0 ->
                registroDao.atualizar(atual.copy(valor = novoValor))
            atual != null ->
                registroDao.excluir(atual)
        }
    }

    /** Para hábitos sem meta: marca (CREATE) ou desmarca (DELETE). */
    suspend fun alternarFeito(habitoId: Long, data: LocalDate) {
        val atual = registroDao.buscar(habitoId, data)
        if (atual == null) registroDao.inserir(RegistroDiario(habitoId = habitoId, data = data, valor = 1))
        else registroDao.excluir(atual)
    }
}
