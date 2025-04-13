package com.example.timeapp.domain

import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ClockRepository {
    suspend fun syncClock(timeZone: String)
    fun observeSyncedTime(): Flow<Date>
    fun startClockSyncing(timeZone: String)
}
