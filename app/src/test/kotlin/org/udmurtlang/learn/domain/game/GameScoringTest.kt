package org.udmurtlang.learn.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameScoringTest {

    @Test
    fun `score is points plus streak bonus`() {
        assertEquals(0, GameScoring.score(0, 0))
        assertEquals(10, GameScoring.score(1, 0))
        assertEquals(24, GameScoring.score(2, 2))
        assertEquals(80 + 8, GameScoring.score(8, 4))
    }

    @Test
    fun `stars depend on accuracy thresholds`() {
        assertEquals(3, GameScoring.stars(9, 10))
        assertEquals(3, GameScoring.stars(10, 10))
        assertEquals(2, GameScoring.stars(7, 10))
        assertEquals(1, GameScoring.stars(3, 10))
        assertEquals(0, GameScoring.stars(0, 0))
    }

    @Test
    fun `accuracy is safe for empty total`() {
        assertEquals(0f, GameScoring.accuracy(0, 0))
        assertEquals(0.5f, GameScoring.accuracy(1, 2))
    }
}
