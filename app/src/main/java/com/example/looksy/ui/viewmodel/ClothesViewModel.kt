package com.example.looksy.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClothesViewModel(private val repository: ClothesRepository) : ViewModel() {

    val allClothes: StateFlow<List<Clothes>> = repository.allClothes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _lastDiscardedClothes = mutableStateOf<List<Clothes>?>(null)
    val lastDiscardedClothes: State<List<Clothes>?> = _lastDiscardedClothes

    fun insert(clothes: Clothes) = viewModelScope.launch {
        repository.insert(clothes)
    }

    fun getClothesById(id: Int): Flow<Clothes?> {
        return repository.getClothesById(id)
    }

    fun getClothesByType(type: Type): Flow<List<Clothes>> {
        return repository.getClothesByType(type)
    }

    fun update(clothes: Clothes) = viewModelScope.launch {
        repository.update(clothes)
    }

    fun updateAll(clothes: List<Clothes>) = viewModelScope.launch {
        repository.updateAll(clothes)
    }

    fun delete(clothes: Clothes) = viewModelScope.launch {
        repository.delete(clothes)
    }

    fun discardClothes(clothes: List<Clothes>) = viewModelScope.launch {
        _lastDiscardedClothes.value = clothes
        repository.deleteAll(clothes)
    }

    fun undoLastDiscard() = viewModelScope.launch {
        _lastDiscardedClothes.value?.let {
            repository.insertAll(it)
            _lastDiscardedClothes.value = null
        }
    }

    fun incrementClothesPreference(clothes: List<Clothes>) {
        viewModelScope.launch {
            val updatedList = clothes.map { it.copy(wornClothes = it.wornClothes + 1) }
            repository.updateAll(updatedList)
        }
    }

    suspend fun getByIdDirect(id: Int): Clothes? {
        return repository.getByIdDirect(id)
    }
}
