package com.example.looksy.data.model

enum class Season(val displayName: String) {
    Winter("Winter"),
    Summer("Sommer"),
    inBetween("Übergang");

    override fun toString(): String {
        return this.displayName
    }
}
