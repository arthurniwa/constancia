package com.example.constancia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // ABORT: e-mail repetido lança erro; o AuthRepository vira "E-mail já cadastrado".
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarPorId(id: Long): Flow<Usuario?>

    // ABORT: se o novo e-mail já for de outra conta, o índice único lança erro.
    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun atualizar(usuario: Usuario)
}
