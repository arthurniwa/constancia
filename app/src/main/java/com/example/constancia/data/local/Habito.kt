package com.example.constancia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tabela "habitos".
 *
 * `data class` ≈ `record` do C#: o compilador gera equals/hashCode/toString/copy.
 * `@Entity` ≈ uma classe mapeada no DbContext do Entity Framework (vira tabela).
 */
@Entity(
    tableName = "habitos",
    // Cada hábito pertence a um usuário (1 Usuario → N Habitos).
    // CASCADE: se o usuário for apagado, os hábitos dele (e, em cadeia, os registros) somem junto.
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]   // acelera "SELECT ... WHERE usuarioId = ?"
)
data class Habito(
    // `val` = propriedade somente leitura (≈ `init` / readonly). `var` seria mutável.
    // autoGenerate = true ≈ IDENTITY no SQL Server. O valor 0 significa "ainda não salvo".
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Dono do hábito (chave estrangeira para usuarios.id).
    val usuarioId: Long,

    val nome: String,

    // Um emoji (ex.: "💧"). Simples de salvar e de mostrar na tela.
    val icone: String,

    // Cor no formato ARGB (Int), ex.: 0xFF4CAF50.toInt(). Na tela vira Color(cor).
    val cor: Int,

    // Room não sabe salvar Set<DayOfWeek>: o Converters.kt transforma em texto "1,3,5".
    val diasDaSemana: Set<DayOfWeek>,

    // `Int?` = pode ser nulo (≈ `int?` no C#). Em Kotlin, `Int` sem `?` NUNCA aceita null:
    // o compilador barra. Null = hábito do tipo "feito/não feito"; com valor = meta numérica
    // (ex.: 8 copos de água).
    val meta: Int? = null,

    // Opcional: só usado se der tempo de fazer os lembretes (extra).
    val horarioLembrete: LocalTime? = null,

    // Valor padrão de parâmetro (≈ parâmetro opcional do C#).
    val dataCriacao: LocalDate = LocalDate.now()
) {
    /** Quanto o usuário precisa registrar no dia para o hábito contar como feito. */
    val alvoDiario: Int
        get() = meta ?: 1   // `?:` (Elvis) ≈ `??` do C#: se meta for null, usa 1.

    /** O hábito está programado para esse dia da semana? */
    fun programadoPara(data: LocalDate): Boolean = data.dayOfWeek in diasDaSemana
}
