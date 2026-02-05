package com.example.looksy

import com.example.looksy.util.ColorCompatibility
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorCompatibilityTest {

    @Test
    fun `areCompatible returns true when both colors are null or empty`() {
        assertTrue(ColorCompatibility.areCompatible(null, null))
        assertTrue(ColorCompatibility.areCompatible("", null))
        assertTrue(ColorCompatibility.areCompatible(null, ""))
    }

    @Test
    fun `areCompatible returns true when one color is null or empty`() {
        assertTrue(ColorCompatibility.areCompatible(null, "Schwarz"))
        assertTrue(ColorCompatibility.areCompatible("Weiß", null))
        assertTrue(ColorCompatibility.areCompatible("", "Braun"))
    }

    @Test
    fun `areCompatible returns true for same color`() {
        assertTrue(ColorCompatibility.areCompatible("Schwarz", "Schwarz"))
        assertTrue(ColorCompatibility.areCompatible("Blau", "Blau"))
    }

    @Test
    fun `areCompatible returns true for neutral group Schwarz Weiß Braun`() {
        assertTrue(ColorCompatibility.areCompatible("Schwarz", "Weiß"))
        assertTrue(ColorCompatibility.areCompatible("Schwarz", "Braun"))
        assertTrue(ColorCompatibility.areCompatible("Weiß", "Braun"))
        assertTrue(ColorCompatibility.areCompatible("Grau", "Schwarz"))
        assertTrue(ColorCompatibility.areCompatible("Beige", "Braun"))
    }

    @Test
    fun `areCompatible returns false for incompatible colors`() {
        assertFalse(ColorCompatibility.areCompatible("Schwarz", "Rot"))
        assertFalse(ColorCompatibility.areCompatible("Weiß", "Blau"))
        assertFalse(ColorCompatibility.areCompatible("Braun", "Rosa"))
    }

    @Test
    fun `areCompatible is case insensitive`() {
        assertTrue(ColorCompatibility.areCompatible("schwarz", "WEISS"))
        assertTrue(ColorCompatibility.areCompatible("SCHWARZ", "weiß"))
    }

    @Test
    fun `isColorAllowed returns true when allowedColors is null`() {
        assertTrue(ColorCompatibility.isColorAllowed("Rot", null))
        assertTrue(ColorCompatibility.isColorAllowed(null, null))
    }

    @Test
    fun `isColorAllowed returns true when itemColor is null or empty`() {
        assertTrue(ColorCompatibility.isColorAllowed(null, setOf("Schwarz")))
        assertTrue(ColorCompatibility.isColorAllowed("", setOf("Schwarz")))
    }
}
