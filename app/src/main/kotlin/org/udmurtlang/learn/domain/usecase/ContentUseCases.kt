package org.udmurtlang.learn.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.TopicProgress
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import javax.inject.Inject

class ObserveTopicsUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    operator fun invoke(): Flow<List<TopicProgress>> = flow {
        val topics = content.topics()
        val wordsByTopic = content.allWords().groupBy { it.topicId }
        emitAll(progress.observeProgress().map { entries ->
            val known = entries.filter { it.status == ProgressStatus.KNOWN }.mapTo(hashSetOf()) { it.wordId }
            topics.map { topic ->
                val words = wordsByTopic[topic.id].orEmpty()
                TopicProgress(topic, words.count { it.id in known }, words.size)
            }
        })
    }
}

class GetTopicWordsUseCase @Inject constructor(
    private val content: ContentRepository,
) {
    suspend operator fun invoke(topicId: String) = content.words(topicId)
}

class GetTopicUseCase @Inject constructor(
    private val content: ContentRepository,
) {
    suspend operator fun invoke(topicId: String) = content.topics().firstOrNull { it.id == topicId }
}

class GetWordUseCase @Inject constructor(
    private val content: ContentRepository,
) {
    suspend operator fun invoke(wordId: String) = content.word(wordId)
}
