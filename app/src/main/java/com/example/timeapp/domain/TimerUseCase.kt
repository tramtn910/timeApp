package com.example.timeapp.domain

import kotlinx.coroutines.flow.Flow

class TimerUseCase(private val repository: TimerRepository) {
    fun start(): Flow<TimerState> = repository.start()
    fun stop() = repository.stop()
    fun reset() = repository.reset()
    fun resume(): Flow<TimerState> = repository.resume()
}