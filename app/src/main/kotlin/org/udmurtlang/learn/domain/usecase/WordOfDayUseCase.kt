package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.repository.WordOfDayRepository
import javax.inject.Inject

class GetWordOfDayUseCase @Inject constructor(
    private val repository: WordOfDayRepository,
) {
    suspend operator fun invoke() = repository.wordOfDay()
}
