package com.example.looksy.util

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.OutfitLayoutMode
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

/**
 * Produces a single random outfit candidate.
 *
 * @param layoutMode Controls which garment slots are eligible.
 *   - [OutfitLayoutMode.TWO_LAYERS]   → dress + optional jacket/shoes; no top/pullover/pants/skirt.
 *   - [OutfitLayoutMode.THREE_LAYERS] → XOR top/pullover, one of pants/skirt, optional jacket/shoes; no dress.
 *   - [OutfitLayoutMode.FOUR_LAYERS]  → both TShirt AND Pullover independently, one of pants/skirt, optional jacket/shoes; no dress.
 *   - [OutfitLayoutMode.GRID]         → all slots free (original layout-agnostic logic).
 */
fun generateSingleRandomOutfit(
    allClothes: List<Clothes>,
    allOutfits: List<Outfit>,
    layoutMode: OutfitLayoutMode = OutfitLayoutMode.THREE_LAYERS
): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    val clothesWeightedByWorn = cleanClothes.flatMap { c -> List(c.wornClothes + 1) { c } }
    val finalClothes = if (Random.nextDouble() < 0.7) clothesWeightedByWorn else cleanClothes
    val cleanClothesIds = cleanClothes.map { it.id }.toSet()

    // With 30% probability, try to re-use a saved outfit that matches the current layout mode.
    // Outfits saved in a different layout are skipped to avoid slot mismatches.
    if (allOutfits.isNotEmpty() && Random.nextDouble() < 0.3) {
        val cleanOutfits = allOutfits.filter { outfit ->
            outfit.layoutMode == layoutMode &&
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
                    top      = savedOutfit.topsId?.let     { id -> cleanClothes.find { it.id == id } },
                    pants    = savedOutfit.pantsId?.let    { id -> cleanClothes.find { it.id == id } },
                    skirt    = savedOutfit.skirtId?.let    { id -> cleanClothes.find { it.id == id } },
                    jacket   = savedOutfit.jacketId?.let   { id -> cleanClothes.find { it.id == id } },
                    dress    = savedOutfit.dressId?.let    { id -> cleanClothes.find { it.id == id } },
                    pullover = savedOutfit.pulloverId?.let { id -> cleanClothes.find { it.id == id } },
                    shoes    = savedOutfit.shoesId?.let    { id -> cleanClothes.find { it.id == id } }
                )
                if (OutfitCompatibilityCalculator.calculateCompatibilityScore(candidate) > 0) {
                    return candidate
                }
                // else fall through to fresh random generation
            }
        }
    }

    // Per-layout random generation
    return when (layoutMode) {
        OutfitLayoutMode.TWO_LAYERS   -> generateTwoLayers(finalClothes)
        OutfitLayoutMode.THREE_LAYERS -> generateThreeLayers(finalClothes)
        OutfitLayoutMode.FOUR_LAYERS  -> generateFourLayers(finalClothes)
        OutfitLayoutMode.GRID         -> generateGrid(finalClothes)
    }
}

/**
 * TWO_LAYERS: dress + optional jacket + optional shoes.
 * All top/pullover/pants/skirt slots are forced null.
 */
private fun generateTwoLayers(finalClothes: List<Clothes>): OutfitResult {
    val dress  = finalClothes.filter { it.type == Type.Dress }.randomOrNull()
    val jacket = finalClothes.filter { it.type == Type.Jacket }.randomOrNull()
    val shoes  = finalClothes.filter { it.type == Type.Shoes }.randomOrNull()
    return OutfitResult(
        top = null, pullover = null, pants = null, skirt = null,
        dress = dress, jacket = jacket, shoes = shoes
    )
}

/**
 * THREE_LAYERS: XOR pick one of TShirt/Pullover, one of Pants/Skirt,
 * optional jacket + shoes. Dress is always null.
 */
private fun generateThreeLayers(finalClothes: List<Clothes>): OutfitResult {
    var randomTop: Clothes? = null
    var randomPullover: Clothes? = null
    val tshirts   = finalClothes.filter { it.type == Type.TShirt }
    val pullovers = finalClothes.filter { it.type == Type.Pullover }
    if (tshirts.isNotEmpty() && pullovers.isNotEmpty()) {
        if (Random.nextBoolean()) randomTop = tshirts.randomOrNull()
        else randomPullover = pullovers.randomOrNull()
    } else {
        randomTop = tshirts.randomOrNull()
        randomPullover = pullovers.randomOrNull()
    }

    val randomPants  = finalClothes.filter { it.type == Type.Pants }.randomOrNull()
    val randomSkirt  = finalClothes.filter { it.type == Type.Skirt }.randomOrNull()
    val randomJacket = finalClothes.filter { it.type == Type.Jacket }.randomOrNull()
    val randomShoes  = finalClothes.filter { it.type == Type.Shoes }.randomOrNull()

    var finalPants: Clothes? = randomPants
    var finalSkirt: Clothes? = randomSkirt
    if (randomPants != null && randomSkirt != null) {
        if (Random.nextBoolean()) finalPants = null else finalSkirt = null
    }

    return OutfitResult(
        top = randomTop, pullover = randomPullover,
        pants = finalPants, skirt = finalSkirt,
        jacket = randomJacket, dress = null, shoes = randomShoes
    )
}

