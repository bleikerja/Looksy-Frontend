package com.example.looksy.data.repository

import androidx.compose.animation.core.copy
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

    suspend fun incrementOutfitPreference(
        selectedTopId: Int?,
        selectedDressId: Int?,
        selectedSkirtId: Int?,
        selectedPantsId: Int?,
        selectedJacketId: Int?
    ) {
        val existingOutfit = outfitDao.findMatchingOutfit(
            selectedTopId,
            selectedDressId,
            selectedSkirtId,
            selectedPantsId,
            selectedJacketId
        )
        if (existingOutfit != null) {
            // 2. Outfit existiert -> Count erhöhen
            val updatedOutfit = existingOutfit.copy(
                preference = existingOutfit.preference.plus(1)
            )
            outfitDao.update(updatedOutfit)
        } else {
            // 3. Optional: Neues Outfit speichern, da es zum ersten Mal gewählt wurde
            val newOutfit = Outfit(
                topsId = selectedTopId,
                pantsId = selectedPantsId,
                dressId = selectedDressId,
                skirtId = selectedSkirtId,
                jacketId = selectedJacketId,
                preference = 1
            )
            outfitDao.insert(newOutfit)
        }
    }
}