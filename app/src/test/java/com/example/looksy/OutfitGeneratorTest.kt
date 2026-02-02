package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.ColorCompatibility
import com.example.looksy.util.generateRandomOutfit
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutfitGeneratorTest {

    private fun createClothes(
        id: Int,
        type: Type,
        color: String? = null,
        clean: Boolean = true
    ): Clothes = Clothes(
        id = id,
        size = Size._M,
        seasonUsage = Season.Summer,
        type = type,
        material = Material.Cotton,
        clean = clean,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "",
        isSynced = false,
        color = color
    )

    @Test
    fun `generateRandomOutfit returns outfit with at least top or dress when clothes available`() {
        val clothes = listOf(
            createClothes(1, Type.Tops),
            createClothes(2, Type.Pants)
        )
        repeat(20) {
            val result = generateRandomOutfit(clothes)
            assertTrue(result.top != null || result.dress != null)
        }
    }

    @Test
    fun `generateRandomOutfit only assembles color-matching outfits when anchor has color`() {
        val clothes = listOf(
            createClothes(1, Type.Tops, "Schwarz"),
            createClothes(2, Type.Pants, "Weiß"),
            createClothes(3, Type.Pants, "Rot")
        )
        repeat(30) {
            val result = generateRandomOutfit(clothes)
            if (result.top != null && result.pants != null) {
                assertTrue(
                    "Outfit should be color-matching: top=${result.top!!.color}, pants=${result.pants!!.color}",
                    ColorCompatibility.areCompatible(result.top!!.color, result.pants!!.color)
                )
            }
        }
    }

    @Test
    fun `generateRandomOutfit with only compatible colors always returns color-matching outfit`() {
        val clothes = listOf(
            createClothes(1, Type.Tops, "Schwarz"),
            createClothes(2, Type.Pants, "Weiß"),
            createClothes(3, Type.Jacket, "Braun")
        )
        repeat(20) {
            val result = generateRandomOutfit(clothes)
            val items = listOfNotNull(result.top, result.pants, result.skirt, result.jacket, result.dress)
            for (i in items.indices) {
                for (j in i + 1 until items.size) {
                    assertTrue(
                        "Items should be color-compatible: ${items[i].color} vs ${items[j].color}",
                        ColorCompatibility.areCompatible(items[i].color, items[j].color)
                    )
                }
            }
        }
    }

    @Test
    fun `generateRandomOutfit allows items with no color when anchor has color`() {
        val clothes = listOf(
            createClothes(1, Type.Tops, "Schwarz"),
            createClothes(2, Type.Pants, null)
        )
        repeat(20) {
            val result = generateRandomOutfit(clothes)
            if (result.top != null && result.pants != null) {
                assertTrue(ColorCompatibility.areCompatible(result.top!!.color, result.pants!!.color))
            }
        }
    }

    @Test
    fun `generateRandomOutfit when anchor has no color can pick any color`() {
        val clothes = listOf(
            createClothes(1, Type.Tops, null),
            createClothes(2, Type.Pants, "Rot"),
            createClothes(3, Type.Pants, "Schwarz")
        )
        var seenPants = false
        repeat(30) {
            val result = generateRandomOutfit(clothes)
            if (result.pants != null) seenPants = true
            if (result.top != null && result.pants != null) {
                assertTrue(ColorCompatibility.areCompatible(result.top!!.color, result.pants!!.color))
            }
        }
        assertTrue(seenPants)
    }
}
