package com.example.constancia.domain

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Guarda senhas do jeito certo: nunca o texto, só um HASH lento e com SALT.
 * ≈ Rfc2898DeriveBytes / PasswordHasher do ASP.NET Identity (que também usa PBKDF2).
 *
 * - Hash: função de mão única. Dá para conferir uma senha, mas não dá para "desfazer" o hash.
 * - Salt: 16 bytes aleatórios por usuário. Duas pessoas com a senha "123456" ficam com hashes
 *   diferentes, e tabelas prontas de hashes (rainbow tables) não servem.
 * - Iterações: repete o cálculo milhares de vezes para deixar ataques de força bruta lentos.
 */
object SenhaHasher {
    private const val ITERACOES = 120_000
    private const val TAMANHO_BITS = 256

    fun gerarSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(senha: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(senha.toCharArray(), salt, ITERACOES, TAMANHO_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    /** Recalcula o hash da senha digitada com o MESMO salt e compara com o salvo. */
    fun confere(senha: String, salt: ByteArray, hashSalvo: ByteArray): Boolean =
        // isEqual compara em tempo constante (não "vaza" quantos bytes acertou).
        MessageDigest.isEqual(hash(senha, salt), hashSalvo)
}
