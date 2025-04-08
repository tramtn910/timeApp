package com.example.timeapp.mvi

data class TimerViewState(
    val time: Long,
    val isRunning: Boolean
) : MviViewState {
    companion object {
        fun initial() = TimerViewState(
            time = 0L,
            isRunning = false
        )
    }
}

sealed interface TimerIntent : MviIntent {
    object Start : TimerIntent
    object Stop : TimerIntent
    object Reset : TimerIntent
    object Resume : TimerIntent
}