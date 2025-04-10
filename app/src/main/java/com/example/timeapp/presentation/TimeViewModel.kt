package com.example.timeapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeapp.data.api.TimeApiService
import com.example.timeapp.data.model.TimeResponse
import com.example.timeapp.domain.usecase.ResetTimerUseCase
import com.example.timeapp.domain.usecase.ResumeTimerUseCase
import com.example.timeapp.domain.usecase.StartTimerUseCase
import com.example.timeapp.domain.usecase.StopTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timeApiService: TimeApiService,
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val resetTimerUseCase: ResetTimerUseCase,
    private val resumeTimerUseCase: ResumeTimerUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(TimerViewState.initial())
    val viewState: StateFlow<TimerViewState> = _viewState

    private val _event = MutableSharedFlow<TimerEvent>()
    val event = _event.asSharedFlow()

    private var timeUpdateJob: Job? = null
    private val timeZone = "Asia/Ho_Chi_Minh"
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var cachedTimeResponse: TimeResponse? = null
    private var lastFetchTime: Long = 0L

    private var syncedTimeMillis: Long = 0L
    private var syncAt: Long = 0L

    init {
        timeFormat.timeZone = TimeZone.getTimeZone(timeZone)
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        startTimeClock()
    }

    override fun onCleared() {
        super.onCleared()
        timeUpdateJob?.cancel()
    }

    fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.Start -> startTimer()
            is TimerIntent.Stop -> stopTimer()
            is TimerIntent.Reset -> resetTimer()
            is TimerIntent.Resume -> resumeTimer()
            is TimerIntent.GetCurrentTime -> getCurrentTime(true)
        }
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

    private fun startTimeClock() {
        timeUpdateJob?.cancel()

        timeUpdateJob = viewModelScope.launch {
            getCurrentTime(true)

            launch {
                while (true) {
                    delay(10000)
                    getCurrentTime(true)
                }
            }

            launch {
                while (true) {
                    delay(1000)
                    val offset = System.currentTimeMillis() - syncAt
                    val accurateTime = Date(syncedTimeMillis + offset)
                    updateTimeDisplay(accurateTime)
                }
            }
        }
    }

    private fun getCurrentTime(notifyUpdate: Boolean = false) {
        viewModelScope.launch {
            try {
                val response = timeApiService.getCurrentTime(timeZone)
                if (response.isSuccessful && response.body() != null) {
                    val timeResponse = response.body()!!
                    cachedTimeResponse = timeResponse
                    lastFetchTime = System.currentTimeMillis()

                    updateCalendarFromResponse(timeResponse)

                    if (notifyUpdate) {
                        _event.emit(TimerEvent.ShowCurrentTimeToast)
                    }
                } else if (cachedTimeResponse != null) {
                    updateCalendarFromResponse(cachedTimeResponse!!)
                    _event.emit(TimerEvent.ShowErrorToast("Failed to fetch time: ${response.code()}, using cache"))
                } else {
                    _event.emit(TimerEvent.ShowErrorToast("Failed to fetch time: ${response.code()}"))
                }
            } catch (e: Exception) {
                if (cachedTimeResponse != null) {
                    updateCalendarFromResponse(cachedTimeResponse!!)
                    _event.emit(TimerEvent.ShowErrorToast("Network error, using cache"))
                } else {
                    _event.emit(TimerEvent.ShowErrorToast("Network error: ${e.message}"))
                }
            }
        }
    }

    private fun updateCalendarFromResponse(timeResponse: TimeResponse) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone)).apply {
            set(Calendar.YEAR, timeResponse.year)
            set(Calendar.MONTH, timeResponse.month - 1)
            set(Calendar.DAY_OF_MONTH, timeResponse.day)
            set(Calendar.HOUR_OF_DAY, timeResponse.hour)
            set(Calendar.MINUTE, timeResponse.minute)
            set(Calendar.SECOND, timeResponse.seconds)
            set(Calendar.MILLISECOND, timeResponse.milliSeconds)
        }

        syncedTimeMillis = cal.timeInMillis
        syncAt = System.currentTimeMillis()

        updateTimeDisplay(Date(syncedTimeMillis))
    }

    private fun updateTimeDisplay(date: Date) {
        val timeString = timeFormat.format(date)
        val dateString = dateFormat.format(date)

        _viewState.value = _viewState.value.copy(
            currentTime = "$timeString $dateString",
            timeZone = timeZone
        )
    }
}