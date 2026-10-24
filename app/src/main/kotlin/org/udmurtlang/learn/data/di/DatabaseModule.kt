package org.udmurtlang.learn.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.udmurtlang.learn.core.common.DefaultDispatcherProvider
import org.udmurtlang.learn.core.common.DispatcherProvider
import org.udmurtlang.learn.core.common.SystemTimeProvider
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.data.local.db.AccountDao
import org.udmurtlang.learn.data.local.db.AchievementDao
import org.udmurtlang.learn.data.local.db.ActivityDao
import org.udmurtlang.learn.data.local.db.AppDatabase
import org.udmurtlang.learn.data.local.db.ContentDao
import org.udmurtlang.learn.data.local.db.GameResultDao
import org.udmurtlang.learn.data.local.db.ProgressDao
import org.udmurtlang.learn.data.local.db.SyncQueueDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "zechebur.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideContentDao(db: AppDatabase): ContentDao = db.contentDao()
    @Provides fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
    @Provides fun provideGameResultDao(db: AppDatabase): GameResultDao = db.gameResultDao()
    @Provides fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()
    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideDispatchers(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideTime(): TimeProvider = SystemTimeProvider()
}
