package com.example.looksy.data.model
data class Weather(
    val locationName: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val humidity: Int,
    val iconUrl: String
)