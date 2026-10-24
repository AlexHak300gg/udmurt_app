package org.udmurtlang.learn.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.udmurtlang.learn.data.notifications.WordOfDayScheduler
import org.udmurtlang.learn.domain.notifications.NotificationScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(impl: WordOfDayScheduler): NotificationScheduler
}
