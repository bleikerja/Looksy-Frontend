package com.example.looksy.data.model

import androidx.compose.ui.graphics.Color

/**
 * Color of a clothing item (named ClothesColor to avoid conflict with Compose Color).
 * Used for outfit color compatibility (e.g. neutral/earth vs accent, accent clash).
 * [colorValue] is the visual Compose Color used to render the color bubble in the UI.
 */
enum class ClothesColor(val displayName: String, val kind: Kind, val colorValue: Color) {
    // NEUTRAL
    Black("Schwarz", Kind.NEUTRAL, Color(0xFF1A1A1A)),
    White("Weiß", Kind.NEUTRAL, Color(0xFFFFFFFF)),
    Grey("Grau", Kind.NEUTRAL, Color(0xFF9E9E9E)),
    Navy("Navy", Kind.NEUTRAL, Color(0xFF0D2137)),
    // EARTH
    Beige("Beige", Kind.EARTH, Color(0xFFF5F0DC)),
    Brown("Braun", Kind.EARTH, Color(0xFF795548)),
    Olive("Olivegrün", Kind.EARTH, Color(0xFF6B6B2A)),
    // ACCENT
    Blue("Blau", Kind.ACCENT, Color(0xFF1565C0)),
    LightBlue("Hellblau", Kind.ACCENT, Color(0xFF42A5F5)),
    Green("Grün", Kind.ACCENT, Color(0xFF2E7D32)),
    Red("Rot", Kind.ACCENT, Color(0xFFC62828)),
    Burgundy("Burgunderrot", Kind.ACCENT, Color(0xFF6A0032)),
    Pink("Pink/Rosa", Kind.ACCENT, Color(0xFFF48FB1)),
    Purple("Lila/Violett", Kind.ACCENT, Color(0xFF7B1FA2)),
    Yellow("Gelb", Kind.ACCENT, Color(0xFFFDD835)),
    Orange("Orange", Kind.ACCENT, Color(0xFFEF6C00));

    override fun toString(): String = displayName

    enum class Kind {
        NEUTRAL,
        EARTH,
        ACCENT
    }
}
