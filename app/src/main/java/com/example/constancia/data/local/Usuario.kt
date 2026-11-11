package com.example.constancia.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Tabela "usuarios". A senha NUNCA é salva: guardamos só o hash (resultado do PBKDF2)
 * e o salt (bytes aleatórios usados no hash). Ver SenhaHasher.kt.
 * ≈ a tabela AspNetUsers do ASP.NET Identity (que também guarda só o PasswordHash).
 */
@Entity(
    tableName = "usuarios",
    // E-mail único: o banco recusa dois cadastros com o mesmo e-mail.
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String,          // sempre salvo em minúsculas, sem espaços
    val senhaHash: String,      // Base64 do hash
    val salt: String,           // Base64 do salt
    val dataCriacao: LocalDate = LocalDate.now()
)
