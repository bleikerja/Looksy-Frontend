package com.example.looksy.data.model

/**
 * Color of a clothing item (named ClothesColor to avoid conflict with Compose Color).
 * Used for outfit color compatibility (e.g. neutral/earth vs accent, accent clash).
 */
enum class ClothesColor(val displayName: String, val kind: Kind) {
    // NEUTRAL
    Black("Schwarz", Kind.NEUTRAL),
    White("Weiß", Kind.NEUTRAL),
    Grey("Grau", Kind.NEUTRAL),
    Navy("Navy", Kind.NEUTRAL),
    // EARTH
    Beige("Beige", Kind.EARTH),
    Brown("Braun", Kind.EARTH),
    Olive("Olivegrün", Kind.EARTH),
    // ACCENT
    Blue("Blau", Kind.ACCENT),
    LightBlue("Hellblau", Kind.ACCENT),
    Green("Grün", Kind.ACCENT),
    Red("Rot", Kind.ACCENT),
    Burgundy("Burgunderrot", Kind.ACCENT),
    Pink("Pink/Rosa", Kind.ACCENT),
    Purple("Lila/Violett", Kind.ACCENT),
    Yellow("Gelb", Kind.ACCENT),
    Orange("Orange", Kind.ACCENT);

    override fun toString(): String = displayName

    enum class Kind {
        NEUTRAL,
        EARTH,
        ACCENT
    }
}
