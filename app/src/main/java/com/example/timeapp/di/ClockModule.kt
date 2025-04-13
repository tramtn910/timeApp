package com.example.timeapp.di

import com.example.timeapp.data.ClockRepositoryImpl
import com.example.timeapp.domain.ClockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClockModule {

    @Binds
    @Singleton
    abstract fun bindClockRepository(
        impl: ClockRepositoryImpl
    ): ClockRepository
}