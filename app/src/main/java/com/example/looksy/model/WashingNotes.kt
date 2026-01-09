package com.example.looksy.model

enum class WashingNotes(val displayName: String) {
    None("-"),
    DontWash("Nicht Waschen"),
    Temperature30("Waschen 30°C"),
    Temperature40("Waschen 40°C"),
    Temperature60("Waschen 60°C"),
    Hand("Handwäsche"),
    Dryer("Trockner"),
    NoDryer("Kein Trockner");

    override fun toString(): String {
        return this.displayName
    }
}
