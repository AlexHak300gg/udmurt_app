package org.udmurtlang.learn.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface TimeProvider {
    fun nowMillis(): Long
    fun today(): LocalDate
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun today(): LocalDate =
        Instant.ofEpochMilli(nowMillis()).atZone(ZoneId.systemDefault()).toLocalDate()
}
