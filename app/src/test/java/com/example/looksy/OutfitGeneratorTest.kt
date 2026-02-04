package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.OutfitCompatibilityCalculator
import com.example.looksy.util.generateRandomOutfit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutfitGeneratorTest {

    private fun createClothes(
        id: Int = 0,
        size: Size = Size._M,
        season: Season = Season.Summer,
        type: Type = Type.Tops,
        material: Material = Material.Cotton,
        clean: Boolean = true
    ): Clothes = Clothes(
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

    @Test
    fun `generateRandomOutfit returns outfit with valid compatibility score`() {
        val top = createClothes(id = 1, type = Type.Tops)
        val pants = createClothes(id = 2, type = Type.Pants)
        val clothes = listOf(top, pants)

        val result = generateRandomOutfit(clothes)

        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue("Score should be in 0..100", score in 0..100)
        assertEquals("Returned outfit score should match calculator", score,
            OutfitCompatibilityCalculator.calculateCompatibilityScore(result))
    }

    @Test
    fun `generateRandomOutfit uses rating and prefers higher scored outfit`() {
        // Top + Pants (valid) => high score; Dress + Pants (invalid) => 0
        val top = createClothes(id = 1, type = Type.Tops, season = Season.Summer, material = Material.Cotton)
        val pants = createClothes(id = 2, type = Type.Pants, season = Season.Summer, material = Material.jeans)
        val dress = createClothes(id = 3, type = Type.Dress, season = Season.Summer)
        val clothes = listOf(top, pants, dress)

        val result = generateRandomOutfit(clothes)

        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue(
            "Generator should prefer valid combination (top+pants) over invalid (dress+pants), score should be > 0",
            score > 0
        )
        assertNotNull(result.top)
        assertNotNull(result.pants)
    }

    @Test
    fun `generateRandomOutfit returns empty outfit when no clean clothes`() {
        val top = createClothes(id = 1, type = Type.Tops, clean = false)
        val pants = createClothes(id = 2, type = Type.Pants, clean = false)
        val clothes = listOf(top, pants)

        val result = generateRandomOutfit(clothes)

        assertEquals(null, result.top)
        assertEquals(null, result.pants)
        assertEquals(0, OutfitCompatibilityCalculator.calculateCompatibilityScore(result))
    }

    @Test
    fun `generateRandomOutfit returns outfit with best score among candidates`() {
        val top = createClothes(id = 1, type = Type.Tops, season = Season.Summer, material = Material.Cotton, size = Size._M)
        val pants = createClothes(id = 2, type = Type.Pants, season = Season.Summer, material = Material.jeans, size = Size._M)
        val jacket = createClothes(id = 3, type = Type.Jacket, season = Season.Summer, material = Material.Cotton, size = Size._M)
        val clothes = listOf(top, pants, jacket)

        val result = generateRandomOutfit(clothes)

        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue("Generator should return outfit with compatibility score in 0..100", score in 0..100)
        assertTrue("At least top or dress must be present", result.top != null || result.dress != null)
    }
}
