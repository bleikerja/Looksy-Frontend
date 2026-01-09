package com.example.looksy.application

import android.app.Application
import com.example.looksy.data.local.database.ClothesDatabase
import com.example.looksy.domain.repository.ClothesRepository

class ClothesApplication : Application() {
    // Lazily initialize the database and repository
    // This ensures they are only created when first needed
    val database by lazy { ClothesDatabase.getDatabase(this) }
    val repository by lazy { ClothesRepository(database.clothesDao()) }
}
