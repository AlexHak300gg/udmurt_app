package org.udmurtlang.learn.core.media

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Короткая тактильная обратная связь: успех / мягкая ошибка / тик. */
@Singleton
class Haptics @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun success() {
        vibrate(longArrayOf(0, 28, 40, 34), intArrayOf(0, 90, 0, 120))
    }

    fun softError() {
        vibrate(longArrayOf(0, 18, 60, 18), intArrayOf(0, 55, 0, 55))
    }

    fun tick() {
        vibrate(longArrayOf(0, 12), intArrayOf(0, 40))
    }

    private fun vibrate(pattern: LongArray, amplitudes: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}
