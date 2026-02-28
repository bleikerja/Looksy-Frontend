package com.example.looksy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfits_table")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Alle IDs sind nullable, damit ein Outfit nicht alle Kleidungsstücktypen enthalten muss
    val dressId: Int? = null,
    val topsId: Int? = null,
    val skirtId: Int? = null,
    val pantsId: Int? = null,
    val jacketId: Int? = null,
    val shoesId: Int? = null,
    val preference: Int = 0,
    val isSynced: Boolean = false,
    val isManuelSaved: Boolean = false
)