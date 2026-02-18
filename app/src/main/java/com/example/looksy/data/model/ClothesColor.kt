package com.example.looksy.data.model

/**
 * Color of a clothing item (named ClothesColor to avoid conflict with Compose Color).
 * Used for outfit color compatibility (e.g. neutral/earth vs accent, accent clash).
 */
enum class ClothesColor(val displayName: String, val kind: Kind) {
    // NEUTRAL
    Black("Black", Kind.NEUTRAL),
    White("White", Kind.NEUTRAL),
    Grey("Grey", Kind.NEUTRAL),
    Navy("Navy", Kind.NEUTRAL),
    // EARTH
    Beige("Beige", Kind.EARTH),
    Brown("Brown", Kind.EARTH),
    Olive("Olive", Kind.EARTH),
    // ACCENT
    Blue("Blue", Kind.ACCENT),
    LightBlue("LightBlue", Kind.ACCENT),
    Green("Green", Kind.ACCENT),
    Red("Red", Kind.ACCENT),
    Burgundy("Burgundy", Kind.ACCENT),
    Pink("Pink", Kind.ACCENT),
    Purple("Purple", Kind.ACCENT),
    Yellow("Yellow", Kind.ACCENT),
    Orange("Orange", Kind.ACCENT);

    override fun toString(): String = displayName

    enum class Kind {
        NEUTRAL,
        EARTH,
        ACCENT
    }
}
