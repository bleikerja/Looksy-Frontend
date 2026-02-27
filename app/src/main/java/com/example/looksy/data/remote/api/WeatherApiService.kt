package com.example.looksy.data.remote.api

import com.example.looksy.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeatherByLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric", // Celsius
        @Query("lang") lang: String = "de"
    ): WeatherResponse
}