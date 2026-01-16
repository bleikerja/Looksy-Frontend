package com.example.looksy.data.repository

import com.example.looksy.data.local.dao.ClothesDao
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import kotlinx.coroutines.flow.Flow

class ClothesRepository(private val clothesDao: ClothesDao) {
    val allClothes: Flow<List<Clothes>> = clothesDao.getAllClothes()

    suspend fun insert(clothes: Clothes) {
        clothesDao.insert(clothes)
    }

    fun getClothesById(id: Int): Flow<Clothes?> {
        return clothesDao.getClothesById(id)
    }

    fun getClothesByType(type: Type): Flow<List<Clothes>> {
        return clothesDao.getByType(type)
    }

    suspend fun update(clothes: Clothes) {
        clothesDao.update(clothes)
    }

    suspend fun updateAll(clothes: List<Clothes>) {
        clothesDao.updateAll(clothes)
    }

    suspend fun delete(clothes: Clothes) {
        clothesDao.delete(clothes)
    }

    suspend fun getByIdDirect(id: Int): Clothes? {
        return clothesDao.getByIdDirect(id)
    }
}
