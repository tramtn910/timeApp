package com.example.timeapp.di

import com.example.timeapp.domain.ClockRepository
import com.example.timeapp.domain.TimerRepository
import com.example.timeapp.domain.usecase.ObserveClockUseCase
import com.example.timeapp.domain.usecase.ResetTimerUseCase
import com.example.timeapp.domain.usecase.ResumeTimerUseCase
import com.example.timeapp.domain.usecase.StartTimerUseCase
import com.example.timeapp.domain.usecase.StopTimerUseCase
import com.example.timeapp.domain.usecase.SyncClockUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideStartTimerUseCase(repository: TimerRepository): StartTimerUseCase {
        return StartTimerUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStopTimerUseCase(repository: TimerRepository): StopTimerUseCase {
        return StopTimerUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResetTimerUseCase(repository: TimerRepository): ResetTimerUseCase {
        return ResetTimerUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResumeTimerUseCase(repository: TimerRepository): ResumeTimerUseCase {
        return ResumeTimerUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSyncClockUseCase(
        clockRepository: ClockRepository
    ): SyncClockUseCase {
        return SyncClockUseCase(clockRepository)
    }

    @Provides
    @Singleton
    fun provideObserveClockUseCase(
        clockRepository: ClockRepository
    ): ObserveClockUseCase {
        return ObserveClockUseCase(clockRepository)
    }
}
