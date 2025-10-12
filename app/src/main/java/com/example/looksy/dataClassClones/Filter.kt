package com.example.looksy.dataClassClones

class Filter {
    fun byType(type: Type, list: MutableList<Clothes>): MutableList<Clothes> {
        return list.filter { it.type == type }.toMutableList()
    }

    fun bySeason(season: Season, list: MutableList<Clothes>): MutableList<Clothes> {
        return list.filter { it.seasonUsage == season }.toMutableList()
    }

    fun bySize(size: Size, list: MutableList<Clothes>): MutableList<Clothes> {
        return list.filter { it.size == size }.toMutableList()
    }

    fun byMaterial(material: Material, list: MutableList<Clothes>): MutableList<Clothes> {
        return list.filter { it.material == material }.toMutableList()
    }

    fun byCleanliness(clean: Boolean, list: MutableList<Clothes>): MutableList<Clothes> {
        return list.filter { it.clean == clean }.toMutableList()
    }
}