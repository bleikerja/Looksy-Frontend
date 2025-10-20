package com.example.looksy.dataClassClones

enum class Material (val displayName: String) {
    Wool ("Wolle"),
    Cotton ("Baumwolle"),
    Polyester ("Polyester"),
    cashmere ("Kaschmir"),
    silk ("Seide"),
    linen ("Leinen"),
    fur ("Pelz"),
    jeans ("Jeans");

    override fun toString(): String {
        return this.displayName
    }
}