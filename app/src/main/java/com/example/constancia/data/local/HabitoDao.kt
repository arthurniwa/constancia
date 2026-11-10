package com.example.constancia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): a interface com as operações de banco de uma tabela.
 * Você só declara; o Room (via KSP) GERA a implementação na compilação.
 * ≈ um repositório do EF, só que o SQL é escrito por você e validado em tempo de compilação.
 */
@Dao
interface HabitoDao {

    // ---------- CREATE ----------
    // `suspend` ≈ método `async Task<long>`: roda sem travar a tela (Room usa uma thread de I/O).
    // Retorna o id gerado.
    @Insert
    suspend fun inserir(habito: Habito): Long

    // ---------- READ ----------
    // Retorna Flow (≈ IObservable / IAsyncEnumerable que nunca termina):
    // sempre que a tabela mudar, o Room emite a lista nova e a tela se atualiza sozinha.
    // Por isso não é `suspend`: o Flow já é assíncrono.
    @Query("SELECT * FROM habitos ORDER BY nome COLLATE NOCASE")
    fun observarTodos(): Flow<List<Habito>>

    // `:id` liga o parâmetro do método ao SQL (≈ @id no Dapper/SqlCommand).
    // `Habito?` porque o id pode não existir (ex.: acabou de ser excluído).
    @Query("SELECT * FROM habitos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Habito?>

    @Query("SELECT * FROM habitos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Habito?

    // ---------- UPDATE ----------
    @Update
    suspend fun atualizar(habito: Habito)

    // ---------- DELETE ----------
    // Os registros do hábito somem junto por causa do onDelete = CASCADE na FK.
    @Delete
    suspend fun excluir(habito: Habito)
}
