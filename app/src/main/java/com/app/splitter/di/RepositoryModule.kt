package com.app.splitter.di

import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.data.repository.SplitterRepositoryImpl
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
    abstract fun bindSplitterRepository(
        impl: SplitterRepositoryImpl
    ): SplitterRepository
}
