package com.example.looksy.data.local.database

import androidx.room.TypeConverter
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes

class Converters {
    @TypeConverter
    fun fromSize(size: Size): String {
        return size.name
    }

    @TypeConverter
    fun toSize(sizeString: String): Size {
        return Size.valueOf(sizeString)
    }

    @TypeConverter
    fun fromSeason(season: Season): String {
        return season.name
    }

    @TypeConverter
    fun toSeason(seasonString: String): Season {
        return Season.valueOf(seasonString)
    }

    @TypeConverter
    fun fromType(type: Type): String {
        return type.name
    }

    @TypeConverter
    fun toType(typeString: String): Type {
        return Type.valueOf(typeString)
    }

    @TypeConverter
    fun fromMaterial(material: Material): String {
        return material.name
    }

    @TypeConverter
    fun toMaterial(materialString: String): Material {
        return Material.valueOf(materialString)
    }

    @TypeConverter
    fun fromWashingNotes(washingNotes: WashingNotes): String {
        return washingNotes.name
    }

    @TypeConverter
    fun toWashingNotes(washingNotesString: String): WashingNotes {
        return WashingNotes.valueOf(washingNotesString)
    }
}
