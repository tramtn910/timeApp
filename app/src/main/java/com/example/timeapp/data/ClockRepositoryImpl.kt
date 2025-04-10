package com.example.timeapp.data

import com.example.timeapp.data.api.TimeApiService
import com.example.timeapp.domain.ClockRepository
import jakarta.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Singleton
class ClockRepositoryImpl @Inject constructor(
    private val timeApiService: TimeApiService
) : ClockRepository {

    private val syncedTimeFlow = MutableStateFlow(Date())

    private var syncedTimeMillis: Long = 0L
    private var syncAt: Long = System.currentTimeMillis()

    private var syncJob: Job? = null
    private var clockJob: Job? = null

    override suspend fun syncClock(timeZone: String) {
        val response = timeApiService.getCurrentTime(timeZone)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone)).apply {
                set(Calendar.YEAR, body.year)
                set(Calendar.MONTH, body.month - 1)
                set(Calendar.DAY_OF_MONTH, body.day)
                set(Calendar.HOUR_OF_DAY, body.hour)
                set(Calendar.MINUTE, body.minute)
                set(Calendar.SECOND, body.seconds)
                set(Calendar.MILLISECOND, body.milliSeconds)
            }

            syncedTimeMillis = calendar.timeInMillis
            syncAt = System.currentTimeMillis()

            val currentTime = Date(syncedTimeMillis)
            syncedTimeFlow.emit(currentTime)
        } else {
            throw Exception("Failed to sync time: ${response.code()}")
        }
    }

    override fun startClockSyncing(timeZone: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncClock(timeZone)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            startClock()
            startSyncingClock(timeZone)
        }
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val offset = System.currentTimeMillis() - syncAt
                val currentTime = Date(syncedTimeMillis + offset)
                syncedTimeFlow.emit(currentTime)
            }
        }
    }

    private fun startSyncingClock(timeZone: String) {
        syncJob?.cancel()
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    syncClock(timeZone)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000)
            }
        }
    }

    override fun observeSyncedTime(): Flow<Date> = syncedTimeFlow
}