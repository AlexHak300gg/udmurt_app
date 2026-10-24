package org.udmurtlang.learn.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.udmurtlang.learn.core.media.FeedbackController
import org.udmurtlang.learn.core.media.FeedbackPort
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackPort(impl: FeedbackController): FeedbackPort
}
