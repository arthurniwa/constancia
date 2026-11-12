package com.example.constancia.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Tabela "usuarios". A senha nunca é salva: só o hash PBKDF2 e o salt (ver SenhaHasher). */
@Entity(
    tableName = "usuarios",
    // E-mail único: o banco recusa dois cadastros com o mesmo e-mail.
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String,          // sempre em minúsculas
    val senhaHash: String,      // Base64
    val salt: String,           // Base64
    val dataCriacao: LocalDate = LocalDate.now()
)
