package com.example.timeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeapp.mvi.TimerIntent
import com.example.timeapp.mvi.TimerViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(TimerViewState.initial())
    val viewState: StateFlow<TimerViewState> = _viewState.asStateFlow()

    private var currentTime = 0L
    private var timerJob: Job? = null

    fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.Start -> startTimer()
            is TimerIntent.Stop -> stopTimer()
            is TimerIntent.Reset -> resetTimer()
            is TimerIntent.Resume -> resumeTimer()
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                currentTime += 1000
                _viewState.value = _viewState.value.copy(time = currentTime, isRunning = true)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _viewState.value = _viewState.value.copy(isRunning = false)
    }

    private fun resetTimer() {
        stopTimer()
        currentTime = 0L
        _viewState.value = _viewState.value.copy(time = currentTime)
    }

    private fun resumeTimer() {
        if (!_viewState.value.isRunning) {
            startTimer()
        }
    }
}
