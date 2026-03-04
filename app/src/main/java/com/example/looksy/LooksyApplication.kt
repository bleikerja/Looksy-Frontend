package com.example.looksy

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.looksy.data.local.database.ClothesDatabase
import com.example.looksy.data.preferences.UserPreferencesRepository
import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.data.repository.GeocodingRepository
import com.example.looksy.data.repository.OutfitRepository
import com.example.looksy.data.repository.WeatherRepository
import com.example.looksy.data.remote.api.GeocodingApiService
import com.example.looksy.data.remote.api.WeatherApiService
import com.example.looksy.data.location.LocationProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_prefs")

class LooksyApplication : Application() {
    // Lazily initialize the database and repository
    // This ensures they are only created when first needed
    val database by lazy { ClothesDatabase.getDatabase(this) }
    val repository by lazy { ClothesRepository(database.clothesDao()) }
    val outfitRepository by lazy { OutfitRepository(database.outfitDao()) }
    
    // Weather API Service with Retrofit
    val weatherApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
    
    // Geocoding API Service (uses same base URL for OpenWeatherMap)
    val geocodingApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }
    
    // Weather Repository
    val weatherRepository by lazy { 
        WeatherRepository(weatherApiService, BuildConfig.WEATHER_API_KEY) 
    }
    
    // Geocoding Repository
        // Note: reuses BuildConfig.WEATHER_API_KEY — the same free-tier OpenWeatherMap key
    // covers both /data/2.5/weather and /geo/1.0/direct endpoints.
    val geocodingRepository by lazy {
        GeocodingRepository(geocodingApiService, BuildConfig.WEATHER_API_KEY)
    }
    
    // Location Provider
    val locationProvider by lazy { 
        LocationProvider(this) 
    }

    // User Preferences (city weather persistence)
    val userPreferencesRepository by lazy {
        UserPreferencesRepository(userPreferencesDataStore)
    }
}