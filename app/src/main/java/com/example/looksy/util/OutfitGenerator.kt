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

/**
 * Stellt nur farblich zusammenpassende Outfits zusammen.
 * Berücksichtigt die Farbe der Kleidungsstücke; kompatible Farbgruppen (z.B. Schwarz, Weiß, Braun)
 * werden über [ColorCompatibility] definiert.
 */
fun generateRandomOutfit(allClothes: List<Clothes>): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }

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

    val anchorItem = randomTop ?: randomDress
    val allowedColors = anchorItem?.color?.let { ColorCompatibility.getAllowedColors(it) }

    fun colorOk(cloth: Clothes): Boolean =
        ColorCompatibility.isColorAllowed(cloth.color, allowedColors)

    val randomPants = cleanClothes.filter { it.type == Type.Pants }.filter(::colorOk).randomOrNull()
    val randomSkirt = cleanClothes.filter { it.type == Type.Skirt }.filter(::colorOk).randomOrNull()
    val randomJacket = cleanClothes.filter { it.type == Type.Jacket }.filter(::colorOk).randomOrNull()

    var finalSkirt = randomSkirt
    if (randomDress != null) {
        finalSkirt = null
    }

    val result = OutfitResult(
        top = randomTop,
        pants = randomPants,
        skirt = finalSkirt,
        jacket = randomJacket,
        dress = randomDress
    )
    return result
}
