package com.example.looksy.Repository

import com.example.looksy.dataClassClones.Clothes
import kotlinx.coroutines.flow.Flow
import com.example.looksy.dao.ClothesDao
import com.example.looksy.dataClassClones.Type

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

}