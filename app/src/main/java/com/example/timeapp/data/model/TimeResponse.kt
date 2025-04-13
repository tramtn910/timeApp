package com.example.timeapp.data.model

import com.google.gson.annotations.SerializedName

data class TimeResponse(
    @SerializedName("year") val year: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("day") val day: Int,
    @SerializedName("hour") val hour: Int,
    @SerializedName("minute") val minute: Int,
    @SerializedName("seconds") val seconds: Int,
    @SerializedName("milliSeconds") val milliSeconds: Int,
    @SerializedName("dateTime") val dateTime: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("timeZone") val timeZone: String
)