package com.example.looksy

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.generateRandomOutfit
import org.junit.Rule
import org.junit.Test

class OutfitGeneratorTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val testTopOftenWorn = Clothes(
        id = 1,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false,
        wornClothes = 5
    )

    private val testTopRarelyWorn = Clothes(
        id = 2,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false,
        wornClothes = 1
    )

    private val testPants = Clothes(
        id = 3,
        type = Type.Pants,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false,
        wornClothes = 2
    )

    val clothes = listOf(testTopOftenWorn, testTopRarelyWorn, testPants)

    @Test
    fun oftenWornClothesGetGeneratedMoreOften() {
        val counts = mutableMapOf<Int, Int>()

        repeat(10000) {
            val outfit = generateRandomOutfit(clothes)
            val top = outfit.top ?: return@repeat
            counts[top.id] = counts.getOrDefault(top.id, 0) + 1
        }

        val oftenCount = counts[1] ?: 0
        val rareCount = counts[2] ?: 0

        assert(oftenCount > rareCount)
    }

}