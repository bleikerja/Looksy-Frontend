package com.example.looksy.dataClassClones

enum class Type (val displayName: String) {
    Dress ("Kleid"),
    Tops ("Oberteil"),
    Skirt ("Rock"),
    Pants ("Hose"),
    Jacket ("Jacke");

    override fun toString(): String {
        return this.displayName
    }
}