package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.OutfitLayoutMode
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import org.junit.Assert.*
import org.junit.Test

class SavedOutfitsScreenTest {

    private val testClothes = listOf(
        Clothes(
            id = 1,
            type = Type.TShirt,
            size = Size._M,
            material = Material.Cotton,
            washingNotes = listOf(WashingNotes.Temperature30),
            clean = true,
            seasonUsage = Season.Summer,
            imagePath = "path/to/top.jpg"
        ),
        Clothes(
            id = 2,
            type = Type.Pants,
            size = Size._M,
            material = Material.jeans,
            washingNotes = listOf(WashingNotes.Temperature40),
            clean = true,
            seasonUsage = Season.inBetween,
            imagePath = "path/to/pants.jpg"
        ),
        Clothes(
            id = 3,
            type = Type.Dress,
            size = Size._S,
            material = Material.silk,
            washingNotes = listOf(WashingNotes.Temperature30),
            clean = true,
            seasonUsage = Season.Summer,
            imagePath = "path/to/dress.jpg"
        ),
        Clothes(
            id = 4,
            type = Type.Jacket,
            size = Size._L,
            material = Material.Cotton,
            washingNotes = listOf(WashingNotes.Temperature40),
            clean = true,
            seasonUsage = Season.Winter,
            imagePath = "path/to/jacket.jpg"
        ),
        Clothes(
            id = 5,
            type = Type.Pullover,
            size = Size._M,
            material = Material.Wool,
            washingNotes = listOf(WashingNotes.Temperature30),
            clean = true,
            seasonUsage = Season.Winter,
            imagePath = "path/to/pullover.jpg"
        )
    )

    @Test
    fun `empty outfits list should be handled correctly`() {
        // Given
        val outfits = emptyList<Outfit>()

        // When / Then
        assertTrue(outfits.isEmpty())
    }

    @Test
    fun `outfits list with items should not be empty`() {
        // Given
        val outfits = listOf(
            Outfit(id = 1, topsId = 1, pantsId = 2),
            Outfit(id = 2, dressId = 3)
        )

        // When / Then
        assertFalse(outfits.isEmpty())
        assertEquals(2, outfits.size)
    }

    @Test
    fun `outfit should correctly reference clothes by id`() {
        // Given
        val outfit = Outfit(id = 1, topsId = 1, pantsId = 2, jacketId = 4)

        // When
        val top = testClothes.find { it.id == outfit.topsId }
        val pants = testClothes.find { it.id == outfit.pantsId }
        val jacket = testClothes.find { it.id == outfit.jacketId }
        val dress = outfit.dressId?.let { id -> testClothes.find { it.id == id } }
        val pullover = outfit.pulloverId?.let { id -> testClothes.find { it.id == id } }

        // Then
        assertNotNull(top)
        assertNotNull(pants)
        assertNotNull(jacket)
        assertNull(dress)
        assertNull(pullover)
        assertEquals(Type.TShirt, top?.type)
        assertEquals(Type.Pants, pants?.type)
        assertEquals(Type.Jacket, jacket?.type)
    }

    @Test
    fun `outfit should correctly reference pullover by id`() {
        // Given
        val outfit = Outfit(id = 1, pulloverId = 5, pantsId = 2)

        // When
        val pullover = outfit.pulloverId?.let { id -> testClothes.find { it.id == id } }
        val pants = testClothes.find { it.id == outfit.pantsId }
        val top = outfit.topsId?.let { id -> testClothes.find { it.id == id } }

        // Then
        assertNotNull(pullover)
        assertNotNull(pants)
        assertNull(top)
        assertEquals(Type.Pullover, pullover?.type)
        assertEquals(Type.Pants, pants?.type)
    }

    @Test
    fun `outfit can contain only dress without top and pants`() {
        // Given
        val outfit = Outfit(id = 1, dressId = 3, topsId = null, pantsId = null)

        // When
        val dress = outfit.dressId?.let { id -> testClothes.find { it.id == id } }
        val top = outfit.topsId?.let { id -> testClothes.find { it.id == id } }
        val pants = outfit.pantsId?.let { id -> testClothes.find { it.id == id } }

        // Then
        assertNotNull(dress)
        assertNull(top)
        assertNull(pants)
        assertEquals(Type.Dress, dress?.type)
    }

    @Test
    fun `outfit clothes list should be in correct order`() {
        // Given
        val outfit = Outfit(
            id = 1,
            jacketId = 4,
            topsId = 1,
            pantsId = 2
        )

        // When - Simuliere die Reihenfolge wie im SavedOutfitsScreen
        val jacket = outfit.jacketId?.let { id -> testClothes.find { it.id == id } }
        val dress = outfit.dressId?.let { id -> testClothes.find { it.id == id } }
        val top = outfit.topsId?.let { id -> testClothes.find { it.id == id } }
        val pullover = outfit.pulloverId?.let { id -> testClothes.find { it.id == id } }
        val skirt = outfit.skirtId?.let { id -> testClothes.find { it.id == id } }
        val pants = outfit.pantsId?.let { id -> testClothes.find { it.id == id } }

        val clothesList = listOfNotNull(jacket, dress, top, pullover, skirt, pants)

        // Then - Die Reihenfolge sollte Jacket, Top, Pants sein
        assertEquals(3, clothesList.size)
        assertEquals(Type.Jacket, clothesList[0].type)
        assertEquals(Type.TShirt, clothesList[1].type)
        assertEquals(Type.Pants, clothesList[2].type)
    }

