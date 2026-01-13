package com.example.looksy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.looksy.data.repository.ClothesRepository

class ClothesViewModelFactory(
    private val repository: ClothesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClothesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClothesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}