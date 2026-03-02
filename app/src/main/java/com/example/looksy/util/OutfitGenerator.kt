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
    val dress: Clothes?,
    val pullover: Clothes? = null,
    val shoes: Clothes? = null
)

private const val MAX_ATTEMPTS = 80

fun generateSingleRandomOutfit(allClothes: List<Clothes>, allOutfits: List<Outfit>): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    val clothesWeightedByWorn = cleanClothes.flatMap { c -> List(c.wornClothes+1) {c} }
    val finalClothes = if (Random.nextDouble() < 0.7) clothesWeightedByWorn else cleanClothes
    val cleanClothesIds = cleanClothes.map { it.id }.toSet()

    // With a 40% probability, try to use a saved outfit
    if (allOutfits.isNotEmpty() && Random.nextDouble() < 0.3) {
        val cleanOutfits = allOutfits.filter { outfit ->
            (outfit.topsId == null || outfit.topsId in cleanClothesIds) &&
                    (outfit.pulloverId == null || outfit.pulloverId in cleanClothesIds) &&
                    (outfit.pantsId == null || outfit.pantsId in cleanClothesIds) &&
                    (outfit.skirtId == null || outfit.skirtId in cleanClothesIds) &&
                    (outfit.jacketId == null || outfit.jacketId in cleanClothesIds) &&
                    (outfit.dressId == null || outfit.dressId in cleanClothesIds) &&
                    (outfit.shoesId == null || outfit.shoesId in cleanClothesIds)
        }

        if (cleanOutfits.isNotEmpty()) {
            val outfitsWeightedByPreference =
                cleanOutfits.flatMap { c -> List(c.preference + 1) { c } }
            if (outfitsWeightedByPreference.isNotEmpty()) {
                val savedOutfit = outfitsWeightedByPreference.random()
                val candidate = OutfitResult(
                    top = savedOutfit.topsId?.let { id -> cleanClothes.find { it.id == id } },
                    pants = savedOutfit.pantsId?.let { id -> cleanClothes.find { it.id == id } },
                    skirt = savedOutfit.skirtId?.let { id -> cleanClothes.find { it.id == id } },
                    jacket = savedOutfit.jacketId?.let { id -> cleanClothes.find { it.id == id } },
                    dress = savedOutfit.dressId?.let { id -> cleanClothes.find { it.id == id } },
                    pullover = savedOutfit.pulloverId?.let { id -> cleanClothes.find { it.id == id } },
                    shoes = savedOutfit.shoesId?.let { id -> cleanClothes.find { it.id == id } }
                )
                if (OutfitCompatibilityCalculator.calculateCompatibilityScore(candidate) > 0) {
                    return candidate
                }
                // else fall through to random generation
            }
        }
    }

    // If no saved outfit was chosen, generate a random one
    val searchForTops = listOf(true, false).random()
    var randomTop: Clothes? = null
    var randomPullover: Clothes? = null
    var randomDress: Clothes? = null

    if (searchForTops) {
        // Randomly pick either TShirt or Pullover
        val tshirts = finalClothes.filter { it.type == Type.TShirt }
        val pullovers = finalClothes.filter { it.type == Type.Pullover }
        if (tshirts.isNotEmpty() && pullovers.isNotEmpty()) {
            if (Random.nextBoolean()) {
                randomTop = tshirts.randomOrNull()
            } else {
                randomPullover = pullovers.randomOrNull()
            }
        } else {
            randomTop = tshirts.randomOrNull()
            randomPullover = pullovers.randomOrNull()
        }
    } else {
        randomDress = finalClothes.filter { it.type == Type.Dress }.randomOrNull()
    }

    if (randomTop == null && randomPullover == null && randomDress == null) {
        if (searchForTops) {
            randomDress = finalClothes.filter { it.type == Type.Dress }.randomOrNull()
        } else {
            val tshirts = finalClothes.filter { it.type == Type.TShirt }
            val pullovers = finalClothes.filter { it.type == Type.Pullover }
            randomTop = tshirts.randomOrNull()
            if (randomTop == null) randomPullover = pullovers.randomOrNull()
        }
    }

    val randomPants = finalClothes.filter { it.type == Type.Pants }.randomOrNull()
    val randomSkirt = finalClothes.filter { it.type == Type.Skirt }.randomOrNull()
    val randomJacket = finalClothes.filter { it.type == Type.Jacket }.randomOrNull()
    val randomShoes = finalClothes.filter { it.type == Type.Shoes }.randomOrNull()

    // Do not combine pants and skirt; if dress, no pants/skirt
    var finalPants: Clothes? = randomPants
    var finalSkirt: Clothes? = randomSkirt
    if (randomDress != null) {
        finalPants = null
        finalSkirt = null
        randomTop = null
        randomPullover = null
    } else if (randomPants != null && randomSkirt != null) {
        if (Random.nextBoolean()) finalPants = null else finalSkirt = null
    }

    return OutfitResult(
        top = randomTop,
        pants = finalPants,
        skirt = finalSkirt,
        jacket = randomJacket,
        dress = randomDress,
        pullover = randomPullover,
        shoes = randomShoes
    )
}

/**
 * Generates an outfit by creating up to MAX_ATTEMPTS random candidates and returning
 * the one with the highest compatibility score. Only color-compatible outfits (score > 0) are kept.
 * If no compatible candidate is found, returns an empty OutfitResult.
 */
fun generateRandomOutfit(allClothes: List<Clothes>, allOutfits: List<Outfit>): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    if (cleanClothes.isEmpty()) {
        return OutfitResult(null, null, null, null, null)
    }

    val candidates = (1..MAX_ATTEMPTS).map { generateSingleRandomOutfit(cleanClothes, allOutfits) }
    val compatible = candidates.filter { OutfitCompatibilityCalculator.calculateCompatibilityScore(it) > 0 }
    return compatible.maxByOrNull { OutfitCompatibilityCalculator.calculateCompatibilityScore(it) }
        ?: candidates.random()
}
