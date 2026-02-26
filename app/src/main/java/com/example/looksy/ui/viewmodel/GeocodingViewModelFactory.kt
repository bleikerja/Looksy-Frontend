package com.example.looksy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.looksy.data.repository.GeocodingRepository

class GeocodingViewModelFactory(
    private val repository: GeocodingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeocodingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeocodingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
