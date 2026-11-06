package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.model.TopicProgress
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import javax.inject.Inject

class GetTopicsUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) {
    suspend operator fun invoke(): List<TopicProgress> {
        val topics = content.topics()
        val known = progress.knownWordIds()
        val wordsByTopic = content.allWords().groupBy { it.topicId }
        return topics.map { topic ->
            val words = wordsByTopic[topic.id].orEmpty()
            TopicProgress(topic, words.count { it.id in known }, words.size)
        }
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
