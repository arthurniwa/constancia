package com.example.constancia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO: só declaramos; o Room gera a implementação na compilação (via KSP). */
@Dao
interface HabitoDao {

    // ---------- CREATE ----------
    // suspend (como async no C#): roda sem travar a tela. Retorna o id gerado.
    @Insert
    suspend fun inserir(habito: Habito): Long

    // ---------- READ ----------
    // Flow: quando a tabela muda, emite de novo e a tela se atualiza sozinha.
    @Query("SELECT * FROM habitos WHERE usuarioId = :usuarioId ORDER BY nome COLLATE NOCASE")
    fun observarDoUsuario(usuarioId: Long): Flow<List<Habito>>

    @Query("SELECT * FROM habitos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Habito?>

    @Query("SELECT * FROM habitos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Habito?

    // ---------- UPDATE ----------
    @Update
    suspend fun atualizar(habito: Habito)

    // ---------- DELETE ----------
    // Os registros do hábito somem junto (CASCADE).
    @Delete
    suspend fun excluir(habito: Habito)
}
