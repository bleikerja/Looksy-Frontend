package com.example.looksy.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.looksy.data.local.dao.ClothesDao
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.local.dao.OutfitDao
import com.example.looksy.data.model.Outfit

@Database(entities = [Clothes::class, Outfit::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ClothesDatabase : RoomDatabase() {
    abstract fun clothesDao(): ClothesDao
    abstract fun outfitDao(): OutfitDao

    companion object {
        @Volatile
        private var INSTANCE: ClothesDatabase? = null

        fun getDatabase(context: Context): ClothesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClothesDatabase::class.java,
                    "clothes_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
