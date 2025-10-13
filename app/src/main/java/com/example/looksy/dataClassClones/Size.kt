package com.example.looksy.dataClassClones

enum class Size {  _34, _36, _38, _40, _42, _44, _46, _48, _50, _52, _54, _56, _58, _60, _XS, _S, _M, _L, _XL;
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
