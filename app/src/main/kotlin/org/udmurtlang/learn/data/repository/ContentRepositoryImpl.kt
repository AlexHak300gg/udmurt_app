package org.udmurtlang.learn.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.udmurtlang.learn.data.assets.ContentAssetLoader
import org.udmurtlang.learn.data.assets.toTopics
import org.udmurtlang.learn.data.assets.toWords
import org.udmurtlang.learn.data.local.AppDataStore
import org.udmurtlang.learn.data.local.db.ContentDao
import org.udmurtlang.learn.data.local.db.TopicEntity
import org.udmurtlang.learn.data.local.db.WordEntity
import org.udmurtlang.learn.data.mapper.toDomain
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.repository.ContentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val loader: ContentAssetLoader,
    private val dataStore: AppDataStore,
) : ContentRepository {

    private val seedMutex = Mutex()
    @Volatile private var seeded = false

    override suspend fun topics(): List<Topic> {
        ensureSeeded()
        return contentDao.topics().map { it.toDomain() }
    }

    override suspend fun words(topicId: String): List<Word> {
        ensureSeeded()
        return contentDao.words(topicId).map { it.toDomain() }
    }

    override suspend fun word(wordId: String): Word? {
        ensureSeeded()
        return contentDao.word(wordId)?.toDomain()
    }

    override suspend fun allWords(): List<Word> {
        ensureSeeded()
        return contentDao.allWords().map { it.toDomain() }
    }

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            val dto = loader.load()
            if (dataStore.contentVersion() < dto.version || !contentDao.hasWords()) {
                val topics = dto.toTopics().map {
                    TopicEntity(it.id, it.slug, it.titleUdm, it.titleRu, it.color.toHex(), it.emoji, it.sortOrder)
                }
                val words = dto.toWords().mapIndexed { index, word ->
                    WordEntity(
                        id = word.id,
                        topicId = word.topicId,
                        udm = word.udm,
                        ru = word.ru,
                        transcription = word.transcription,
                        emoji = word.emoji,
                        exampleUdm = word.exampleUdm,
                        exampleRu = word.exampleRu,
                        sortOrder = index,
                    )
                }
                contentDao.seedContent(topics, words)
                dataStore.setContentVersion(dto.version)
            }
            seeded = true
        }
    }

    private fun Long.toHex(): String = "#%06X".format(this and 0xFFFFFF)
}
