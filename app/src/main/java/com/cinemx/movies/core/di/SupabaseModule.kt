package com.cinemx.movies.core.di

import com.cinemx.movies.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Auth) {
            // En Android, supabase-kt persiste la sesión y refresca el token solo.
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }
    }

    /** Se expone `Auth` y no el cliente completo: contrato de inyección más estrecho. */
    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth
}
