package com.example.timeapp.presentation

import com.example.timeapp.mvi.MviIntent
import com.example.timeapp.mvi.MviSingleEvent
import com.example.timeapp.mvi.MviViewState

data class TimerViewState(
    val time: Long,
    val isRunning: Boolean
) : MviViewState {
    companion object {
        fun initial() = TimerViewState(
            0L,
            false
        )
    }
}

sealed interface TimerIntent : MviIntent {
    object Start : TimerIntent
    object Stop : TimerIntent
    object Reset : TimerIntent
    object Resume : TimerIntent
}

sealed interface TimerEvent : MviSingleEvent {
    object ShowTimerStartToast : TimerEvent
    object ShowTimerStopToast : TimerEvent
    object ShowTimerResetToast : TimerEvent
    object ShowTimerResumeToast : TimerEvent
}