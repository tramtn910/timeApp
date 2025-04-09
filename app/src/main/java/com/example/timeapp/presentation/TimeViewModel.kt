package com.example.timeapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeapp.domain.TimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val useCase: TimerUseCase
) : ViewModel() {
    private val _viewState = MutableStateFlow(TimerViewState.initial())
    val viewState: StateFlow<TimerViewState> = _viewState

    private val _event = MutableSharedFlow<TimerEvent>()
    val event = _event.asSharedFlow()

    fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.Start -> startTimer()
            is TimerIntent.Stop -> stopTimer()
            is TimerIntent.Reset -> resetTimer()
            is TimerIntent.Resume -> resumeTimer()
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            launch {
                useCase.start().collectLatest { state ->
                    _viewState.value = TimerViewState(state.time, state.isRunning)
                }
            }
            _event.emit(TimerEvent.ShowTimerStartToast)
        }
    }

    private fun stopTimer() {
        viewModelScope.launch {
            useCase.stop()
            _event.emit(TimerEvent.ShowTimerStopToast)
        }
    }

    private fun resetTimer() {
        viewModelScope.launch {
            useCase.reset()
            _viewState.value = TimerViewState(0L, false)
            _event.emit(TimerEvent.ShowTimerResetToast)
        }
    }

    private fun resumeTimer() {
        viewModelScope.launch {
            launch {
                useCase.resume().collectLatest { state ->
                    _viewState.value = TimerViewState(state.time, state.isRunning)
                }
            }
            _event.emit(TimerEvent.ShowTimerResumeToast)
        }
    }
}
