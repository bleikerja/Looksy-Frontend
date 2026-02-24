package com.example.looksy.util

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.ClothesColor
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type

object OutfitCompatibilityCalculator {
    
    /**
     * Calculates the compatibility score of an outfit (0-100 points)
     */
    fun calculateCompatibilityScore(outfit: OutfitResult): Int {
        val items = listOfNotNull(outfit.top, outfit.pants, outfit.skirt, outfit.jacket, outfit.dress)
        
        // Empty outfit gets 0 points
        if (items.isEmpty()) return 0
        
        // Validate required rules - returns 0 if rules are violated (includes color clash)
        if (!validateOutfitRules(outfit)) return 0

        val colorScore = calculateColorCompatibility(items)

        // Calculate scores for each attribute
        val seasonScore = calculateSeasonCompatibility(items)
        val materialScore = calculateMaterialCompatibility(items)
        val typeScore = calculateTypeCompatibility(outfit)
        val sizeScore = calculateSizeCompatibility(items)
        val cleanScore = calculateCleanScore(items)

        // Base score (weighted average, color not included in weights)
        val baseScore = (seasonScore * 0.30 +
                materialScore * 0.25 +
                typeScore * 0.20 +
                sizeScore * 0.15 +
                cleanScore * 0.10)

        // Color as slight multiplier: 0.85..1.0 so color never dominates but can reduce score
        val colorFactor = 0.85 + 0.15 * (colorScore / 100.0)
        val finalScore = baseScore * colorFactor

        return finalScore.toInt().coerceIn(0, 100)
    }
    
    /**
     * Returns true if the outfit passes color compatibility (no 3+ accent clash).
     * Used by generator to filter invalid outfits and by tests.
     */
    fun isOutfitColorCompatible(outfit: OutfitResult): Boolean {
        val items = listOfNotNull(outfit.top, outfit.pants, outfit.skirt, outfit.jacket, outfit.dress)
        return calculateColorCompatibility(items) > 0.0
    }

    /**
     * Color compatibility score 0..100. Returns 0 when >= 3 different ACCENT colors (clash).
     * null (unknown) is allowed but slightly penalized; NEUTRAL/EARTH are high; 0–2 ACCENT scored by hue pairs.
     */
    private fun calculateColorCompatibility(items: List<Clothes>): Double {
        if (items.isEmpty()) return 100.0

        val colors = items.map { it.color }
        val accentColors = colors.filterNotNull().filter { it.kind == ClothesColor.Kind.ACCENT }
        val distinctAccents = accentColors.distinct()

        // Hard rule: >= 3 different ACCENT colors => clash => 0
        if (distinctAccents.size >= 3) return 0.0

        // Slight penalty for null (uncertainty)
        val nullCount = colors.count { it == null }
        val nullPenalty = nullCount * 2.0 // up to ~2*5 = 10 points if many nulls

        return when (distinctAccents.size) {
            0 -> 100.0 - nullPenalty.coerceAtMost(10.0)  // no accent: high
            1 -> 98.0 - nullPenalty.coerceAtMost(5.0)    // one accent: high
            2 -> {
                val pairScore = getAccentPairScore(distinctAccents[0], distinctAccents[1])
                (pairScore - nullPenalty.coerceAtMost(5.0)).coerceAtLeast(0.0)
            }
            else -> 0.0
        }
    }

    /** Score for two accent colors: EXCELLENT 100, GOOD 90, other 75. */
    private fun getAccentPairScore(a: ClothesColor, b: ClothesColor): Double {
        val c1 = canonicalHue(a)
        val c2 = canonicalHue(b)
        val pair = setOf(c1, c2)

        val excellentPairs = setOf(
            setOf(ClothesColor.Blue, ClothesColor.Orange),
            setOf(ClothesColor.Red, ClothesColor.Green),
            setOf(ClothesColor.Yellow, ClothesColor.Purple),
            setOf(ClothesColor.Red, ClothesColor.Blue)
        )
        if (excellentPairs.any { it == pair }) return 100.0

        val goodPairs = setOf(
            setOf(ClothesColor.Red, ClothesColor.Orange),
            setOf(ClothesColor.Orange, ClothesColor.Yellow),
            setOf(ClothesColor.Yellow, ClothesColor.Green),
            setOf(ClothesColor.Green, ClothesColor.Blue),
            setOf(ClothesColor.Blue, ClothesColor.Purple),
            setOf(ClothesColor.Purple, ClothesColor.Red),
            setOf(ClothesColor.Blue, ClothesColor.Yellow)
        )
        if (goodPairs.any { it == pair }) return 90.0

        return 75.0
    }

    /** Normalize accent color for pairing (LightBlue->Blue, Burgundy/Pink->Red). */
    private fun canonicalHue(c: ClothesColor): ClothesColor = when (c) {
        ClothesColor.LightBlue -> ClothesColor.Blue
        ClothesColor.Burgundy, ClothesColor.Pink -> ClothesColor.Red
        else -> c
    }

    /**
     * Validates required outfit rules
     * - Dress cannot be worn with pants or skirt
     * - At least a top or dress must be present
     * - Color: no 3+ accent clash (isOutfitColorCompatible)
     */
    private fun validateOutfitRules(outfit: OutfitResult): Boolean {
        // Dress cannot be combined with pants or skirt
        if (outfit.dress != null && (outfit.pants != null || outfit.skirt != null)) {
            return false
        }

        // At least a top or dress must be present
        if (outfit.top == null && outfit.dress == null) {
            return false
        }

        if (!isOutfitColorCompatible(outfit)) return false

        return true
    }
    
