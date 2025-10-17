package com.example.looksy.dataClassClones

enum class Size (val displayName: String) {
    _XS ("XS"),
    _S ("S"),
    _M ("M"),
    _L ("L"),
    _XL ("XL"),
    _34 ("34"),
    _36 ("36"),
    _38 ("38"),
    _40 ("40"),
    _42 ("42"),
    _44 ("44"),
    _46 ("46"),
    _48 ("48"),
    _50 ("50"),
    _52 ("52"),
    _54 ("54"),
    _56 ("56"),
    _58 ("58"),
    _60 ("60");

    override fun toString(): String {
        return this.displayName
    }

    val toLetterSize: Size
        get() = when (this) { // `this` bezieht sich auf die Instanz, z.B. Size._34
            _34, _36 -> _XS
            _38 -> _S
            _40 -> _M
            _42 -> _L
            _44 -> _XL
            // Wenn es bereits eine Buchstaben-Größe ist, gib sie einfach zurück
            _XS, _S, _M, _L, _XL -> this
            // Für alle anderen numerischen Größen, die wir nicht kennen
            else -> throw NoKnownSize("Keine Buchstabengröße für '${this.name}' bekannt.")
        }
}

class NoKnownSize(message: String?) : RuntimeException(message)
