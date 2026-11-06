package org.udmurtlang.learn.data.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.ZonedDateTime

class WordOfDayScheduleTest {
    @Test
    fun keepsTodaysReminderWhenLessThanOneMinuteAway() {
        val now = ZonedDateTime.parse("2026-11-06T17:29:45+04:00")

        assertEquals(now.plusSeconds(15), nextWordOfDayTime(now, LocalTime.of(17, 30)))
    }

    @Test
    fun movesToTomorrowAfterTheSelectedTime() {
        val now = ZonedDateTime.parse("2026-11-06T23:59:59+04:00")
        val midnight = ZonedDateTime.parse("2026-11-07T00:00:00+04:00")

        assertEquals(midnight, nextWordOfDayTime(now, LocalTime.MIDNIGHT))
    }

    @Test
    fun keepsTheLocalHourAcrossADaylightSavingChange() {
        val now = ZonedDateTime.parse("2026-10-24T09:00:00+02:00[Europe/Berlin]")
        val nextMorning = ZonedDateTime.parse("2026-10-25T09:00:00+01:00[Europe/Berlin]")

        assertEquals(nextMorning, nextWordOfDayTime(now, LocalTime.of(9, 0)))
    }
}
