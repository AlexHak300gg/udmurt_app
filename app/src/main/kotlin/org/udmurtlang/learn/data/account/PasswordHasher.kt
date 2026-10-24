package org.udmurtlang.learn.data.account

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/** Пароли хранятся только хешем PBKDF2; открытого пароля в коде и БД нет. */
@Singleton
class PasswordHasher @Inject constructor() {

    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
        val iterations = pbkdf2(password.toCharArray(), salt, ITERATIONS)
        return listOf(
            ALGORITHM_TAG,
            ITERATIONS.toString(),
            encode(salt),
            encode(iterations),
        ).joinToString(SEPARATOR)
    }

    fun matches(password: String, stored: String): Boolean {
        val parts = stored.split(SEPARATOR)
        if (parts.size != 4 || parts[0] != ALGORITHM_TAG) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = decode(parts[2]) ?: return false
        val expected = decode(parts[3]) ?: return false
        val actual = pbkdf2(password.toCharArray(), salt, iterations)
        return actual.contentEquals(expected)
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
    }

    private fun encode(bytes: ByteArray): String =
        Base64.getEncoder().withoutPadding().encodeToString(bytes)

    private fun decode(value: String): ByteArray? = try {
        Base64.getDecoder().decode(value)
    } catch (_: IllegalArgumentException) {
        null
    }

    private companion object {
        const val ALGORITHM_TAG = "pbkdf2"
        const val ITERATIONS = 120_000
        const val SALT_BYTES = 16
        const val KEY_BITS = 256
        const val SEPARATOR = "$"
    }
}
