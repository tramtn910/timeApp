package com.example.timeapp

import com.example.timeapp.mvi.MviIntent
import com.example.timeapp.mvi.MviViewState

data class TimerViewState(
    val time: Long,
    val isRunning: Boolean
) : MviViewState {
    companion object {
        fun initial() = TimerViewState(time = 0L, isRunning = false)
    }
}

sealed interface TimerIntent : MviIntent {
    object Start : TimerIntent
    object Stop : TimerIntent
    object Reset : TimerIntent
    object Resume : TimerIntent
}


sealed interface TimerEvent : MviSingleEvent {
    object Finished : TimerEvent
}
