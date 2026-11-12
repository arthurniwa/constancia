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
    // REPLACE: se já existir registro do mesmo hábito no mesmo dia (índice único), substitui
    // em vez de lançar erro — proteção contra toques duplos muito rápidos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(registro: RegistroDiario): Long

    // ---------- READ ----------
    // Histórico de um hábito, do mais recente para o mais antigo.
    @Query("SELECT * FROM registros_diarios WHERE habitoId = :habitoId ORDER BY data DESC")
    fun observarPorHabito(habitoId: Long): Flow<List<RegistroDiario>>

    // Todos os registros de um dia (usado na tela "Hoje").
    // O Converters transforma o LocalDate do parâmetro em Long automaticamente.
    @Query("SELECT * FROM registros_diarios WHERE data = :data")
    fun observarPorData(data: LocalDate): Flow<List<RegistroDiario>>

    // Registros de TODOS os hábitos de um usuário num intervalo de datas (gráfico "Sua semana").
    // INNER JOIN: registros_diarios não tem usuarioId; quem sabe o dono é a tabela habitos.
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