    @Test
    fun `outfit clothes list should include pullover in correct order`() {
        // Given - Outfit mit Pullover statt TShirt
        val outfit = Outfit(
            id = 1,
            jacketId = 4,
            pulloverId = 5,
            pantsId = 2
        )

        // When
        val jacket = outfit.jacketId?.let { id -> testClothes.find { it.id == id } }
        val dress = outfit.dressId?.let { id -> testClothes.find { it.id == id } }
        val top = outfit.topsId?.let { id -> testClothes.find { it.id == id } }
        val pullover = outfit.pulloverId?.let { id -> testClothes.find { it.id == id } }
        val skirt = outfit.skirtId?.let { id -> testClothes.find { it.id == id } }
        val pants = outfit.pantsId?.let { id -> testClothes.find { it.id == id } }

        val clothesList = listOfNotNull(jacket, dress, top, pullover, skirt, pants)

        // Then - Die Reihenfolge sollte Jacket, Pullover, Pants sein
        assertEquals(3, clothesList.size)
        assertEquals(Type.Jacket, clothesList[0].type)
        assertEquals(Type.Pullover, clothesList[1].type)
        assertEquals(Type.Pants, clothesList[2].type)
    }

    @Test
    fun `outfit with missing clothes ids should handle gracefully`() {
        // Given - Outfit mit IDs die nicht in der Kleidungsliste existieren
        val outfit = Outfit(id = 1, topsId = 999, pantsId = 888)

        // When
        val top = testClothes.find { it.id == outfit.topsId }
        val pants = testClothes.find { it.id == outfit.pantsId }
        val clothesList = listOfNotNull(top, pants)

        // Then
        assertTrue(clothesList.isEmpty())
    }

    @Test
    fun `multiple outfits should be distinct`() {
        // Given
        val outfits = listOf(
            Outfit(id = 1, topsId = 1, pantsId = 2),
            Outfit(id = 2, dressId = 3),
            Outfit(id = 3, topsId = 1, pantsId = 2, jacketId = 4)
        )

        // When
        val uniqueIds = outfits.map { it.id }.distinct()

        // Then
        assertEquals(3, uniqueIds.size)
        assertEquals(outfits.size, uniqueIds.size)
    }

    @Test
    fun `outfit click callback should receive correct id`() {
        // Given
        val outfit = Outfit(id = 42, topsId = 1, pantsId = 2)
        var clickedId: Int? = null
        val onOutfitClick: (Int) -> Unit = { id -> clickedId = id }

        // When
        onOutfitClick(outfit.id)

        // Then
        assertEquals(42, clickedId)
    }

    // ─── OutfitLayoutMode tests ───

    @Test
    fun `outfit default layoutMode should be THREE_LAYERS`() {
        val outfit = Outfit(id = 1, topsId = 1, pantsId = 2)
        assertEquals(OutfitLayoutMode.THREE_LAYERS, outfit.layoutMode)
    }

    @Test
    fun `outfit default isJacketVisible should be false`() {
        val outfit = Outfit(id = 1, topsId = 1, pantsId = 2)
        assertFalse(outfit.isJacketVisible)
    }

    @Test
    fun `outfit with TWO_LAYERS layout should store correctly`() {
        val outfit = Outfit(
            id = 1,
            dressId = 3,
            layoutMode = OutfitLayoutMode.TWO_LAYERS
        )
        assertEquals(OutfitLayoutMode.TWO_LAYERS, outfit.layoutMode)
        assertNotNull(outfit.dressId)
    }

    @Test
    fun `outfit with FOUR_LAYERS layout should store correctly`() {
        val outfit = Outfit(
            id = 1,
            topsId = 1,
            pulloverId = 5,
            pantsId = 2,
            layoutMode = OutfitLayoutMode.FOUR_LAYERS
        )
        assertEquals(OutfitLayoutMode.FOUR_LAYERS, outfit.layoutMode)
    }

    @Test
    fun `outfit with GRID layout should store correctly`() {
        val outfit = Outfit(
            id = 1,
            topsId = 1,
            pantsId = 2,
            layoutMode = OutfitLayoutMode.GRID
        )
        assertEquals(OutfitLayoutMode.GRID, outfit.layoutMode)
    }

    @Test
    fun `outfit with jacket visible should store correctly`() {
        val outfit = Outfit(
            id = 1,
            topsId = 1,
            pantsId = 2,
            jacketId = 4,
            isJacketVisible = true,
            layoutMode = OutfitLayoutMode.THREE_LAYERS
        )
        assertTrue(outfit.isJacketVisible)
        assertNotNull(outfit.jacketId)
    }

    @Test
    fun `grid outfit should resolve all slot IDs including missing`() {
        // Given – GRID mode outfit with some empty slots
        val outfit = Outfit(
            id = 1,
            topsId = 1,
            pantsId = 2,
            jacketId = null,
            dressId = null,
            skirtId = null,
            shoesId = null,
            layoutMode = OutfitLayoutMode.GRID
        )

        // When – resolving clothes for each slot
        val slotIds = listOf(
            outfit.jacketId, outfit.topsId, outfit.pantsId,
            outfit.skirtId, outfit.pulloverId, outfit.dressId, outfit.shoesId
        )
        val filledSlots = slotIds.filterNotNull()
        val emptySlots = slotIds.count { it == null }

        // Then – in GRID mode, empty slots should still be shown as placeholders
        assertEquals(2, filledSlots.size)  // topsId=1, pantsId=2
        assertEquals(5, emptySlots)
    }

    @Test
    fun `outfit layoutMode values should cover all cases`() {
        val allModes = OutfitLayoutMode.entries
        assertEquals(4, allModes.size)
        assertTrue(allModes.contains(OutfitLayoutMode.TWO_LAYERS))
        assertTrue(allModes.contains(OutfitLayoutMode.THREE_LAYERS))
        assertTrue(allModes.contains(OutfitLayoutMode.FOUR_LAYERS))
        assertTrue(allModes.contains(OutfitLayoutMode.GRID))
    }
}
