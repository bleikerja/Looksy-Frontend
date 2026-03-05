package com.example.looksy.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.util.DemoDataManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Handles loading / removing demo clothing data.
 *
 * [isDemoActive] reflects whether any demo items currently exist in the DB.
 * [toggleDemo] inserts all demo clothes when inactive, removes them when active.
 */
class DemoDataViewModel(
    private val repository: ClothesRepository,
    private val appContext: Context
) : ViewModel() {

    val isDemoActive: StateFlow<Boolean> = repository.hasDemoClothes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleDemo() {
        viewModelScope.launch {
            if (isDemoActive.value) {
                repository.deleteAllDemoClothes()
            } else {
                val items = DemoDataManager.loadDemoClothes(appContext)
                repository.insertAll(items)
            }
        }
    }
}

class DemoDataViewModelFactory(
    private val repository: ClothesRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DemoDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DemoDataViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
