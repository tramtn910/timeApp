package com.example.timeapp.di

import com.example.timeapp.domain.TimerRepository
import com.example.timeapp.domain.usecase.ResetTimerUseCase
import com.example.timeapp.domain.usecase.ResumeTimerUseCase
import com.example.timeapp.domain.usecase.StartTimerUseCase
import com.example.timeapp.domain.usecase.StopTimerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideStartTimerUseCase(repository: TimerRepository): StartTimerUseCase {
        return StartTimerUseCase(repository)
    }

    @Provides
    fun provideStopTimerUseCase(repository: TimerRepository): StopTimerUseCase {
        return StopTimerUseCase(repository)
    }

    @Provides
    fun provideResetTimerUseCase(repository: TimerRepository): ResetTimerUseCase {
        return ResetTimerUseCase(repository)
    }

    @Provides
    fun provideResumeTimerUseCase(repository: TimerRepository): ResumeTimerUseCase {
        return ResumeTimerUseCase(repository)
    }
}
