package com.example.constancia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RegistroDiarioDao {

    // ---------- CREATE ----------
    // REPLACE: se já existe registro no mesmo dia, substitui (evita erro com toque duplo).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(registro: RegistroDiario): Long

    // ---------- READ ----------
    @Query("SELECT * FROM registros_diarios WHERE habitoId = :habitoId ORDER BY data DESC")
    fun observarPorHabito(habitoId: Long): Flow<List<RegistroDiario>>

    // Registros de um dia (tela "Hoje").
    @Query("SELECT * FROM registros_diarios WHERE data = :data")
    fun observarPorData(data: LocalDate): Flow<List<RegistroDiario>>

    // INNER JOIN com habitos, porque só lá está o usuarioId. Usado no gráfico da semana.
    @Query(
        """
        SELECT r.* FROM registros_diarios r
        INNER JOIN habitos h ON h.id = r.habitoId
        WHERE h.usuarioId = :usuarioId AND r.data BETWEEN :de AND :ate
        """
    )
    fun observarDoUsuarioEntre(usuarioId: Long, de: LocalDate, ate: LocalDate): Flow<List<RegistroDiario>>

    @Query("SELECT * FROM registros_diarios WHERE habitoId = :habitoId AND data = :data")
    suspend fun buscar(habitoId: Long, data: LocalDate): RegistroDiario?

    // ---------- UPDATE ----------
    @Update
    suspend fun atualizar(registro: RegistroDiario)

    // ---------- DELETE ----------
    @Delete
    suspend fun excluir(registro: RegistroDiario)
}
