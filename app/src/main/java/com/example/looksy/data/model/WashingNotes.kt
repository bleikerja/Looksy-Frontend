package com.example.looksy.data.model

import com.example.looksy.R

enum class WashingNotes(val displayName: String, val iconRes: Int? = null) {
    None("-"),
    Hand("Handwäsche", R.drawable.hand),
    DontWash("Nicht Waschen", R.drawable.no_w),
    Temperature30("Waschen 30°C", R.drawable.temp30),
    Temperature30Soft("Pflegeleicht 30°C", R.drawable.temp30s),
    CautiousWashing("Schonwaschgang 30°C", R.drawable.cou),
    Temperature40("Waschen 40°C", R.drawable.temp40),
    Temperature40Soft("Pflegeleicht 40°C", R.drawable.temp40s),
    DelicateWashing("Feinwäsche 40°C", R.drawable.deli),
    Temperature60("Waschen 60°C", R.drawable.temp60),
    Temperature60Soft("Pflegeleicht 60°C", R.drawable.temp60s),
    CookingWashing("Kochwäsche 95°C", R.drawable.cooking),
    Bleach("Bleichen", R.drawable.bleach),
    DontBleach("Nicht Bleichen", R.drawable.no_b),
    NoChlorBleach("Nur bei Bedarf chlorfrei bleichen", R.drawable.no_cb),
    LyingDry("Liegend trocknen", R.drawable.lying_d),
    WetDry("Tropfnass trocknen", R.drawable.wet_d),
    HangingDryLine("Trocknen auf der Leine", R.drawable.hanging_l_d),
    HangingDry("Hängend Trocknen", R.drawable.hanging_d),
    Dryer("Trockner", R.drawable.dry),
    DryerLowTemperatur("Trockner niedrige Temperatur", R.drawable.dry_l),
    DryerNormalTemperatur("Trockner normale Temperatur", R.drawable.dry_n),
    //HangingShadowDry("Im Schatten trocknen"),
    NoDryer("Kein Trockner", R.drawable.no_dry),
    IronLowTemperatur("Bügeln mit niedriger Temperatur", R.drawable.iron_l),
    IronNormalTemperatur("Bügeln mit normaler Temperatur", R.drawable.iron_n),
    IronHighTemperatur("Bügeln mit hoher Temperatur", R.drawable.iron_h),
    NoIron("Kein Bügeln", R.drawable.no_iron),
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
                HangingDryLine, HangingDry, LyingDry, WetDry -> listOf(None, DontWash, Dryer, DryerLowTemperatur, DryerNormalTemperatur)
                NoDryer -> listOf(None, Dryer, DryerLowTemperatur, DryerNormalTemperatur)
                Bleach, NoChlorBleach, DontBleach -> listOf(None) + listOf(NoChlorBleach, DontBleach,Bleach).filter { it != note }
                IronLowTemperatur -> listOf(None, NoIron, IronNormalTemperatur, IronHighTemperatur)
                IronNormalTemperatur -> listOf(None, NoIron, IronLowTemperatur, IronHighTemperatur)
                IronHighTemperatur -> listOf(None, NoIron, IronLowTemperatur, IronNormalTemperatur)
                NoIron -> listOf(None, IronLowTemperatur, IronNormalTemperatur, IronHighTemperatur)
                DryerLowTemperatur, DryerNormalTemperatur, Dryer -> listOf(None, NoDryer) + listOf(
                    DryerLowTemperatur,
                    DryerNormalTemperatur,
                    Dryer,
                    HangingDryLine,
                    HangingDry,
                    LyingDry,
                    WetDry
                ).filter { it != note }
            }
        }
    }
}
