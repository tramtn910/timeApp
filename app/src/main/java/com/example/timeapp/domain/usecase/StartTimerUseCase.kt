package com.example.timeapp.domain.usecase

import com.example.timeapp.domain.TimerRepository
import com.example.timeapp.domain.TimerState
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class StartTimerUseCase @Inject constructor(private val repo: TimerRepository) {
    operator fun invoke(): Flow<TimerState> = repo.start()
}