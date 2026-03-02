package com.example.looksy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.looksy.data.model.Weather
import com.example.looksy.data.preferences.UserPreferencesRepository
import com.example.looksy.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    val lastSearchedCity: StateFlow<String> = prefs.lastSearchedCity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val lastSearchedLat: StateFlow<Double?> = prefs.lastSearchedLat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val lastSearchedLon: StateFlow<Double?> = prefs.lastSearchedLon
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            try {
                val weather = repository.getWeather(latitude, longitude)
                _weatherState.value = WeatherUiState.Success(weather)
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchWeatherForSavedCity() {
        // Use first() to wait for DataStore to emit its persisted values before reading —
        // on app restart the stateIn cache starts as null until the Flow delivers the
        // first value from disk, so we cannot rely on .value here.
        viewModelScope.launch {
            val lat = prefs.lastSearchedLat.first()
            val lon = prefs.lastSearchedLon.first()
            if (lat != null && lon != null) {
                fetchWeather(lat, lon)
            }
        }
    }

    fun saveLastSearchedCity(city: String, lat: Double, lon: Double) {
        viewModelScope.launch { prefs.saveLastSearchedCity(city, lat, lon) }
    }

    fun clearLastSearchedCity() {
        viewModelScope.launch { prefs.clearLastSearchedCity() }
    }
}

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
