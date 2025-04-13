package com.example.timeapp.domain.usecase

import com.example.timeapp.domain.ClockRepository

class SyncClockUseCase(
    private val clockRepository: ClockRepository
) {
    fun startClockSyncing(timeZone: String) {
        clockRepository.startClockSyncing(timeZone)
    }
}