package com.example.constancia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Tabela "registros_diarios": cada linha diz "no dia X, o hábito Y teve valor Z".
 *
 * Relacionamento 1:N (um Habito tem vários RegistroDiario), como um
 * HasMany/WithOne no Entity Framework.
 */
@Entity(
    tableName = "registros_diarios",
    foreignKeys = [
        ForeignKey(
            entity = Habito::class,          // `Habito::class` ≈ `typeof(Habito)` no C#
            parentColumns = ["id"],          // coluna na tabela pai (habitos.id)
            childColumns = ["habitoId"],     // coluna nesta tabela
            onDelete = ForeignKey.CASCADE    // excluiu o hábito → o SQLite apaga os registros dele
        )
    ],
    indices = [
        // Índice único: impede dois registros do mesmo hábito no mesmo dia.
        // Também acelera as buscas por habitoId (o Room avisa se a FK não tiver índice).
        Index(value = ["habitoId", "data"], unique = true)
    ]
)
data class RegistroDiario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitoId: Long,
    val data: LocalDate,       // salvo como número (dias desde 1970) pelo Converters.kt
    val valor: Int = 1         // hábito simples: 1 = feito; com meta: quantidade (ex.: 5 copos)
)
