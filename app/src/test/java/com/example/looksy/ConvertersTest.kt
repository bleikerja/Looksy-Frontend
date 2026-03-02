package com.example.looksy

import com.example.looksy.data.local.database.Converters
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.OutfitLayoutMode
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import org.junit.Assert
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun sizeConversion() {
        val size = Size._M
        val converted = converters.fromSize(size)
        Assert.assertEquals("_M", converted)
        Assert.assertEquals(size, converters.toSize(converted))
    }

    @Test
    fun seasonConversion() {
        val season = Season.Summer
        val converted = converters.fromSeason(season)
        Assert.assertEquals("Summer", converted)
        Assert.assertEquals(season, converters.toSeason(converted))
    }

    @Test
    fun typeConversion() {
        val type = Type.TShirt
        val converted = converters.fromType(type)
        Assert.assertEquals("TShirt", converted)
        Assert.assertEquals(type, converters.toType(converted))
    }

    @Test
    fun materialConversion() {
        val material = Material.Wool
        val converted = converters.fromMaterial(material)
        Assert.assertEquals("Wool", converted)
        Assert.assertEquals(material, converters.toMaterial(converted))
    }

    @Test
    fun washingNotesList_toJsonAndBack() {
        val notes = listOf(WashingNotes.Temperature30, WashingNotes.Dryer)
        val json = converters.fromWashingNotesList(notes)
        val result = converters.toWashingNotesList(json)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(WashingNotes.Temperature30, result[0])
        Assert.assertEquals(WashingNotes.Dryer, result[1])
    }

    @Test
    fun toWashingNotesList_handlesJsonWithNulls() {
        // This simulates a corrupt JSON or unexpected nulls from Gson
        val json = "[ \"Temperature30\", null, \"Dryer\" ]"
        val result = converters.toWashingNotesList(json)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(WashingNotes.Temperature30, result[0])
        Assert.assertEquals(WashingNotes.Dryer, result[1])
        // Ensure no nulls are in the list (this was causing the NPE)
        Assert.assertTrue(result.all { it != null })
    }

    @Test
    fun toWashingNotesList_handlesLegacySingleValue() {
        // Room might have saved a single enum name as a string before it was a list
        val legacyValue = "Hand"
        val result = converters.toWashingNotesList(legacyValue)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(WashingNotes.Hand, result[0])
    }

    @Test
    fun toWashingNotesList_handlesEmptyOrInvalid() {
        Assert.assertEquals(emptyList<WashingNotes>(), converters.toWashingNotesList(""))
        Assert.assertEquals(
            emptyList<WashingNotes>(),
            converters.toWashingNotesList("InvalidValue")
        )
    }

    @Test
    fun singleWashingNoteConversion() {
        val note = WashingNotes.NoDryer
        val converted = converters.fromWashingNotes(note)
        Assert.assertEquals("NoDryer", converted)
        Assert.assertEquals(note, converters.toWashingNotes(converted))
    }

    @Test
    fun toWashingNotes_defaultsOnInvalid() {
        Assert.assertEquals(WashingNotes.None, converters.toWashingNotes("DoesNotExist"))
    }

    @Test
    fun pulloverTypeConversion() {
        val type = Type.Pullover
        val converted = converters.fromType(type)
        Assert.assertEquals("Pullover", converted)
        Assert.assertEquals(type, converters.toType(converted))
    }

    @Test
    fun shoesTypeConversion() {
        val type = Type.Shoes
        val converted = converters.fromType(type)
        Assert.assertEquals("Shoes", converted)
        Assert.assertEquals(type, converters.toType(converted))
    }

    @Test
    fun shoeSizeConversion() {
        val shoeSizes = listOf(
            Size._36 to "_36",
            Size._37 to "_37",
            Size._38 to "_38",
            Size._39 to "_39",
            Size._40 to "_40",
            Size._41 to "_41",
            Size._42 to "_42",
            Size._43 to "_43",
            Size._44 to "_44",
            Size._45 to "_45"
        )
        for ((size, expectedName) in shoeSizes) {
            val converted = converters.fromSize(size)
            Assert.assertEquals(expectedName, converted)
            Assert.assertEquals(size, converters.toSize(converted))
        }
    }

    // ─── OutfitLayoutMode converter tests ───

    @Test
    fun outfitLayoutModeConversion_allValues() {
        for (mode in OutfitLayoutMode.entries) {
            val converted = converters.fromOutfitLayoutMode(mode)
            Assert.assertEquals(mode.name, converted)
            Assert.assertEquals(mode, converters.toOutfitLayoutMode(converted))
        }
    }

    @Test
    fun toOutfitLayoutMode_defaultsOnInvalid() {
        Assert.assertEquals(
            OutfitLayoutMode.THREE_LAYERS,
            converters.toOutfitLayoutMode("INVALID_VALUE")
        )
    }

    @Test
    fun outfitLayoutModeConversion_twoLayers() {
        val mode = OutfitLayoutMode.TWO_LAYERS
        val converted = converters.fromOutfitLayoutMode(mode)
        Assert.assertEquals("TWO_LAYERS", converted)
        Assert.assertEquals(mode, converters.toOutfitLayoutMode(converted))
    }

    @Test
    fun outfitLayoutModeConversion_grid() {
        val mode = OutfitLayoutMode.GRID
        val converted = converters.fromOutfitLayoutMode(mode)
        Assert.assertEquals("GRID", converted)
        Assert.assertEquals(mode, converters.toOutfitLayoutMode(converted))
    }
}