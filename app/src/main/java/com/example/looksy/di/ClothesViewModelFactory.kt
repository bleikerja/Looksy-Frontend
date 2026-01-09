package com.example.looksy.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.looksy.domain.repository.ClothesRepository
import com.example.looksy.presentation.viewmodel.ClothesViewModel

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
