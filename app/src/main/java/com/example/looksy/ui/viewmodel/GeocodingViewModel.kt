package com.example.looksy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.looksy.data.location.Location
import com.example.looksy.data.repository.GeocodingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for geocoding operations
 */
sealed class GeocodingUiState {
    data object Idle : GeocodingUiState()
    data object Loading : GeocodingUiState()
    data class Success(val location: Location, val cityName: String) : GeocodingUiState()
    data class Error(val message: String) : GeocodingUiState()
}

/**
 * ViewModel for managing geocoding operations (city name to coordinates)
 * Follows MVVM pattern: Screen → ViewModel → Repository → API
 */
class GeocodingViewModel(
    private val repository: GeocodingRepository
) : ViewModel() {

    private val _geocodingState = MutableStateFlow<GeocodingUiState>(GeocodingUiState.Idle)
    val geocodingState: StateFlow<GeocodingUiState> = _geocodingState.asStateFlow()

    /**
     * Converts a city name to geographic coordinates
     * @param cityName Name of the city (e.g., "Berlin", "München")
     */
    fun getCityCoordinates(cityName: String) {
        if (cityName.isBlank()) {
            _geocodingState.value = GeocodingUiState.Error("Bitte gib einen Stadtnamen ein")
            return
        }

        viewModelScope.launch {
            _geocodingState.value = GeocodingUiState.Loading
            
            val result = repository.getCityCoordinates(cityName.trim())
            
            result.onSuccess { location ->
                _geocodingState.value = GeocodingUiState.Success(location, cityName.trim())
            }.onFailure { error ->
                _geocodingState.value = GeocodingUiState.Error(
                    error.message ?: "Fehler beim Suchen der Stadt"
                )
            }
        }
    }

    /**
     * Resets the geocoding state to Idle
     */
    fun resetState() {
        _geocodingState.value = GeocodingUiState.Idle
    }
}
