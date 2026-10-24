package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import javax.inject.Inject

class MarkAnswerUseCase @Inject constructor(
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
) {
    suspend operator fun invoke(wordId: String, correct: Boolean) {
        progress.markAnswer(wordId, correct)
        activity.touchToday()
    }
}

class MarkWordKnownUseCase @Inject constructor(
    private val progress: ProgressRepository,
    private val activity: ActivityRepository,
) {
    suspend operator fun invoke(wordId: String) {
        progress.markKnown(wordId)
        activity.registerWordsLearned(1)
        activity.touchToday()
    }
}
