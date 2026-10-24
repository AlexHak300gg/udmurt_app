package org.udmurtlang.learn.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.udmurtlang.learn.BuildConfig
import org.udmurtlang.learn.data.repository.LocalAccountRepository
import org.udmurtlang.learn.data.supabase.SupabaseAccountRepository
import org.udmurtlang.learn.domain.repository.AccountRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    /**
     * Единственная точка выбора реализации аккаунта.
     * SUPABASE_ENABLED = false (по умолчанию) → локальный аккаунт, сеть не используется.
     */
    @Provides
    @Singleton
    fun provideAccountRepository(
        local: dagger.Lazy<LocalAccountRepository>,
        cloud: dagger.Lazy<SupabaseAccountRepository>,
    ): AccountRepository = if (BuildConfig.SUPABASE_ENABLED) cloud.get() else local.get()
}
