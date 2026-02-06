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
    val cleanClothesIds = cleanClothes.map { it.id }.toSet()

    // With a 40% probability, try to use a saved outfit
    if (allOutfits.isNotEmpty() && Random.nextDouble() < 0.4) {
        val cleanOutfits = allOutfits.filter { outfit ->
            (outfit.topsId == null || outfit.topsId in cleanClothesIds) &&
                    (outfit.pantsId == null || outfit.pantsId in cleanClothesIds) &&
                    (outfit.skirtId == null || outfit.skirtId in cleanClothesIds) &&
                    (outfit.jacketId == null || outfit.jacketId in cleanClothesIds) &&
                    (outfit.dressId == null || outfit.dressId in cleanClothesIds)
        }

        if (cleanOutfits.isNotEmpty()) {
            val outfitsWeightedByPreference =
                cleanOutfits.flatMap { c -> List(c.preference + 1) { c } }
            if (outfitsWeightedByPreference.isNotEmpty()) {
                val savedOutfit = outfitsWeightedByPreference.random()
                // Found a saved outfit to return
                return OutfitResult(
                    top = savedOutfit.topsId?.let { id -> cleanClothes.find { it.id == id } },
                    pants = savedOutfit.pantsId?.let { id -> cleanClothes.find { it.id == id } },
                    skirt = savedOutfit.skirtId?.let { id -> cleanClothes.find { it.id == id } },
                    jacket = savedOutfit.jacketId?.let { id -> cleanClothes.find { it.id == id } },
                    dress = savedOutfit.dressId?.let { id -> cleanClothes.find { it.id == id } }
                )
            }
        }
    }

    // If no saved outfit was chosen, generate a random one
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
