package com.example.looksy.data.repository

import com.example.looksy.data.model.Weather
import com.example.looksy.data.remote.api.WeatherApiService

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val apiKey: String
) {
    suspend fun getWeather(latitude: Double, longitude: Double): Weather {
        val response = apiService.getWeatherByLocation(latitude, longitude, apiKey)
        return Weather(
            locationName = response.name,
            temperature = response.main.temp,
            feelsLike = response.main.feels_like,
            description = response.weather.firstOrNull()?.description ?: "",
            humidity = response.main.humidity,
            iconUrl = "https://openweathermap.org/img/w/${response.weather.firstOrNull()?.icon}.png"
        )
    }
}