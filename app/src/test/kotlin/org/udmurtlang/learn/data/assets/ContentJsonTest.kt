package org.udmurtlang.learn.data.assets

import kotlinx.serialization.json.Json
import org.udmurtlang.learn.core.media.SpeechAudioAssets
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentJsonTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun loadDto(): ContentDto {
        val file = File("src/main/assets/content/content.json")
        assertTrue(file.exists(), "content.json not found at ${file.absolutePath}")
        return json.decodeFromString(file.readText())
    }

    @Test
    fun `content has expected shape`() {
        val dto = loadDto()
        assertEquals(13, dto.topics.size)
        assertEquals(104, dto.words.size)
        assertTrue(dto.topics.all { it.titleUdm.isNotBlank() && it.emoji.isNotBlank() })
        assertTrue(dto.words.all { it.udm.isNotBlank() && it.ru.isNotBlank() && it.transcription.isNotBlank() })
    }

    @Test
    fun `topics slugs are unique and words reference known topics`() {
        val dto = loadDto()
        val slugs = dto.topics.map { it.slug }
        assertEquals(slugs.distinct(), slugs)
        val known = slugs.toSet()
        assertTrue(dto.words.all { it.topic in known })
        assertTrue(slugs.all { slug -> dto.words.count { it.topic == slug } >= 3 })
    }

    @Test
    fun `every word and example has a bundled recording`() {
        val assets = File("src/main/assets")
        val texts = loadDto().words.flatMap { word -> listOfNotNull(word.udm, word.exampleUdm) }
        for (text in texts.filter(String::isNotBlank).distinct()) {
            val audio = assets.resolve(SpeechAudioAssets.path(text))
            assertTrue(audio.isFile && audio.length() > 44, "Missing WAV recording for $text")
        }
    }

    @Test
    fun `mapping produces domain topics and words`() {
        val dto = loadDto()
        val topics = dto.toTopics()
        val words = dto.toWords()
        assertEquals(13, topics.size)
        assertEquals(104, words.size)
        assertTrue(topics.all { it.color != 0L })
        assertTrue(words.map { it.id }.distinct().size == words.size)
        assertTrue(words.all { w -> topics.any { it.id == w.topicId } })
    }

    @Test
    fun `corrected words use the expected spellings and meanings`() {
        val words = loadDto().words
        val expected = mapOf(
            "Умоесь" to "здравствуйте",
            "Ӝук" to "каша",
            "Сӥль" to "мясо",
            "Кар" to "город",
            "Шур" to "река",
            "Книга" to "книга",
            "Гожъяны" to "писать",
        )
        for ((udm, ru) in expected) {
            assertEquals(ru, words.single { it.udm == udm }.ru, udm)
        }
    }
}
