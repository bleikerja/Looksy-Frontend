package com.example.looksy

import android.app.Application
import com.example.looksy.data.local.database.ClothesDatabase
import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.data.repository.OutfitRepository

class LooksyApplication : Application() {
    // Lazily initialize the database and repository
    // This ensures they are only created when first needed
    val database by lazy { ClothesDatabase.Companion.getDatabase(this) }
    val repository by lazy { ClothesRepository(database.clothesDao()) }
    val outfitRepository by lazy { OutfitRepository(database.outfitDao()) }
}