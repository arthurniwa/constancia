package com.example.constancia

import com.example.constancia.domain.SenhaHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SenhaHasherTest {

    @Test
    fun `senha correta confere e senha errada nao`() {
        val salt = SenhaHasher.gerarSalt()
        val hash = SenhaHasher.hash("minhaSenha123", salt)
        assertTrue(SenhaHasher.confere("minhaSenha123", salt, hash))
        assertFalse(SenhaHasher.confere("minhaSenha124", salt, hash))
    }

    @Test
    fun `mesma senha com salts diferentes gera hashes diferentes`() {
        val h1 = SenhaHasher.hash("123456", SenhaHasher.gerarSalt())
        val h2 = SenhaHasher.hash("123456", SenhaHasher.gerarSalt())
        assertFalse(h1.contentEquals(h2))
    }
}
