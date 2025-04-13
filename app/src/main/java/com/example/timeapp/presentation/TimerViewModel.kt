package com.example.timeapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeapp.domain.usecase.ObserveClockUseCase
import com.example.timeapp.domain.usecase.ResetTimerUseCase
import com.example.timeapp.domain.usecase.ResumeTimerUseCase
import com.example.timeapp.domain.usecase.StartTimerUseCase
import com.example.timeapp.domain.usecase.StopTimerUseCase
import com.example.timeapp.domain.usecase.SyncClockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val resetTimerUseCase: ResetTimerUseCase,
    private val resumeTimerUseCase: ResumeTimerUseCase,
    private val syncClockUseCase: SyncClockUseCase,
    private val observeClockUseCase: ObserveClockUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow(TimerViewState.initial())
    val viewState: StateFlow<TimerViewState> = _viewState

    private val _event = MutableSharedFlow<TimerEvent>()
    val event = _event.asSharedFlow()

    private val timeZone: String = "Asia/Ho_Chi_Minh"

    init {
        handleClock()
    }

    fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.Start -> startTimer()
            is TimerIntent.Stop -> stopTimer()
            is TimerIntent.Reset -> resetTimer()
            is TimerIntent.Resume -> resumeTimer()
            is TimerIntent.GetCurrentTime -> handleClock()
        }
    }

    private fun handleClock() {
        viewModelScope.launch {
            try {
                syncClockUseCase.startClockSyncing(timeZone)
                observeClockUseCase().collectLatest { date ->
                    val formatted = formatDate(date)
                    _viewState.value = _viewState.value.copy(
                        currentTime = formatted,
                        timeZone = timeZone
                    )
                }
            } catch (e: Exception) {
                _event.emit(TimerEvent.ShowErrorToast("Failed to sync time: ${e.message}"))
            }
        }
    }

    private fun formatDate(date: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone(_viewState.value.timeZone)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone(_viewState.value.timeZone)
        }
        return "${timeFormat.format(date)} ${dateFormat.format(date)}"
    }

    private fun startTimer() {
        viewModelScope.launch {
            launch {
                startTimerUseCase().collectLatest { state ->
                    _viewState.value = _viewState.value.copy(
                        time = state.time,
                        isRunning = state.isRunning
                    )
                }
            }
            _event.emit(TimerEvent.ShowTimerStartToast)
        }
    }

    private fun stopTimer() {
        viewModelScope.launch {
            stopTimerUseCase()
            _event.emit(TimerEvent.ShowTimerStopToast)
        }
    }

    private fun resetTimer() {
        viewModelScope.launch {
            resetTimerUseCase()
            _viewState.value = _viewState.value.copy(
                time = 0L,
                isRunning = false
            )
            _event.emit(TimerEvent.ShowTimerResetToast)
        }
    }

    private fun resumeTimer() {
        viewModelScope.launch {
            launch {
                resumeTimerUseCase().collectLatest { state ->
                    _viewState.value = _viewState.value.copy(
                        time = state.time,
                        isRunning = state.isRunning
                    )
                }
            }
            _event.emit(TimerEvent.ShowTimerResumeToast)
        }
    }
}