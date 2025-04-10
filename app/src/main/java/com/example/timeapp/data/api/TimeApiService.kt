package com.example.timeapp.data.api

import com.example.timeapp.data.model.TimeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TimeApiService {
    @GET("api/Time/current/zone")
    suspend fun getCurrentTime(
        @Query("timeZone") timeZone: String
    ): Response<TimeResponse>
}