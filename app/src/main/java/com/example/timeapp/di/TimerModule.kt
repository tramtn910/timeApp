package com.example.timeapp.di

import com.example.timeapp.data.TimerRepositoryImpl
import com.example.timeapp.domain.TimerRepository
import com.example.timeapp.domain.TimerUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {
    @Binds
    @Singleton
    abstract fun bindTimerRepository(
        timerRepositoryImpl: TimerRepositoryImpl
    ): TimerRepository
}