package com.example.looksy.util

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type

data class OutfitResult(
    val top: Clothes?,
    val pants: Clothes?,
    val skirt: Clothes?,
    val jacket: Clothes?,
    val dress: Clothes?
)

private const val CANDIDATE_COUNT = 10

/**
 * Generates a single random outfit from clean clothes (used for candidates).
 */
private fun generateSingleRandomOutfit(cleanClothes: List<Clothes>): OutfitResult {
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

/**
 * Generates an outfit by creating multiple random candidates and returning
 * the one with the highest compatibility score (OutfitCompatibilityCalculator).
 */
fun generateRandomOutfit(allClothes: List<Clothes>): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    if (cleanClothes.isEmpty()) {
        return OutfitResult(null, null, null, null, null)
    }

    val candidates = (1..CANDIDATE_COUNT).map { generateSingleRandomOutfit(cleanClothes) }
    return candidates.maxByOrNull { OutfitCompatibilityCalculator.calculateCompatibilityScore(it) }
        ?: candidates.first()
}