/**
 * FOUR_LAYERS: independently pick both a TShirt AND a Pullover (so both carousels populate),
 * one of Pants/Skirt, optional jacket + shoes. Dress is always null.
 * If a garment type is absent from the wardrobe the slot stays null gracefully.
 */
private fun generateFourLayers(finalClothes: List<Clothes>): OutfitResult {
    val randomTop      = finalClothes.filter { it.type == Type.TShirt }.randomOrNull()
    val randomPullover = finalClothes.filter { it.type == Type.Pullover }.randomOrNull()
    val randomPants    = finalClothes.filter { it.type == Type.Pants }.randomOrNull()
    val randomSkirt    = finalClothes.filter { it.type == Type.Skirt }.randomOrNull()
    val randomJacket   = finalClothes.filter { it.type == Type.Jacket }.randomOrNull()
    val randomShoes    = finalClothes.filter { it.type == Type.Shoes }.randomOrNull()

    var finalPants: Clothes? = randomPants
    var finalSkirt: Clothes? = randomSkirt
    if (randomPants != null && randomSkirt != null) {
        if (Random.nextBoolean()) finalPants = null else finalSkirt = null
    }

    return OutfitResult(
        top = randomTop, pullover = randomPullover,
        pants = finalPants, skirt = finalSkirt,
        jacket = randomJacket, dress = null, shoes = randomShoes
    )
}

/**
 * GRID: all slots independent — same logic as the original layout-agnostic generator.
 * Dress still excludes tops/bottoms; pants and skirt remain mutually exclusive.
 */
private fun generateGrid(finalClothes: List<Clothes>): OutfitResult {
    val searchForTops = listOf(true, false).random()
    var randomTop: Clothes? = null
    var randomPullover: Clothes? = null
    var randomDress: Clothes? = null

    if (searchForTops) {
        val tshirts   = finalClothes.filter { it.type == Type.TShirt }
        val pullovers = finalClothes.filter { it.type == Type.Pullover }
        if (tshirts.isNotEmpty() && pullovers.isNotEmpty()) {
            if (Random.nextBoolean()) randomTop = tshirts.randomOrNull()
            else randomPullover = pullovers.randomOrNull()
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
            val tshirts   = finalClothes.filter { it.type == Type.TShirt }
            val pullovers = finalClothes.filter { it.type == Type.Pullover }
            randomTop = tshirts.randomOrNull()
            if (randomTop == null) randomPullover = pullovers.randomOrNull()
        }
    }

    val randomPants  = finalClothes.filter { it.type == Type.Pants }.randomOrNull()
    val randomSkirt  = finalClothes.filter { it.type == Type.Skirt }.randomOrNull()
    val randomJacket = finalClothes.filter { it.type == Type.Jacket }.randomOrNull()
    val randomShoes  = finalClothes.filter { it.type == Type.Shoes }.randomOrNull()

    var finalPants: Clothes? = randomPants
    var finalSkirt: Clothes? = randomSkirt
    if (randomDress != null) {
        finalPants = null; finalSkirt = null; randomTop = null; randomPullover = null
    } else if (randomPants != null && randomSkirt != null) {
        if (Random.nextBoolean()) finalPants = null else finalSkirt = null
    }

    return OutfitResult(
        top = randomTop, pullover = randomPullover,
        pants = finalPants, skirt = finalSkirt,
        jacket = randomJacket, dress = randomDress, shoes = randomShoes
    )
}

/**
 * Generates an outfit by creating up to [MAX_ATTEMPTS] random candidates and returning
 * the one with the highest compatibility score. Only color-compatible outfits (score > 0) are kept.
 * If no compatible candidate is found, returns a random candidate anyway.
 *
 * @param layoutMode Controls which slots are eligible — forwarded to [generateSingleRandomOutfit].
 */
fun generateRandomOutfit(
    allClothes: List<Clothes>,
    allOutfits: List<Outfit>,
    layoutMode: OutfitLayoutMode = OutfitLayoutMode.THREE_LAYERS
): OutfitResult {
    val cleanClothes = allClothes.filter { it.clean }
    if (cleanClothes.isEmpty()) {
        return OutfitResult(null, null, null, null, null)
    }

    val candidates = (1..MAX_ATTEMPTS).map { generateSingleRandomOutfit(cleanClothes, allOutfits, layoutMode) }
    val compatible = candidates.filter { OutfitCompatibilityCalculator.calculateCompatibilityScore(it) > 0 }
    return compatible.maxByOrNull { OutfitCompatibilityCalculator.calculateCompatibilityScore(it) }
        ?: candidates.random()
}
