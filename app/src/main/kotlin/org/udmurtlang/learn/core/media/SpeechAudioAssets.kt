package org.udmurtlang.learn.core.media

import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale

/** Shared filename contract with tools/generate_silero_audio.py; independent of word IDs. */
object SpeechAudioAssets {
    const val DIRECTORY = "audio/silero"
    private val whitespace = Regex("[\\s\\p{Z}]+")

    fun key(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFC)
        .lowercase(Locale.ROOT)
        .trim()
        .replace(whitespace, " ")

    fun path(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key(text).toByteArray(Charsets.UTF_8))
        val filename = digest.joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return "$DIRECTORY/$filename.wav"
    }
}
