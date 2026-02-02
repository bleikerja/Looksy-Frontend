package com.example.looksy.util

/**
 * Definiert farblich zusammenpassende Farbgruppen für den Outfitgenerator.
 * Z.B. Neutrale: Schwarz, Weiß, Braun (laut Aufgabe); weitere harmonierende Gruppen erweiterbar.
 */
object ColorCompatibility {

    /** Neutrale Farben, die untereinander passen (Schwarz, Weiß, Braun). */
    private val NEUTRAL_COLORS = setOf(
        "Schwarz", "Weiß", "Braun", "Beige", "Grau"
    ).map { it.trim().lowercase() }.toSet()

    /** Zusätzliche harmonierende Farbgruppen (jede Zeile = eine Gruppe). */
    private val COLOR_GROUPS: List<Set<String>> = listOf(
        NEUTRAL_COLORS,
        setOf("blau", "navy", "dunkelblau"),
        setOf("rot", "burgunder", "weinrot"),
        setOf("grün", "oliv", "dunkelgrün"),
        setOf("rosa", "pink", "lachs")
    ).map { group -> group.map { it.trim().lowercase() }.toSet() }

    /**
     * Prüft, ob zwei Farben zusammenpassen.
     * - Wenn eine Farbe null oder leer ist, passt sie zu jeder anderen (optional = neutral).
     * - Ansonsten müssen beide derselben Gruppe angehören oder gleich sein.
     */
    fun areCompatible(color1: String?, color2: String?): Boolean {
        val c1 = color1?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        val c2 = color2?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        if (c1 == null || c2 == null) return true
        if (c1 == c2) return true
        val group1 = COLOR_GROUPS.find { c1 in it }
        val group2 = COLOR_GROUPS.find { c2 in it }
        return group1 != null && group1 == group2
    }

    /**
     * Gibt true zurück, wenn [itemColor] mit mindestens einer Farbe aus [allowedColors] kompatibel ist.
     * [allowedColors] kann null sein = alle Farben erlaubt (z.B. wenn Anker-Item keine Farbe hat).
     */
    fun isColorAllowed(itemColor: String?, allowedColors: Set<String>?): Boolean {
        if (allowedColors == null) return true
        val item = itemColor?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        if (item == null) return true
        return allowedColors.any { areCompatible(item, it) }
    }

    /**
     * Ermittelt die Menge der erlaubten Farben ausgehend von der Anker-Farbe.
     * Wenn [anchorColor] null/leer, wird null zurückgegeben (= alle erlaubt).
     */
    fun getAllowedColors(anchorColor: String?): Set<String>? {
        val c = anchorColor?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return null
        return COLOR_GROUPS.find { c in it } ?: setOf(c)
    }
}
