package com.example.looksy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.Type
import com.example.looksy.data.repository.OutfitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OutfitViewModel(private val repository: OutfitRepository) : ViewModel() {
    val allOutfits: StateFlow<List<Outfit>> = repository.allOutfits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(outfit: Outfit) = viewModelScope.launch {
        repository.insert(outfit)
    }

    fun getOutfitById(id: Int): Flow<Outfit?> {
        return repository.getOutfitById(id)
    }

    fun update(outfit: Outfit) = viewModelScope.launch {
        repository.update(outfit)
    }

    fun updateAll(outfits: List<Outfit>) = viewModelScope.launch {
        repository.updateAll(outfits)
    }

    fun delete(outfit: Outfit) = viewModelScope.launch {
        repository.delete(outfit)
    }

    suspend fun getByIdDirect(id: Int): Outfit? {
        return repository.getByIdDirect(id)
    }
    fun incrementOutfitPreference(
        selectedTopId: Int?,
        selectedDressId: Int?,
        selectedSkirtId: Int?,
        selectedPantsId: Int?,
        selectedJacketId: Int?,
        selectedShoesId: Int? = null
    ){
        viewModelScope.launch {
            repository.incrementOutfitPreference(
                selectedDressId = selectedDressId,
                selectedTopId = selectedTopId,
                selectedSkirtId = selectedSkirtId,
                selectedPantsId = selectedPantsId,
                selectedJacketId = selectedJacketId,
                selectedShoesId = selectedShoesId
            )
        }
    }
}
