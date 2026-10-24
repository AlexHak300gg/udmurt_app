package org.udmurtlang.learn.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.udmurtlang.learn.data.repository.AchievementRepositoryImpl
import org.udmurtlang.learn.data.repository.ActivityRepositoryImpl
import org.udmurtlang.learn.data.repository.ContentRepositoryImpl
import org.udmurtlang.learn.data.repository.GameRepositoryImpl
import org.udmurtlang.learn.data.repository.ProgressRepositoryImpl
import org.udmurtlang.learn.data.repository.SettingsRepositoryImpl
import org.udmurtlang.learn.data.repository.SyncQueueRepositoryImpl
import org.udmurtlang.learn.data.repository.WordOfDayRepositoryImpl
import org.udmurtlang.learn.domain.repository.AchievementRepository
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import org.udmurtlang.learn.domain.repository.SettingsRepository
import org.udmurtlang.learn.domain.repository.SyncQueueRepository
import org.udmurtlang.learn.domain.repository.WordOfDayRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindWordOfDayRepository(impl: WordOfDayRepositoryImpl): WordOfDayRepository

    @Binds
    @Singleton
    abstract fun bindSyncQueueRepository(impl: SyncQueueRepositoryImpl): SyncQueueRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository
}
