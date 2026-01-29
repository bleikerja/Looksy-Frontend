package com.example.looksy.data.remote.dto

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<WeatherInfo>
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)

data class WeatherInfo(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)