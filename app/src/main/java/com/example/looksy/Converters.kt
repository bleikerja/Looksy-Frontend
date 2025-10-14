package com.example.looksy

import com.example.looksy.dataClassClones.Size
import androidx.room.TypeConverter
import com.example.looksy.dataClassClones.Material
import com.example.looksy.dataClassClones.Season
import com.example.looksy.dataClassClones.Type
import com.example.looksy.dataClassClones.WashingNotes

class Converters {
    @TypeConverter
    fun fromSize(size: Size): String {
        return size.name // Wandelt z.B. Size._46 in den String "_46" um
    }

    @TypeConverter
    fun toSize(sizeString: String): Size {
        return Size.valueOf(sizeString) // Wandelt den String "_46" zurück in Size._46 um
    }

    // --- Konverter für 'Season' Enum ---
    @TypeConverter
    fun fromSeason(season: Season): String {
        return season.name
    }

    @TypeConverter
    fun toSeason(seasonString: String): Season {
        return Season.valueOf(seasonString)
    }

    // --- Konverter für 'Type' Enum ---
    @TypeConverter
    fun fromType(type: Type): String {
        return type.name
    }

    @TypeConverter
    fun toType(typeString: String): Type {
        return Type.valueOf(typeString)
    }

    // --- Konverter für 'Material' Enum ---
    @TypeConverter
    fun fromMaterial(material: Material): String {
        return material.name
    }

    @TypeConverter
    fun toMaterial(materialString: String): Material {
        return Material.valueOf(materialString)
    }

    // --- Konverter für 'WashingNotes' Enum ---
    @TypeConverter
    fun fromWashingNotes(washingNotes: WashingNotes): String {
        return washingNotes.name
    }

    @TypeConverter
    fun toWashingNotes(washingNotesString: String): WashingNotes {
        return WashingNotes.valueOf(washingNotesString)
    }
}