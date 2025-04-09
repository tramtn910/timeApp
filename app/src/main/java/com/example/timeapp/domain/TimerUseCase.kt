package com.example.timeapp.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TimerUseCase @Inject constructor(private val repository: TimerRepository) {
    fun start(): Flow<TimerState> = repository.start()
    fun stop() = repository.stop()
    fun reset() = repository.reset()
    fun resume(): Flow<TimerState> = repository.resume()
}