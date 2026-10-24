package org.udmurtlang.learn

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.udmurtlang.learn.data.di.DatabaseModule
import org.udmurtlang.learn.data.local.db.AccountDao
import org.udmurtlang.learn.data.local.db.AchievementDao
import org.udmurtlang.learn.data.local.db.ActivityDao
import org.udmurtlang.learn.data.local.db.AppDatabase
import org.udmurtlang.learn.data.local.db.ContentDao
import org.udmurtlang.learn.data.local.db.GameResultDao
import org.udmurtlang.learn.data.local.db.ProgressDao
import org.udmurtlang.learn.data.local.db.SyncQueueDao
import javax.inject.Singleton

/** In-memory Room вместо файла БД ради изоляции тестов. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    @Provides fun provideContentDao(db: AppDatabase): ContentDao = db.contentDao()
    @Provides fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
    @Provides fun provideGameResultDao(db: AppDatabase): GameResultDao = db.gameResultDao()
    @Provides fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()
    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}
