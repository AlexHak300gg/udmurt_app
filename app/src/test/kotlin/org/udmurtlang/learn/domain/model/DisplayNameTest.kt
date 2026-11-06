package org.udmurtlang.learn.domain.model

import kotlin.test.assertEquals
import org.junit.Test

class DisplayNameTest {
    @Test
    fun `names through 32 characters are preserved`() {
        val name = "Ӟ".repeat(MAX_DISPLAY_NAME_LENGTH)
        assertEquals("Ӟечбур", "Ӟечбур".limitDisplayName())
        assertEquals(name, name.limitDisplayName())
    }

    @Test
    fun `an emoji at the limit is kept whole`() {
        val expected = "А".repeat(MAX_DISPLAY_NAME_LENGTH - 1) + "🐻"
        assertEquals(expected, (expected + "Б").limitDisplayName())
    }
}
