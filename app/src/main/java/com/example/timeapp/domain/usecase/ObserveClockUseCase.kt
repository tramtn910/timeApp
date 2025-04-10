package com.example.timeapp.domain.usecase

import com.example.timeapp.domain.ClockRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ObserveClockUseCase(
    private val clockRepository: ClockRepository
) {
    operator fun invoke(): Flow<Date> {
        return clockRepository.observeSyncedTime()
    }
}