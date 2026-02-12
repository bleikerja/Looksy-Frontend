package com.example.looksy.data.repository

import com.example.looksy.data.location.Location
import com.example.looksy.data.remote.api.GeocodingApiService

class GeocodingRepository(
    private val apiService: GeocodingApiService,
    private val apiKey: String
) {
    /**
     * Converts a city name to coordinates using OpenWeatherMap Geocoding API
     * @param cityName City name (e.g., "Berlin", "München", "New York")
     * @return Result with Location or exception if city not found
     */
    suspend fun getCityCoordinates(cityName: String): Result<Location> {
        return try {
            val response = apiService.getCityCoordinates(cityName, 1, apiKey)
            if (response.isNotEmpty()) {
                val city = response.first()
                Result.success(Location(
                    latitude = city.lat,
                    longitude = city.lon
                ))
            } else {
                Result.failure(Exception("Stadt nicht gefunden: $cityName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
