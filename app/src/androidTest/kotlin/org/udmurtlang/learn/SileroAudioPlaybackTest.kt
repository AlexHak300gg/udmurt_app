package org.udmurtlang.learn

import android.media.MediaPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.udmurtlang.learn.core.media.SpeechAudioAssets

/** Checks real Android WAV decoding and openFd access to packaged (uncompressed) assets. */
@RunWith(AndroidJUnit4::class)
class SileroAudioPlaybackTest {
    @Test
    fun allBundledRecordingsCanBePreparedByAndroid() {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val manifest = assets.open("${SpeechAudioAssets.DIRECTORY}/manifest.json")
            .bufferedReader().use { it.readText() }
        val items = Json.parseToJsonElement(manifest).jsonObject.getValue("items").jsonArray
        assertTrue(items.isNotEmpty())
        for (item in items) {
            val text = item.jsonObject.getValue("text").jsonPrimitive.content
            val player = MediaPlayer()
            try {
                assets.openFd(SpeechAudioAssets.path(text)).use { asset ->
                    player.setDataSource(asset.fileDescriptor, asset.startOffset, asset.length)
                }
                player.prepare()
                assertTrue("Android cannot decode recording: $text", player.duration > 0)
            } finally {
                player.release()
            }
        }
    }
}
