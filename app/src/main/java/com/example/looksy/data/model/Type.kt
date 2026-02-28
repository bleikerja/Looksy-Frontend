package com.example.looksy.data.model

enum class Type(val displayName: String) {
    Dress("Kleid"),
    TShirt("T-Shirt/Longsleeve"),
    Pullover("Pullover/Sweatshirt"),
    Skirt("Rock"),
    Pants("Hose"),
    Jacket("Jacke"),
    Shoes("Schuhe");

    override fun toString(): String {
        return this.displayName
    }

    companion object {
        /** All types that count as a "top" in an outfit. */
        val topTypes: Set<Type> = setOf(TShirt, Pullover)
    }
}
