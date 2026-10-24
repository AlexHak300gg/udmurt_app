package org.udmurtlang.learn.data.account

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    private val hasher = PasswordHasher()

    @Test
    fun `hash matches own password`() {
        val stored = hasher.hash("bur-ki-udmurt")
        assertTrue(hasher.matches("bur-ki-udmurt", stored))
    }

    @Test
    fun `wrong password does not match`() {
        val stored = hasher.hash("secret1")
        assertFalse(hasher.matches("secret2", stored))
        assertFalse(hasher.matches("", stored))
    }

    @Test
    fun `same password yields different hashes due to salt`() {
        assertNotEquals(hasher.hash("same-pass"), hasher.hash("same-pass"))
    }

    @Test
    fun `malformed stored value never matches`() {
        assertFalse(hasher.matches("x", "not-a-hash"))
        assertFalse(hasher.matches("x", "pbkdf2|abc|zzz|zzz"))
        assertFalse(hasher.matches("x", ""))
    }
}
