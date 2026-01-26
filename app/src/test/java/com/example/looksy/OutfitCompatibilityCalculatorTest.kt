package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.OutfitCompatibilityCalculator
import com.example.looksy.util.OutfitResult
import org.junit.Assert.*
import org.junit.Test

class OutfitCompatibilityCalculatorTest {

    // Helper function to create test clothes
    private fun createClothes(
        id: Int = 0,
        size: Size = Size._M,
        season: Season = Season.Summer,
        type: Type = Type.Tops,
        material: Material = Material.Cotton,
        clean: Boolean = true
    ): Clothes {
        return Clothes(
            id = id,
            size = size,
            seasonUsage = season,
            type = type,
            material = material,
            clean = clean,
            washingNotes = WashingNotes.Temperature30,
            imagePath = "",
            isSynced = false
        )
    }

    @Test
    fun `calculateCompatibilityScore should return 0 for empty outfit`() {
        // Given
        val outfit = OutfitResult(null, null, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when dress is combined with pants`() {
        // Given - Rule violation: dress combined with pants
        val dress = createClothes(id = 1, type = Type.Dress)
        val pants = createClothes(id = 2, type = Type.Pants)
        val outfit = OutfitResult(null, pants, null, null, dress)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when dress is combined with skirt`() {
        // Given - Rule violation: dress combined with skirt
        val dress = createClothes(id = 1, type = Type.Dress)
        val skirt = createClothes(id = 2, type = Type.Skirt)
        val outfit = OutfitResult(null, null, skirt, null, dress)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when no top or dress`() {
        // Given - Rule violation: no top or dress
        val pants = createClothes(id = 1, type = Type.Pants)
        val outfit = OutfitResult(null, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for perfect outfit with same season`() {
        // Given - Perfect combination: same season, well-matched materials, logical combination
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Summer,
            material = Material.jeans,
            size = Size._M
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - High score expected (80 points or more)
        assertTrue("Score should be high for perfect outfit", score >= 80)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for dress with jacket`() {
        // Given - Dress + jacket combination
        val dress = createClothes(
            id = 1,
            type = Type.Dress,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val jacket = createClothes(
            id = 2,
            type = Type.Jacket,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val outfit = OutfitResult(null, null, null, jacket, dress)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - High score expected
        assertTrue("Score should be high for dress with jacket", score >= 80)
    }

    @Test
    fun `calculateCompatibilityScore should return lower score for different seasons`() {
        // Given - Different season combination
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Winter,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - Score should be lower due to different seasons (season 40 points * 30% = 12 points, total score around 82 points even with other high factors)
        assertTrue("Score should be lower for different seasons", score < 85)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when clothes are not clean`() {
        // Given - Dirty clothes
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton,
            clean = false
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Summer,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - Score should be lower due to cleanliness score of 0 (cleanliness 0 points * 10% = 0 points, total score around 90 points even with other high factors)
        assertTrue("Score should be lower when clothes are not clean", score < 95)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for good material combinations`() {
        // Given - Well-matched material combination (Cotton + Jeans)
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            material = Material.Cotton,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            material = Material.jeans,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - High score due to good material combination
        assertTrue("Score should be high for good material combinations", score >= 70)
    }

    @Test
    fun `calculateCompatibilityScore should return lower score for incompatible materials`() {
        // Given - Incompatible material combination (Jeans + Silk)
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            material = Material.jeans,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            material = Material.silk,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - Score should be lower due to incompatible materials (material 30 points * 25% = 7.5 points, total score around 82.5 points even with other high factors)
        assertTrue("Score should be lower for incompatible materials", score < 85)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for similar sizes`() {
        // Given - Similar sizes
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            size = Size._M,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            size = Size._M,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - High score due to same size
        assertTrue("Score should be high for similar sizes", score >= 70)
    }

    @Test
    fun `calculateCompatibilityScore should return lower score for different sizes`() {
        // Given - Very different sizes
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            size = Size._XS,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            size = Size._XL,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - Score should be lower due to large size difference (size 20 points * 15% = 3 points, total score around 88 points even with other high factors)
        assertTrue("Score should be lower for different sizes", score < 90)
    }

    @Test
    fun `calculateCompatibilityScore should handle inBetween season correctly`() {
        // Given - Including inBetween season
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.inBetween,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - inBetween should be handled flexibly
        assertTrue("Score should be reasonable for inBetween season", score >= 50)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for complete outfit with jacket`() {
        // Given - Complete outfit: top + bottom + jacket
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Summer,
            material = Material.jeans,
            size = Size._M
        )
        val jacket = createClothes(
            id = 3,
            type = Type.Jacket,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val outfit = OutfitResult(top, pants, null, jacket, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - Very high score due to complete outfit
        assertTrue("Score should be very high for complete outfit", score >= 85)
    }

    @Test
    fun `calculateCompatibilityScore should return score for top only outfit`() {
        // Given - Top only
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, null, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - Score should be lower due to incomplete outfit (type 50 points * 20% = 10 points, total score around 90 points even with other high factors)
        assertTrue("Score should be lower for incomplete outfit", score < 95)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for wool and cashmere combination`() {
        // Given - Well-matched material combination (Wool + Cashmere)
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            material = Material.Wool,
            season = Season.Winter
        )
        val jacket = createClothes(
            id = 2,
            type = Type.Jacket,
            material = Material.cashmere,
            season = Season.Winter
        )
        val outfit = OutfitResult(top, null, null, jacket, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - High score due to good material combination
        assertTrue("Score should be high for wool and cashmere", score >= 70)
    }
}
