package com.example.looksy.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_LAST_CITY = stringPreferencesKey("last_searched_city")
        private val KEY_LAST_LAT  = stringPreferencesKey("last_searched_lat")
        private val KEY_LAST_LON  = stringPreferencesKey("last_searched_lon")
    }

    val lastSearchedCity: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_CITY] ?: ""
    }

    val lastSearchedLat: Flow<Double?> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_LAT]?.toDoubleOrNull()
    }

    val lastSearchedLon: Flow<Double?> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_LON]?.toDoubleOrNull()
    }

    suspend fun saveLastSearchedCity(city: String, lat: Double, lon: Double) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_CITY] = city
            prefs[KEY_LAST_LAT]  = lat.toString()
            prefs[KEY_LAST_LON]  = lon.toString()
        }
    }

    suspend fun clearLastSearchedCity() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_LAST_CITY)
            prefs.remove(KEY_LAST_LAT)
            prefs.remove(KEY_LAST_LON)
        }
    }
}
