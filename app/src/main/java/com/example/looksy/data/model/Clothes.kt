package com.example.looksy.data.model

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
    val wornSince: Long? = null,
    val daysWorn: Int = 0,
    val clean: Boolean,
    val washingNotes: WashingNotes,

    val selected: Boolean = false,

    val imagePath: String = "",
    val isSynced: Boolean = false,

    val wornClothes: Int = 0
)
