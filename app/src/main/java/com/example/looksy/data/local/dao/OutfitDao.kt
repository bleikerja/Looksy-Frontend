package com.example.looksy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.looksy.data.model.Outfit
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outfit: Outfit)

    @Query("SELECT * FROM outfits_table ORDER BY id DESC")
    fun getAllOutfits(): Flow<List<Outfit>>

    @Query("SELECT * FROM outfits_table WHERE id = :id")
    fun getOutfitById(id: Int): Flow<Outfit?>

    @Query("SELECT * FROM outfits_table WHERE id = :id")
    suspend fun getByIdDirect(id: Int): Outfit?

    @Update
    suspend fun update(outfit: Outfit)

    @Update
    suspend fun updateAll(outfits: List<Outfit>)

    @Delete
    suspend fun delete(outfit: Outfit)
    @Query("""
        SELECT * FROM outfits_table 
        WHERE (topsId IS :selectedTopId) 
        AND (dressId IS :selectedDressId) 
        AND (skirtId IS :selectedSkirtId) 
        AND (pantsId IS :selectedPantsId) 
        AND (jacketId IS :selectedJacketId) 
        LIMIT 1
    """)
    suspend fun findMatchingOutfit(
        selectedTopId: Int?,
        selectedDressId: Int?,
        selectedSkirtId: Int?,
        selectedPantsId: Int?,
        selectedJacketId: Int?
    ): Outfit?
}
