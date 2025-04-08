package com.example.timeapp.domain

import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    fun start(): Flow<TimerState>
    fun stop()
    fun reset()
    fun resume(): Flow<TimerState>
}