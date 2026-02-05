package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.generateRandomOutfit
import org.junit.Assert
import org.junit.Test

class OutfitGeneratorTest {

    // Die composeTestRule wurde entfernt, da sie für Logik-Tests in src/test nicht funktioniert

    private val testTopOftenWorn = Clothes(
        id = 1,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "path/to/shirt",
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
        imagePath = "path/to/shirt",
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
        imagePath = "path/to/jeans",
        isSynced = false,
        wornClothes = 2
    )

    private val testSkirt = Clothes(
        id = 4, // ID korrigiert (war vorher doppelt vergeben)
        type = Type.Skirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "path/to/skirt",
        isSynced = false,
        wornClothes = 3
    )

    private val testFavOutfit = Outfit(
        id = 1,
        topsId = testTopOftenWorn.id,
        pantsId = testPants.id,
        preference = 10
    )

    private val testOutfitMehh = Outfit(
        id = 2,
        topsId = testTopRarelyWorn.id,
        pantsId = testPants.id,
        preference = 1
    )

    private val clothes = listOf(testTopOftenWorn, testTopRarelyWorn, testPants, testSkirt)
    private val outfits = listOf(testFavOutfit, testOutfitMehh)

    @Test
    fun outfitsAreChosenImGenerator(){
        var foundSavedOutfit = false
        repeat(1000) {
            val outfit = generateRandomOutfit(clothes, outfits)
            if ((outfit.top?.id == testFavOutfit.topsId && outfit.pants?.id == testFavOutfit.pantsId)
                || (outfit.top?.id == testOutfitMehh.topsId && outfit.pants?.id == testOutfitMehh.pantsId)) {
                foundSavedOutfit = true
            }
        }
        Assert.assertTrue("Der Generator sollte gespeicherte Outfits vorschlagen", foundSavedOutfit)
    }

    @Test
    fun preferredOutfitsAreChosenMoreOften(){
        var favOutfitCount = 0
        var mehOutfitCount = 0
        var noSavedOutfitCount = 0
        val runs = 10000

        repeat(runs) {
            val outfit = generateRandomOutfit(clothes, outfits)
            val isFav = outfit.top?.id == testFavOutfit.topsId && outfit.pants?.id == testFavOutfit.pantsId
            val isMeh = outfit.top?.id == testOutfitMehh.topsId && outfit.pants?.id == testOutfitMehh.pantsId

            if (isFav) {
                favOutfitCount++
            } else if (isMeh) {
                mehOutfitCount++
            } else {
                noSavedOutfitCount++
            }
        }

        Assert.assertTrue(
            "Favorisiertes Outfit ($favOutfitCount) sollte öfter als Mehh-Outfit ($mehOutfitCount) vorkommen",
            favOutfitCount > mehOutfitCount
        )
        Assert.assertTrue(
            "Zufällige Outfits ($noSavedOutfitCount) sollten insgesamt häufiger als gespeicherte vorkommen",
            (favOutfitCount + mehOutfitCount) < noSavedOutfitCount
        )
    }

    @Test
    fun savedOutfitWithDirtyPartIsNotSuggested() {
        val cleanTop = Clothes(
            id = 101,
            type = Type.Tops,
            clean = true,
            size = Size._M,
            seasonUsage = Season.Summer,
            material = Material.Cotton,
            washingNotes = WashingNotes.Temperature30
        )
        val dirtyPants = Clothes(
            id = 102,
            type = Type.Pants,
            clean = false,
            size = Size._M,
            seasonUsage = Season.Summer,
            material = Material.Cotton,
            washingNotes = WashingNotes.Temperature30
        )

        val outfitWithDirtyPart =
            Outfit(id = 10, topsId = cleanTop.id, pantsId = dirtyPants.id, preference = 100)

        val currentClothes = clothes + cleanTop + dirtyPants
        val currentOutfits = outfits + outfitWithDirtyPart

        repeat(1000) {
            val result = generateRandomOutfit(currentClothes, currentOutfits)
            val isTheDirtyOutfit = result.top?.id == cleanTop.id && result.pants?.id == dirtyPants.id
            Assert.assertFalse(
                "Ein Outfit mit schmutzigen Teilen darf nicht aus den gespeicherten Outfits vorgeschlagen werden",
                isTheDirtyOutfit
            )
        }
    }
}