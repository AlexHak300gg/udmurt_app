package org.udmurtlang.learn.core.media

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Короткие SFX генерируются программно — не нужны аудиоассеты. */
@Singleton
class SfxPlayer @Inject constructor(
    private val settings: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tone: ToneGenerator? = null
    private var toneVolume = -1

    fun playSuccess() = play(listOf(ToneGenerator.TONE_PROP_ACK to 140))

    fun playError() = play(listOf(ToneGenerator.TONE_PROP_NACK to 160))

    fun playClick() = play(listOf(ToneGenerator.TONE_PROP_BEEP2 to 60))

    fun playFanfare() =
        play(listOf(ToneGenerator.TONE_PROP_ACK to 120, ToneGenerator.TONE_PROP_BEEP2 to 160))

    private fun player(volume: Int): ToneGenerator {
        val existing = tone
        if (existing == null || toneVolume != volume) {
            existing?.release()
            tone = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
            toneVolume = volume
        }
        return checkNotNull(tone)
    }

    private fun play(notes: List<Pair<Int, Int>>) {
        scope.launch {
            val current = settings.settings.first()
            if (!current.soundEnabled) return@launch
            val volume = (current.ttsVolume.coerceIn(0f, 1f) * 100).toInt().coerceIn(0, 100)
            val generator = player(volume)
            notes.forEach { (toneType, duration) ->
                generator.startTone(toneType, duration)
            }
        }
    }
}
