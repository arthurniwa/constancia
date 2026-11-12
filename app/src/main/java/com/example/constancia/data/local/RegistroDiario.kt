package com.example.constancia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Tabela "registros_diarios": "no dia X, o hábito Y teve valor Z". 1 Habito → N registros. */
@Entity(
    tableName = "registros_diarios",
    foreignKeys = [
        ForeignKey(
            entity = Habito::class,
            parentColumns = ["id"],
            childColumns = ["habitoId"],
            onDelete = ForeignKey.CASCADE    // excluiu o hábito → apaga os registros dele
        )
    ],
    indices = [
        // Índice único: no máximo um registro por hábito por dia.
        Index(value = ["habitoId", "data"], unique = true)
    ]
)
data class RegistroDiario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitoId: Long,
    val data: LocalDate,
    val valor: Int = 1         // 1 = feito; com meta: a quantidade (ex.: 5 copos)
)
