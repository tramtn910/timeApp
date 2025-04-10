package com.example.timeapp.domain.usecase

import com.example.timeapp.domain.TimerRepository
import jakarta.inject.Inject

class ResetTimerUseCase @Inject constructor(private val repo: TimerRepository) {
    operator fun invoke() = repo.reset()
}