    /**
     * Calculates season compatibility score (0-100 points)
     */
    private fun calculateSeasonCompatibility(items: List<Clothes>): Double {
        if (items.isEmpty()) return 0.0
        
        val seasons = items.map { it.seasonUsage }
        val uniqueSeasons = seasons.distinct()
        
        return when {
            // All items have the same season
            uniqueSeasons.size == 1 -> 100.0
            // Mostly matching (2 seasons, one is inBetween)
            uniqueSeasons.size == 2 && uniqueSeasons.contains(Season.inBetween) -> 70.0
            // Mixed (multiple seasons)
            uniqueSeasons.size == 2 -> 40.0
            // Completely different seasons
            else -> 20.0
        }
    }
    
    /**
     * Calculates material combination score (0-100 points)
     */
    private fun calculateMaterialCompatibility(items: List<Clothes>): Double {
        if (items.size < 2) return 100.0 // Perfect score if only one item
        
        var totalScore = 0.0
        var pairCount = 0
        
        // Calculate combination score for all item pairs
        for (i in items.indices) {
            for (j in i + 1 until items.size) {
                val score = getMaterialPairScore(items[i].material, items[j].material)
                totalScore += score
                pairCount++
            }
        }
        
        return if (pairCount > 0) totalScore / pairCount else 100.0
    }
    
    /**
     * Returns the combination score for two materials
     */
    private fun getMaterialPairScore(material1: Material, material2: Material): Double {
        // Same material is a perfect combination
        if (material1 == material2) return 100.0
        
        // Well-matching combinations
        val goodCombinations = setOf(
            setOf(Material.Cotton, Material.jeans),
            setOf(Material.Cotton, Material.linen),
            setOf(Material.Wool, Material.cashmere),
            setOf(Material.silk, Material.cashmere),
            setOf(Material.Polyester, Material.Cotton),
            setOf(Material.linen, Material.Cotton),
            setOf(Material.Wool, Material.Polyester)
        )
        
        val pair = setOf(material1, material2)
        if (goodCombinations.any { it == pair }) {
            return 100.0
        }
        
        // Neutral combinations
        val neutralMaterials = setOf(Material.Cotton, Material.Polyester)
        if (neutralMaterials.contains(material1) || neutralMaterials.contains(material2)) {
            return 60.0
        }
        
        // Incompatible combinations (e.g., fur and linen)
        val incompatiblePairs = setOf(
            setOf(Material.fur, Material.linen),
            setOf(Material.jeans, Material.silk)
        )
        if (incompatiblePairs.any { it == pair }) {
            return 30.0
        }
        
        // Default neutral score
        return 60.0
    }
    
    /**
     * Calculates type combination score (0-100 points)
     */
    private fun calculateTypeCompatibility(outfit: OutfitResult): Double {
        val hasTop = outfit.top != null
        val hasDress = outfit.dress != null
        val hasPants = outfit.pants != null
        val hasSkirt = outfit.skirt != null
        val hasJacket = outfit.jacket != null
        
        // Dress alone or dress + jacket is perfect
        if (hasDress && !hasPants && !hasSkirt) {
            return if (hasJacket) 100.0 else 90.0
        }
        
        // Top + bottom combination
        if (hasTop && (hasPants || hasSkirt)) {
            var score = 100.0
            // Additional points if jacket is present
            if (hasJacket) score = 100.0
            return score
        }
        
        // Only top or only bottom
        if (hasTop && !hasPants && !hasSkirt) {
            return 50.0
        }
        if (!hasTop && (hasPants || hasSkirt)) {
            return 50.0
        }
        
        return 60.0
    }
    
    /**
     * Calculates size consistency score (0-100 points)
     */
    private fun calculateSizeCompatibility(items: List<Clothes>): Double {
        if (items.size < 2) return 100.0
        
        val sizes = items.map { it.size }
        
        // Convert size to number for comparison
        fun sizeToNumber(size: Size): Int {
            return when (size) {
                Size._XS -> 1
                Size._S -> 2
                Size._M -> 3
                Size._L -> 4
                Size._XL -> 5
                Size._34 -> 1
                Size._36 -> 1
                Size._38 -> 2
                Size._40 -> 3
                Size._42 -> 4
                Size._44 -> 5
                Size._46 -> 6
                Size._48 -> 7
                Size._50 -> 8
                Size._52 -> 9
                Size._54 -> 10
                Size._56 -> 11
                Size._58 -> 12
                Size._60 -> 13
            }
        }
        
        val sizeNumbers = sizes.map { sizeToNumber(it) }
        val minSize = sizeNumbers.minOrNull() ?: 0
        val maxSize = sizeNumbers.maxOrNull() ?: 0
        val sizeDiff = maxSize - minSize
        
        return when {
            sizeDiff == 0 -> 100.0 // All same size
            sizeDiff <= 1 -> 80.0  // 1 step difference
            sizeDiff <= 2 -> 60.0  // 2 step difference
            sizeDiff <= 3 -> 40.0  // 3 step difference
            else -> 20.0           // 4+ step difference
        }
    }
    
    /**
     * Calculates cleanliness score (0-100 points)
     */
    private fun calculateCleanScore(items: List<Clothes>): Double {
        if (items.isEmpty()) return 0.0
        
        val allClean = items.all { it.clean }
        return if (allClean) 100.0 else 0.0
    }
}
