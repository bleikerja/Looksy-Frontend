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
    val material: Material? = null,
    val color: ClothesColor? = null,
    val brand: String? = null,
    val comment: String? = null,
    val wornSince: Long? = null,
    val lastWorn: Long? = null,
    val daysWorn: Int = 0,
    val clean: Boolean,
    val washingNotes: List<WashingNotes>,

    val selected: Boolean = false,

    val imagePath: String = "",
    val isSynced: Boolean = false,

    val wornClothes: Int = 0
)
