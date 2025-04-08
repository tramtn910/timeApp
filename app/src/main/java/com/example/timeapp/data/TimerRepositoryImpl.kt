package com.example.timeapp.data

import com.example.timeapp.domain.TimerRepository
import com.example.timeapp.domain.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerRepositoryImpl : TimerRepository {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null
    private var currentTime = 0L
    private val _state = MutableStateFlow(TimerState())

    override fun start(): Flow<TimerState> {
        stop()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                currentTime += 1000
                _state.update { it.copy(time = currentTime, isRunning = true) }
            }
        }
        return _state
    }

    override fun stop() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    override fun reset() {
        stop()
        currentTime = 0L
        _state.update { it.copy(time = 0L, isRunning = false) }
    }

    override fun resume(): Flow<TimerState> = start()
}