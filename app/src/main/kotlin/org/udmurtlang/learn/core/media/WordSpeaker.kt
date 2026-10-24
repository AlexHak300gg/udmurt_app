package org.udmurtlang.learn.core.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Offline playback of bundled Silero udm_bogdan recordings. No system TTS or network. */
@Singleton
class WordSpeaker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private var playbackJob: Job? = null
    private var prepared = false
    private var volume = 1f

    init {
        scope.launch {
            settings.settings.collect { current ->
                volume = current.ttsVolume.coerceIn(0f, 1f)
                if (!current.soundEnabled || current.ttsVolume <= 0f) {
                    stop()
                } else if (prepared) {
                    player?.setVolume(volume, volume)
                }
            }
        }
    }

    @MainThread
    fun speak(text: String) {
        stop()
        if (text.isBlank()) return
        playbackJob = scope.launch {
            val current = settings.settings.first()
            if (!current.soundEnabled || current.ttsVolume <= 0f) return@launch
            volume = current.ttsVolume.coerceIn(0f, 1f)
            val next = MediaPlayer()
            player = next
            try {
                next.setAudioAttributes(attributes)
                next.setOnCompletionListener { completed -> release(completed) }
                next.setOnErrorListener { failed, what, extra ->
                    Log.w(TAG, "Speech playback error: $what/$extra")
                    release(failed)
                    true
                }
                next.setOnPreparedListener { ready ->
                    if (player === ready) {
                        prepared = true
                        ready.setVolume(volume, volume)
                        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .setAudioAttributes(attributes)
                            .setWillPauseWhenDucked(true)
                            .setOnAudioFocusChangeListener({ change ->
                                if (change != AudioManager.AUDIOFOCUS_GAIN) release(ready)
                            }, Handler(Looper.getMainLooper()))
                            .build()
                        focusRequest = request
                        if (audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            try {
                                ready.start()
                            } catch (error: IllegalStateException) {
                                Log.w(TAG, "Cannot start speech playback", error)
                                release(ready)
                            }
                        } else {
                            release(ready)
                        }
                    }
                }
                context.assets.openFd(SpeechAudioAssets.path(text)).use { asset ->
                    next.setDataSource(asset.fileDescriptor, asset.startOffset, asset.length)
                }
                next.prepareAsync()
            } catch (error: CancellationException) {
                release(next)
                throw error
            } catch (error: IOException) {
                Log.w(TAG, "Missing or unreadable Silero audio for: $text", error)
                release(next)
            } catch (error: IllegalArgumentException) {
                Log.w(TAG, "Invalid speech audio for: $text", error)
                release(next)
            } catch (error: IllegalStateException) {
                Log.w(TAG, "Cannot prepare speech playback", error)
                release(next)
            }
        }
    }

    /** Cancels pending requests as well as playback, e.g. when the activity leaves the foreground. */
    @MainThread
    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        player?.let(::release)
    }

    private fun release(expected: MediaPlayer) {
        // A late callback from an older request must not interrupt a newer word.
        if (player !== expected) return
        player = null
        prepared = false
        expected.release()
        focusRequest?.let(audioManager::abandonAudioFocusRequest)
        focusRequest = null
    }

    private companion object {
        const val TAG = "WordSpeaker"
    }
}
