package com.example.looksy.Application

import android.app.Application
import com.example.looksy.Repository.ClothesRepository
import kotlin.getValue
import com.example.looksy.ClothesDatabase

class ClothesApplication: Application() {
    val database by lazy {ClothesDatabase.getDatabase(this)}
    val repository by lazy { ClothesRepository(database.clothesDao()) }
}