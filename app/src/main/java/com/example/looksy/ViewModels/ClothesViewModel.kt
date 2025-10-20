package com.example.looksy.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.looksy.Repository.ClothesRepository
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Type
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
private fun saveImagePermanently(context: Context, tempUri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(tempUri)
    val fileName = "IMG_${System.currentTimeMillis()}.jpg"

    // context.filesDir ist der private, interne Speicher deiner App.
    val persistentFile = File(context.filesDir, fileName)

    val outputStream = FileOutputStream(persistentFile)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    // Lösche die temporäre Datei aus dem Cache, um Speicherplatz zu sparen.
    val tempFile = File(tempUri.path)
    if (tempFile.exists()) {
        tempFile.delete()
    }

    // Gib den permanenten Pfad als String zurück.
    return persistentFile.absolutePath
}
 */
class ClothesViewModel(private val repository: ClothesRepository) : ViewModel() {

    // DIESE ZEILE IST DIE BRÜCKE ZUM FRONTEND:
    val allClothes: StateFlow<List<Clothes>> = repository.allClothes
        .stateIn(
            scope = viewModelScope, // Läuft so lange wie das ViewModel lebt
            started = SharingStarted.WhileSubscribed(5000), // Startet, wenn die UI zuhört
            initialValue = emptyList() // Wichtig: Eine leere Liste als Startwert
        )

    // Die `insert` Funktion, die vom Frontend aufgerufen wird
    fun insert(clothes: Clothes) = viewModelScope.launch {
        repository.insert(clothes)
    }

    // Die `getById` Funktion für den Detailbildschirm
    fun getClothesById(id: Int): Flow<Clothes?> {
        return repository.getClothesById(id)
    }

    fun getClothesByType(type: Type): Flow<List<Clothes>> {
        return repository.getClothesByType(type)
    }

    fun update(clothes: Clothes) = viewModelScope.launch {
        repository.update(clothes) // Ruft die update-Methode des Repositories auf
    }

    companion object

}