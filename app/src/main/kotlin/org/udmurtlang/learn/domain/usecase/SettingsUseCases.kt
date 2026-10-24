package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.repository.SettingsRepository
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val settings: SettingsRepository,
) {
    operator fun invoke() = settings.settings
}

class UpdateSettingsUseCase @Inject constructor(
    private val settings: SettingsRepository,
) {
    suspend operator fun invoke(transform: (org.udmurtlang.learn.domain.model.AppSettings) -> org.udmurtlang.learn.domain.model.AppSettings) =
        settings.update(transform)
}
