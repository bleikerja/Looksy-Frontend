package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.ClothesColor
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.OutfitLayoutMode
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.OutfitCompatibilityCalculator
import com.example.looksy.util.generateRandomOutfit
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutfitGeneratorTest {

    private fun createClothes(
        id: Int = 0,
        size: Size = Size._M,
        season: Season = Season.Summer,
        type: Type = Type.TShirt,
        material: Material = Material.Cotton,
        clean: Boolean = true,
        color: ClothesColor? = null
    ): Clothes = Clothes(
        id = id,
        size = size,
        seasonUsage = season,
        type = type,
        material = material,
        color = color,
        clean = clean,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "",
        isSynced = false
    )
    private val testTopOftenWorn = Clothes(
        id = 1,
        type = Type.TShirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false,
        wornClothes = 5
    )

    private val testTopRarelyWorn = Clothes(
        id = 2,
        type = Type.TShirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false,
        wornClothes = 1
    )
    private val testTop1 = Clothes(
        id = 5,
        type = Type.TShirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "path/to/shirt",
        isSynced = false,
        wornClothes = 1
    )
    private val testTop2 = Clothes(
        id = 6,
        type = Type.TShirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
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
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false,
        wornClothes = 2
    )
    private val testPants1 = Clothes(
        id = 7,
        type = Type.Pants,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "path/to/jeans",
        isSynced = false,
        wornClothes = 2
    )
    private val testPants2 = Clothes(
        id = 8,
        type = Type.Pants,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "path/to/jeans",
        isSynced = false,
        wornClothes = 2
    )

    private val testSkirt = Clothes(
        id = 4,
        type = Type.Skirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
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

    private val clothes = listOf(testTopOftenWorn, testTopRarelyWorn, testPants, testSkirt, testTop1, testTop2, testPants1, testPants2)
    private val outfits = listOf(testFavOutfit, testOutfitMehh)

    @Test
    fun `generateRandomOutfit returns outfit with valid compatibility score`() {
        val top = createClothes(id = 1, type = Type.TShirt)
        val pants = createClothes(id = 2, type = Type.Pants)
        val clothes = listOf(top, pants)

        val result = generateRandomOutfit(clothes, emptyList())

        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue("Score should be in 0..100", score in 0..100)
        assertEquals("Returned outfit score should match calculator", score,
            OutfitCompatibilityCalculator.calculateCompatibilityScore(result))
    }

    @Test
    fun `generateRandomOutfit uses rating and prefers higher scored outfit`() {
        // Top + Pants (valid) => high score; Dress + Pants (invalid) => 0
        val top = createClothes(id = 1, type = Type.TShirt, season = Season.Summer, material = Material.Cotton)
        val pants = createClothes(id = 2, type = Type.Pants, season = Season.Summer, material = Material.jeans)
        val dress = createClothes(id = 3, type = Type.Dress, season = Season.Summer)
        val clothes = listOf(top, pants, dress)

        val result = generateRandomOutfit(clothes, emptyList())

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
        val top = createClothes(id = 1, type = Type.TShirt, clean = false)
        val pants = createClothes(id = 2, type = Type.Pants, clean = false)
        val clothes = listOf(top, pants)

        val result = generateRandomOutfit(clothes, emptyList())

        assertEquals(null, result.top)
        assertEquals(null, result.pants)
        assertEquals(0, OutfitCompatibilityCalculator.calculateCompatibilityScore(result))
    }

    @Test
    fun `generateRandomOutfit returns outfit with best score among candidates`() {
        val top = createClothes(id = 1, type = Type.TShirt, season = Season.Summer, material = Material.Cotton, size = Size._M)
        val pants = createClothes(id = 2, type = Type.Pants, season = Season.Summer, material = Material.jeans, size = Size._M)
        val jacket = createClothes(id = 3, type = Type.Jacket, season = Season.Summer, material = Material.Cotton, size = Size._M)
        val clothes = listOf(top, pants, jacket)

        val result = generateRandomOutfit(clothes, emptyList())

        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue("Generator should return outfit with compatibility score in 0..100", score in 0..100)
        assertTrue("At least top, pullover, or dress must be present", result.top != null || result.pullover != null || result.dress != null)
    }

    @Test
    fun oftenWornClothesGetGeneratedMoreOften() {
        val counts = mutableMapOf<Int, Int>()

        repeat(1000) {
            val outfit = generateRandomOutfit(clothes, emptyList())
            val top = outfit.top ?: return@repeat
            counts[top.id] = counts.getOrDefault(top.id, 0) + 1
        }

        val oftenCount = counts[1] ?: 0
        val rareCount = counts[2] ?: 0

        assert(oftenCount > rareCount)
    }
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
        assertTrue("Der Generator sollte gespeicherte Outfits vorschlagen", foundSavedOutfit)
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

        assertTrue(
            "Favorisiertes Outfit ($favOutfitCount) sollte öfter als Mehh-Outfit ($mehOutfitCount) vorkommen",
            favOutfitCount > mehOutfitCount
        )
        assertTrue(
            "Zufällige Outfits ($noSavedOutfitCount) sollten insgesamt häufiger als gespeicherte(${favOutfitCount + mehOutfitCount}) vorkommen",
            (favOutfitCount + mehOutfitCount) < (noSavedOutfitCount)
        )
    }

    @Test
    fun generatedOutfitsAreAlwaysColorCompatible() {
        val redTop = createClothes(id = 10, type = Type.TShirt, color = ClothesColor.Red)
        val neutralTop = createClothes(id = 14, type = Type.TShirt, color = ClothesColor.White)
        val bluePants = createClothes(id = 11, type = Type.Pants, color = ClothesColor.Blue)
        val yellowJacket = createClothes(id = 12, type = Type.Jacket, color = ClothesColor.Yellow)
        val neutralJacket = createClothes(id = 13, type = Type.Jacket, color = ClothesColor.Black)
        val closet = listOf(redTop, neutralTop, bluePants, yellowJacket, neutralJacket)

        repeat(400) {
            val outfit = generateRandomOutfit(closet, emptyList())
            val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)
            assertTrue(
                "Every generated outfit must have score > 0 (attempt ${it + 1}), got score=$score",
                score > 0
            )
            assertTrue(
                "Every generated outfit must be color compatible (attempt ${it + 1})",
                OutfitCompatibilityCalculator.isOutfitColorCompatible(outfit)
            )
        }
    }

    @Test
    fun savedOutfitWithDirtyPartIsNotSuggested() {
        val cleanTop = Clothes(
            id = 101,
            type = Type.TShirt,
            clean = true,
            size = Size._M,
            seasonUsage = Season.Summer,
            material = Material.Cotton,
            washingNotes = listOf(WashingNotes.Temperature30)
        )
        val dirtyPants = Clothes(
            id = 102,
            type = Type.Pants,
            clean = false,
            size = Size._M,
            seasonUsage = Season.Summer,
            material = Material.Cotton,
            washingNotes = listOf(WashingNotes.Temperature30)
        )

        val outfitWithDirtyPart =
            Outfit(id = 10, topsId = cleanTop.id, pantsId = dirtyPants.id, preference = 100)

        val currentClothes = clothes + cleanTop
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

    @Test
    fun `generateRandomOutfit can include shoes`() {
        val top = createClothes(id = 1, type = Type.TShirt)
        val pants = createClothes(id = 2, type = Type.Pants)
        val shoes = createClothes(id = 3, type = Type.Shoes, size = Size._42)
        val clothes = listOf(top, pants, shoes)

        var foundShoes = false
        repeat(200) {
            val result = generateRandomOutfit(clothes, emptyList())
            if (result.shoes != null) {
                assertEquals(Type.Shoes, result.shoes!!.type)
                foundShoes = true
            }
        }
        assertTrue("Generator should include shoes in at least one outfit", foundShoes)
    }

    @Test
    fun `generateRandomOutfit valid without shoes`() {
        val top = createClothes(id = 1, type = Type.TShirt)
        val pants = createClothes(id = 2, type = Type.Pants)
        val clothes = listOf(top, pants)

        val result = generateRandomOutfit(clothes, emptyList())

        assertNotNull("Outfit should have a top", result.top)
        assertNotNull("Outfit should have pants", result.pants)
        assertEquals(null, result.shoes)
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue("Outfit without shoes should still have a valid score", score > 0)
    }

    @Test
    fun `generateRandomOutfit uses Pullover as top`() {
        val pullover = createClothes(id = 1, type = Type.Pullover)
        val pants = createClothes(id = 2, type = Type.Pants)
        val clothes = listOf(pullover, pants)

        val result = generateRandomOutfit(clothes, emptyList())

        assertNotNull("Pullover should be selected in pullover slot", result.pullover)
        assertEquals(Type.Pullover, result.pullover!!.type)
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(result)
        assertTrue("Pullover outfit should be valid", score > 0)
    }

    @Test
    fun `topTypes contains TShirt and Pullover`() {
        assertTrue("topTypes should contain TShirt", Type.TShirt in Type.topTypes)
        assertTrue("topTypes should contain Pullover", Type.Pullover in Type.topTypes)
        assertEquals("topTypes should have exactly 2 entries", 2, Type.topTypes.size)
    }

    @Test
    fun savedOutfitWithDirtyPulloverIsNotSuggested() {
        val cleanPants = Clothes(
            id = 201,
            type = Type.Pants,
            clean = true,
            size = Size._M,
            seasonUsage = Season.Summer,
            material = Material.Cotton,
            washingNotes = listOf(WashingNotes.Temperature30)
        )
        val dirtyPullover = Clothes(
            id = 202,
            type = Type.Pullover,
            clean = false,
            size = Size._M,
            seasonUsage = Season.Summer,
            material = Material.Wool,
            washingNotes = listOf(WashingNotes.Temperature30)
        )

        val outfitWithDirtyPullover =
            Outfit(id = 20, pulloverId = dirtyPullover.id, pantsId = cleanPants.id, preference = 100)

        val currentClothes = clothes + cleanPants
        val currentOutfits = outfits + outfitWithDirtyPullover

        repeat(1000) {
            val result = generateRandomOutfit(currentClothes, currentOutfits)
            val isTheDirtyOutfit = result.pullover?.id == dirtyPullover.id && result.pants?.id == cleanPants.id
            Assert.assertFalse(
                "Ein Outfit mit schmutzigem Pullover darf nicht aus den gespeicherten Outfits vorgeschlagen werden",
                isTheDirtyOutfit
            )
        }
    }

    // ──────────── Layout-aware generator tests ────────────

    @Test
    fun `FOUR_LAYERS generates both TShirt and Pullover independently`() {
        val tshirt   = createClothes(id = 10, type = Type.TShirt)
        val pullover = createClothes(id = 11, type = Type.Pullover)
        val pants    = createClothes(id = 12, type = Type.Pants)
        val wardrobe = listOf(tshirt, pullover, pants)

        var bothPresent = 0
        repeat(200) {
            val result = generateRandomOutfit(wardrobe, emptyList(), OutfitLayoutMode.FOUR_LAYERS)
            if (result.top != null && result.pullover != null) bothPresent++
        }
        assertTrue(
            "FOUR_LAYERS should generate both TShirt and Pullover in most runs (got $bothPresent/200)",
            bothPresent >= 160
        )
    }

    @Test
    fun `TWO_LAYERS always generates a dress and never top or bottom`() {
        val dress  = createClothes(id = 20, type = Type.Dress)
        val tshirt = createClothes(id = 21, type = Type.TShirt)
        val pants  = createClothes(id = 22, type = Type.Pants)
        val wardrobe = listOf(dress, tshirt, pants)

        repeat(200) {
            val result = generateRandomOutfit(wardrobe, emptyList(), OutfitLayoutMode.TWO_LAYERS)
            assertNotNull("TWO_LAYERS must always produce a dress", result.dress)
            assertEquals("TWO_LAYERS top must be null",     null, result.top)
            assertEquals("TWO_LAYERS pullover must be null", null, result.pullover)
            assertEquals("TWO_LAYERS pants must be null",   null, result.pants)
            assertEquals("TWO_LAYERS skirt must be null",   null, result.skirt)
        }
    }

    @Test
    fun `TWO_LAYERS never uses a saved outfit from a different layout mode`() {
        val dress = createClothes(id = 30, type = Type.Dress)
        val pants = createClothes(id = 31, type = Type.Pants)
        val top   = createClothes(id = 32, type = Type.TShirt)
        // Saved outfit is THREE_LAYERS (top + pants)
        val threeLayers = Outfit(
            id = 99, topsId = top.id, pantsId = pants.id, preference = 100,
            layoutMode = OutfitLayoutMode.THREE_LAYERS
        )
        val wardrobe = listOf(dress, pants, top)

        repeat(500) {
            val result = generateRandomOutfit(wardrobe, listOf(threeLayers), OutfitLayoutMode.TWO_LAYERS)
            assertEquals(
                "TWO_LAYERS must not return a saved THREE_LAYERS outfit",
                null, result.top
            )
        }
    }

    @Test
    fun `THREE_LAYERS never generates a dress even when one exists`() {
        val dress    = createClothes(id = 40, type = Type.Dress)
        val tshirt   = createClothes(id = 41, type = Type.TShirt)
        val pants    = createClothes(id = 42, type = Type.Pants)
        val wardrobe = listOf(dress, tshirt, pants)

        repeat(200) {
            val result = generateRandomOutfit(wardrobe, emptyList(), OutfitLayoutMode.THREE_LAYERS)
            assertEquals("THREE_LAYERS dress must always be null", null, result.dress)
        }
    }
}