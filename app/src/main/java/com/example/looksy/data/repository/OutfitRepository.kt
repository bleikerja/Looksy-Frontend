package com.example.looksy.data.repository

import com.example.looksy.data.local.dao.OutfitDao
import com.example.looksy.data.model.Outfit
import kotlinx.coroutines.flow.Flow

class OutfitRepository(private val outfitDao: OutfitDao) {
    val allOutfits: Flow<List<Outfit>> = outfitDao.getAllOutfits()

    suspend fun insert(outfit: Outfit) {
        outfitDao.insert(outfit)
    }

    fun getOutfitById(id: Int): Flow<Outfit?> {
        return outfitDao.getOutfitById(id)
    }

    suspend fun update(outfit: Outfit) {
        outfitDao.update(outfit)
    }

    suspend fun updateAll(outfits: List<Outfit>) {
        outfitDao.updateAll(outfits)
    }

    suspend fun delete(outfit: Outfit) {
        outfitDao.delete(outfit)
    }

    suspend fun getByIdDirect(id: Int): Outfit? {
        return outfitDao.getByIdDirect(id)
    }
}