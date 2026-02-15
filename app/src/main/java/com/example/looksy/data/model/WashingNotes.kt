package com.example.looksy.data.model

import com.example.looksy.R

enum class WashingNotes(val displayName: String, val iconRes: Int? = null) {
    None("-"),
    Hand("Handwäsche", R.drawable.hand),
    Temperature30("Waschen 30°C", R.drawable.temp30),
    Temperature30Soft("Pflegeleicht 30°C", R.drawable.temp30s),
    CautiousWashing("Schonwaschgang 30°C", R.drawable.cou),
    DontWash("Nicht Waschen", R.drawable.no_w),
    Temperature40("Waschen 40°C", R.drawable.temp40),
    Temperature40Soft("Pflegeleicht 40°C", R.drawable.temp40s),
    DelicateWashing("Feinwäsche 40°C", R.drawable.deli),
    Temperature60("Waschen 60°C", R.drawable.temp60),
    Temperature60Soft("Pflegeleicht 60°C", R.drawable.temp60s),
    CookingWashing("Kochwäsche 95°C", R.drawable.cooking),
    Bleach("Bleichen", R.drawable.bleach),
    LyingDry("Liegend trocknen", R.drawable.lying_d),
    WetDry("Tropfnass trocknen"),
    HangingDryLine("Trocknen auf der Leine"),
    Dryer("Trockner"),
    DryerLowTemperatur("Trockner niedrige Temperatur"),
    DryerNormalTemperatur("Trockner normale Temperatur"),
    HangingDry("Hängend Trocknen"),
    HangingShadowDry("Im Schatten trocknen"),
    NoDryer("Kein Trockner"),
    Iron("Bügeln"),
    IronLowTemperatur("Bügeln mit niedriger Temperatur"),
    IronNormalTemperatur("Bügeln mit normaler Temperatur"),
    IronHighTemperatur("Bügeln mit hoher Temperatur"),
    NoIron("Kein Bügeln"),
    ;

    override fun toString(): String {
        return this.displayName
    }

    companion object {
        fun getConflicts(note: WashingNotes): List<WashingNotes> {
            return when (note) {
                None -> entries.filter { it != None }
                DontWash -> listOf(
                    None,
                    Temperature30,
                    Temperature30Soft,
                    CautiousWashing,
                    Temperature40,
                    Temperature40Soft,
                    DelicateWashing,
                    Temperature60,
                    Temperature60Soft,
                    CookingWashing,
                    Hand
                )

                Temperature30, Temperature40, Temperature60, CookingWashing, Hand, Temperature30Soft, Temperature40Soft, Temperature60Soft, CautiousWashing, DelicateWashing ->
                    listOf(None, DontWash) + listOf(
                        Temperature30,
                        Temperature40,
                        Temperature60,
                        CookingWashing,
                        Hand,
                        Temperature30Soft,
                        Temperature40Soft,
                        Temperature60Soft,
                        CautiousWashing,
                        DelicateWashing
                    ).filter { it != note }
                HangingDryLine, HangingDry, HangingShadowDry, LyingDry, WetDry -> listOf(None, DontWash, Dryer, DryerLowTemperatur, DryerNormalTemperatur)
                Dryer -> listOf(None, NoDryer, DryerLowTemperatur, DryerNormalTemperatur, HangingDryLine, HangingDry, HangingShadowDry, LyingDry, WetDry)
                NoDryer -> listOf(None, Dryer)
                Bleach -> listOf(None)
                Iron -> listOf(None, NoIron)
                IronLowTemperatur -> listOf(None, NoIron, IronNormalTemperatur, IronHighTemperatur)
                IronNormalTemperatur -> listOf(None, NoIron, IronLowTemperatur, IronHighTemperatur)
                IronHighTemperatur -> listOf(None, NoIron, IronLowTemperatur, IronNormalTemperatur)
                NoIron -> listOf(None, Iron)
                DryerLowTemperatur, DryerNormalTemperatur -> listOf(None, NoDryer) + listOf(
                    DryerLowTemperatur,
                    DryerNormalTemperatur,
                    HangingDryLine,
                    HangingDry,
                    HangingShadowDry,
                    LyingDry,
                    WetDry
                ).filter { it != note }
            }
        }
    }
}
