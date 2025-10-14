package com.example.looksy.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.looksy.Repository.ClothesRepository
import com.example.looksy.ViewModels.ClothesViewModel

class ClothesViewModelFactory(private val repository: ClothesRepository) : ViewModelProvider.Factory {

    // Das ist die entscheidende Methode. Android ruft sie auf.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Wir prüfen, ob Android wirklich ein ClothesViewModel von uns will.
        if (modelClass.isAssignableFrom(ClothesViewModel::class.java)) {
            // Wenn ja, erstellen wir es mit dem Repository, das wir haben.
            // Wir müssen es zu 'T' casten, weil die Methode generisch ist.
            @Suppress("UNCHECKED_CAST")
            return ClothesViewModel(repository) as T
        }
        // Wenn Android ein anderes ViewModel will, von dem wir nichts wissen, werfen wir einen Fehler.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}