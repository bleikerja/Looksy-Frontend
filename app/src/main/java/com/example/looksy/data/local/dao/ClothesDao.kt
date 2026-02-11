package com.example.looksy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clothes: Clothes)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clothes: List<Clothes>)

    @Query("SELECT * FROM clothes_table ORDER BY id DESC")
    fun getAllClothes(): Flow<List<Clothes>>

    @Query("SELECT * FROM clothes_table WHERE id = :id")
    fun getClothesById(id: Int): Flow<Clothes?>

    @Query("SELECT * FROM clothes_table WHERE type = :type")
    fun getByType(type: Type): Flow<List<Clothes>>

    @Query("SELECT * FROM clothes_table WHERE id = :id")
    suspend fun getByIdDirect(id: Int): Clothes?

    @Update
    suspend fun update(clothes: Clothes)

    @Update
    suspend fun updateAll(clothes: List<Clothes>)

    @Delete
    suspend fun delete(clothes: Clothes)

    @Delete
    suspend fun deleteAll(clothes: List<Clothes>)
}
