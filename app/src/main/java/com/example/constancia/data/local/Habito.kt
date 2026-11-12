package com.example.constancia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tabela "habitos". `data class` (como record no C#) gera equals/toString/copy.
 * Cada hábito pertence a um usuário.
 */
@Entity(
    tableName = "habitos",
    // CASCADE: apagou o usuário → apaga os hábitos (e os registros) dele.
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Habito(
    // 0 = ainda não salvo; o banco gera o id.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val usuarioId: Long,

    val nome: String,

    val icone: String,   // um emoji

    val cor: Int,        // cor ARGB

    // Salvo como texto "1,3,5" pelo Converters.
    val diasDaSemana: Set<DayOfWeek>,

    // null = hábito feito/não feito; com valor = meta numérica (ex.: 8 copos).
    val meta: Int? = null,

    val horarioLembrete: LocalTime? = null,

    val dataCriacao: LocalDate = LocalDate.now()
) {
    /** Quanto precisa registrar no dia para contar como feito. */
    val alvoDiario: Int
        get() = meta ?: 1

    /** O hábito está programado para esse dia da semana? */
    fun programadoPara(data: LocalDate): Boolean = data.dayOfWeek in diasDaSemana
}
