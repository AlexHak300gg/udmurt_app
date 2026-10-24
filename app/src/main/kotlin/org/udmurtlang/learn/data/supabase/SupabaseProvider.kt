package org.udmurtlang.learn.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.udmurtlang.learn.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ленивая инициализация клиента: при SUPABASE_ENABLED = false
 * клиент никогда не создаётся и сетевых вызовов нет.
 */
@Singleton
class SupabaseProvider @Inject constructor() {

    val isReady: Boolean = BuildConfig.SUPABASE_ENABLED &&
        BuildConfig.SUPABASE_URL.isNotBlank() &&
        BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
        BuildConfig.SUPABASE_URL != "https://your-project-ref.supabase.co"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
