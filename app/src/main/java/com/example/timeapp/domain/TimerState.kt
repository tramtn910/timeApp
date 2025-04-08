package com.example.timeapp.domain

data class TimerState(
    val time: Long = 0L,
    val isRunning: Boolean = false
)