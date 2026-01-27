package com.example.looksy.data.repository

import com.example.looksy.data.model.Weather
import com.example.looksy.data.remote.api.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val apiKey: String
) {
    fun getWeather(latitude: Double, longitude: Double): Flow<Result<Weather>> = flow {
        try {
            val response = apiService.getWeatherByLocation(latitude, longitude, apiKey)
            val weather = Weather(
                locationName = response.name,
                temperature = response.main.temp,
                feelsLike = response.main.feels_like,
                description = response.weather.firstOrNull()?.description ?: "",
                humidity = response.main.humidity,
                iconUrl = "https://openweathermap.org/img/w/${response.weather.firstOrNull()?.icon}.png"
            )
            emit(Result.success(weather))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}