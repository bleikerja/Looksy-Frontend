package com.example.looksy.dataClassClones

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothes_table")
data class Clothes(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val size: Size,
    val seasonUsage: Season,
    val type: Type,
    val material: Material,
    val clean: Boolean,
    val washingNotes: WashingNotes,

    val imagePath: String = "",
    val isSynced: Boolean = false
)