package org.udmurtlang.learn.data.assets

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ContentDto(
    val version: Int = 1,
    val topics: List<TopicDto> = emptyList(),
    val words: List<WordDto> = emptyList(),
)

@Serializable
data class TopicDto(
    val slug: String,
    val titleUdm: String,
    val titleRu: String,
    val color: String,
    val emoji: String,
    val sortOrder: Int,
)

@Serializable
data class WordDto(
    val topic: String,
    val udm: String,
    val ru: String,
    val transcription: String,
    val emoji: String,
    val exampleUdm: String? = null,
    val exampleRu: String? = null,
)

@Singleton
class ContentAssetLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    suspend fun load(): ContentDto = withContext(Dispatchers.IO) {
        val text = context.assets.open(CONTENT_PATH).bufferedReader().use { it.readText() }
        json.decodeFromString(text)
    }

    companion object {
        const val CONTENT_PATH = "content/content.json"
    }
}

fun ContentDto.toTopics(): List<Topic> = topics
    .sortedBy { it.sortOrder }
    .map {
        Topic(
            id = it.slug,
            slug = it.slug,
            titleUdm = it.titleUdm,
            titleRu = it.titleRu,
            color = it.color.removePrefix("#").toLong(16) or 0xFF000000,
            emoji = it.emoji,
            sortOrder = it.sortOrder,
        )
    }

fun ContentDto.toWords(): List<Word> {
    val topicIds = topics.map { it.slug }
    return words.mapIndexed { index, dto ->
        Word(
            id = "${dto.topic}_$index",
            topicId = dto.topic,
            udm = dto.udm,
            ru = dto.ru,
            transcription = dto.transcription,
            emoji = dto.emoji,
            exampleUdm = dto.exampleUdm,
            exampleRu = dto.exampleRu,
        )
    }.filter { it.topicId in topicIds }
}
