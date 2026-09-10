package com.cinemx.movies.core.di

import com.cinemx.movies.feature.auth.data.SupabaseAuthRepository
import com.cinemx.movies.feature.auth.domain.AuthRepository
import com.cinemx.movies.feature.movies.data.MovieRepositoryImpl
import com.cinemx.movies.feature.movies.domain.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository
}
