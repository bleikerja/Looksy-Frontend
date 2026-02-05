package com.example.looksy.util

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import kotlin.random.Random

data class OutfitResult(
    val top: Clothes?,
    val pants: Clothes?,
    val skirt: Clothes?,
    val jacket: Clothes?,
    val dress: Clothes?
)

fun generateRandomOutfit(allClothes: List<Clothes>): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    val clothesWeightedByWorn = cleanClothes.flatMap { c -> List(c.wornClothes+1) {c} }
    val finalClothes = if (Random.nextDouble() < 0.7) clothesWeightedByWorn else cleanClothes

    val searchForTops = listOf(true, false).random()
    var randomTop: Clothes? = null
    var randomDress: Clothes? = null

    if (searchForTops) {
        randomTop = finalClothes.filter { it.type == Type.Tops }.randomOrNull()
    } else {
        randomDress = finalClothes.filter { it.type == Type.Dress }.randomOrNull()
    }

    if (randomTop == null && randomDress == null) {
        if (searchForTops) {
            randomDress = finalClothes.filter { it.type == Type.Dress }.randomOrNull()
        } else {
            randomTop = finalClothes.filter { it.type == Type.Tops }.randomOrNull()
        }
    }

    val randomPants = finalClothes.filter { it.type == Type.Pants }.randomOrNull()
    val randomSkirt = finalClothes.filter { it.type == Type.Skirt }.randomOrNull()
    val randomJacket = finalClothes.filter { it.type == Type.Jacket }.randomOrNull()

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
