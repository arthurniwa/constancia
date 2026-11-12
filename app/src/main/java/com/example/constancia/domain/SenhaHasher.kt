package com.example.constancia.domain

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Senhas: nunca o texto, só um hash PBKDF2 (lento de propósito) com salt aleatório.
 * Salt: mesma senha → hashes diferentes por usuário.
 */
object SenhaHasher {
    private const val ITERACOES = 120_000
    private const val TAMANHO_BITS = 256

    fun gerarSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(senha: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(senha.toCharArray(), salt, ITERACOES, TAMANHO_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    /** Recalcula o hash com o MESMO salt e compara com o salvo. */
    fun confere(senha: String, salt: ByteArray, hashSalvo: ByteArray): Boolean =
        MessageDigest.isEqual(hash(senha, salt), hashSalvo)
}
