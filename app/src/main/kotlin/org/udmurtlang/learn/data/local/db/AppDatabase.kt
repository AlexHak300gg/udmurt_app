package org.udmurtlang.learn.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TopicEntity::class,
        WordEntity::class,
        ProgressEntity::class,
        GameResultEntity::class,
        DailyActivityEntity::class,
        StreakEntity::class,
        AccountEntity::class,
        AchievementEntity::class,
        SyncQueueEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun progressDao(): ProgressDao
    abstract fun gameResultDao(): GameResultDao
    abstract fun activityDao(): ActivityDao
    abstract fun accountDao(): AccountDao
    abstract fun achievementDao(): AchievementDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        // v2: добавлена таблица медалей, без потери прогресса
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `achievements` (" +
                        "`slug` TEXT NOT NULL, `unlockedAt` INTEGER NOT NULL, PRIMARY KEY(`slug`))",
                )
            }
        }
    }
}
