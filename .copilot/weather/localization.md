# Weather Feature — Localization

## What Was Changed (German / `lang=de`)

### Goal

Display all weather descriptions in German by passing `lang=de` to the OpenWeatherMap API.
OpenWeatherMap returns the `description` field (e.g. `"clear sky"`) translated into the requested language.
The `main` field (e.g. `"Clear"`), icon codes, and temperature are always language-independent.

---

### 1. `WeatherApiService.kt` — Added `lang` query parameter

```kotlin
@GET("weather")
suspend fun getWeatherByLocation(
    @Query("lat")   latitude: Double,
    @Query("lon")   longitude: Double,
    @Query("appid") apiKey: String,
    @Query("units") units: String = "metric",
    @Query("lang")  lang: String = "de"        // ← added
): WeatherResponse
```

The default value `"de"` means callers don't need to change; the language is injected automatically by Retrofit.

---

### 2. `getWeatherEmoji()` — Switched from description keywords to OWM icon codes

**Before** (broke with German descriptions):

```kotlin
private fun getWeatherEmoji(description: String): String {
    return when {
        description.contains("clear", ignoreCase = true) -> "☀️"
        description.contains("rain",  ignoreCase = true) -> "🌧️"
        ...
    }
}
```

**After** (language-independent, uses `iconUrl`):

```kotlin
private fun getWeatherEmoji(iconUrl: String): String {
    val code = iconUrl.substringAfterLast("/").removeSuffix(".png").take(2)
    return when (code) {
        "01" -> "☀️"
        "02" -> "🌤️"
        "03" -> "🌥️"
        "04" -> "☁️"
        "09" -> "🌦️"
        "10" -> "🌧️"
        "11" -> "⛈️"
        "13" -> "❄️"
        "50" -> "🌫️"
        else -> "🌤️"
    }
}
```

This function exists in **both** `FullOutfitScreen.kt` and `WeatherScreen.kt`. Both were updated.

#### OWM Icon Code Reference

| Code | Condition                | Emoji |
| ---- | ------------------------ | ----- |
| `01` | Clear sky                | ☀️    |
| `02` | Few clouds               | 🌤️    |
| `03` | Scattered clouds         | 🌥️    |
| `04` | Broken / overcast clouds | ☁️    |
| `09` | Shower rain / drizzle    | 🌦️    |
| `10` | Rain                     | 🌧️    |
| `11` | Thunderstorm             | ⛈️    |
| `13` | Snow                     | ❄️    |
| `50` | Mist / fog / haze        | 🌫️    |

Full reference: https://openweathermap.org/weather-conditions

---

### 3. `getOutfitRecommendations()` in `WeatherScreen.kt` — Rain detection uses icon code

**Before:**

```kotlin
if (weather.description.contains("rain", ignoreCase = true)) { ... }
```

**After:**

```kotlin
val iconCode = weather.iconUrl.substringAfterLast("/").removeSuffix(".png").take(2)
if (iconCode == "09" || iconCode == "10" || iconCode == "11") {
    recommendations.add(OutfitRecommendation("⚠️ Regenschirm empfohlen", true))
}
```

Codes `09` (shower), `10` (rain), and `11` (thunderstorm) all warrant an umbrella.

---

### 4. Tests updated — German mock descriptions

All mock `Weather(...)` objects in unit and instrumented tests now use German descriptions to reflect the real API contract:

| Old (English)       | New (German)       |
| ------------------- | ------------------ |
| `"clear sky"`       | `"klarer Himmel"`  |
| `"Clear sky"`       | `"Klarer Himmel"`  |
| `"Partly cloudy"`   | `"Mäßig bewölkt"`  |
| `"Light rain"`      | `"Leichter Regen"` |
| `"Overcast clouds"` | `"Bedeckt"`        |
| `"Cloudy"`          | `"Bewölkt"`        |

Test files affected:

- `WeatherRepositoryTest.kt`
- `WeatherFeatureIntegrationTest.kt`
- `WeatherViewModelTest.kt`
- `WeatherScreenTest.kt`
- `FullOutfitScreenWeatherTest.kt`

---

## Adding a New Language in the Future

### What needs to change

#### 1. `WeatherApiService.kt` — make `lang` a parameter instead of a hardcoded default

```kotlin
suspend fun getWeatherByLocation(
    @Query("lat")   latitude: Double,
    @Query("lon")   longitude: Double,
    @Query("appid") apiKey: String,
    @Query("units") units: String = "metric",
    @Query("lang")  lang: String = "de"   // pass dynamically from ViewModel/Repository
): WeatherResponse
```

Callers: `WeatherRepository.getWeather(lat, lon, lang = userLocale)`.

#### 2. `WeatherRepository.kt` — accept a `lang` parameter

```kotlin
suspend fun getWeather(latitude: Double, longitude: Double, lang: String = "de"): Weather {
    val response = apiService.getWeatherByLocation(latitude, longitude, apiKey, lang = lang)
    ...
}
```

#### 3. `WeatherViewModel.kt` — resolve locale and forward it

```kotlin
fun fetchWeather(latitude: Double, longitude: Double) {
    val lang = resolveApiLang()  // e.g. from user settings or system locale
    viewModelScope.launch { ... repository.getWeather(latitude, longitude, lang) ... }
}
```

Possible `resolveApiLang()` strategies:

- Read from Android system locale: `Locale.getDefault().language` (e.g. `"de"`, `"en"`, `"fr"`)
- Read from a user-selected preference stored in `DataStore`
- Fall back to `"de"` if the locale isn't supported by OWM

OWM supports ~40 languages. Full list: https://openweathermap.org/current#multi

#### 4. `getWeatherEmoji()` — no change needed

Because emoji mapping is already based on icon codes (language-independent), this function requires **zero changes** for any new language.

#### 5. Outfit recommendation strings in `WeatherScreen.kt`

The recommendation texts (e.g. `"Warme Jacke empfohlen"`) are hardcoded German strings.
For proper multi-language support, move them to `res/values/strings.xml` and use `stringResource()` in Compose.

#### 6. Tests — update mock descriptions per locale

When adding a new language, update test mock `description` fields to match what OWM actually returns for that locale (e.g. `"ciel dégagé"` for French). Consider parameterizing tests so the same test runs for multiple locales.

---

## Architecture Principle

> **The `description` field from the API is for display only.**
> Never use `description` for logic (conditions, recommendations, emoji).
> Always use `iconUrl` (OWM icon code) for any programmatic weather-condition checks.
> This keeps the app fully locale-agnostic at the logic layer.
