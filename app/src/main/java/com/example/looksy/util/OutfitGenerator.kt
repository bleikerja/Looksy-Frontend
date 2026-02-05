package com.example.looksy.util

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.Type
import kotlin.random.Random

data class OutfitResult(
    val top: Clothes?,
    val pants: Clothes?,
    val skirt: Clothes?,
    val jacket: Clothes?,
    val dress: Clothes?
)

fun generateRandomOutfit(allClothes: List<Clothes>, allOutfits: List<Outfit>): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    val cleanOutfits = allOutfits.filter {
        (it.topsId in cleanClothes.map { c -> c.id } || it.topsId == null) &&
                (it.pantsId in cleanClothes.map { c -> c.id } || it.pantsId == null) &&
                (it.skirtId in cleanClothes.map { c -> c.id } || it.skirtId == null) &&
                (it.jacketId in cleanClothes.map { c -> c.id } || it.jacketId == null) &&
                (it.dressId in cleanClothes.map { c -> c.id } || it.dressId == null)
    }

    val outfitsWeightedByWorn = cleanOutfits.flatMap { c-> List(c.preference + 1) { c } }
    val finalOutfit = if (Random.nextDouble() < 0.5) outfitsWeightedByWorn.get(Random.nextInt(outfitsWeightedByWorn.size)) else null
    if (finalOutfit == null) {
        val searchForTops = listOf(true, false).random()
        var randomTop: Clothes? = null
        var randomDress: Clothes? = null

        if (searchForTops) {
            randomTop = cleanClothes.filter { it.type == Type.Tops }.randomOrNull()
        } else {
            randomDress = cleanClothes.filter { it.type == Type.Dress }.randomOrNull()
        }

        if (randomTop == null && randomDress == null) {
            if (searchForTops) {
                randomDress = cleanClothes.filter { it.type == Type.Dress }.randomOrNull()
            } else {
                randomTop = cleanClothes.filter { it.type == Type.Tops }.randomOrNull()
            }
        }

        val randomPants = cleanClothes.filter { it.type == Type.Pants }.randomOrNull()
        val randomSkirt = cleanClothes.filter { it.type == Type.Skirt }.randomOrNull()
        val randomJacket = cleanClothes.filter { it.type == Type.Jacket }.randomOrNull()

        var finalSkirt = randomSkirt
        if (randomDress != null) {
            finalSkirt = null
        }

        return OutfitResult(
            top = randomTop,
            pants = randomPants,
            skirt = finalSkirt,
            jacket = randomJacket,
            dress = randomDress
        )
    }
    return OutfitResult(
        top = finalOutfit.topsId?.let { id -> cleanClothes.find { it.id == id } },
        pants = finalOutfit.pantsId?.let { id -> cleanClothes.find { it.id == id } },
        skirt = finalOutfit.skirtId?.let { id -> cleanClothes.find { it.id == id } },
        jacket = finalOutfit.jacketId?.let { id -> cleanClothes.find { it.id == id } },
        dress = finalOutfit.dressId?.let { id -> cleanClothes.find { it.id == id } }
    )
}
