package com.example.looksy.dataClassClones

//TODO: add more Washing notes
enum class WashingNotes (val displayName: String){
    Temperature30("Waschen 30 °C"),
    Hand ("Handwäsche"),
    Dying ("Trocknen"),
    Dryer("Trockner");

    override fun toString(): String {
        return this.displayName
    }
}