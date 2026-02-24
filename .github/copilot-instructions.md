# Looksy Android App - Copilot Instructions

## Stack & Entry Points

- Kotlin + Jetpack Compose + Room + Navigation Compose in a single `:app` module.
- App entry: `MainActivity` sets `ScreenBlueprint` (`app/src/main/java/com/example/looksy/MainActivity.kt`).
- Lightweight DI lives in `LooksyApplication` via `by lazy` repositories/services (`app/src/main/java/com/example/looksy/LooksyApplication.kt`).
- Build config and API key wiring are in `app/build.gradle.kts` (`BuildConfig.WEATHER_API_KEY`).

## Architecture You Should Follow

- Core flow is `Composable -> ViewModel -> Repository -> DAO/API`.
- Room-backed features expose `Flow` in DAOs/repositories and convert to `StateFlow` in ViewModels using `stateIn(...WhileSubscribed(5000)... )` (see `ClothesViewModel`, `OutfitViewModel`).
- Network-backed weather/geocoding features use explicit `MutableStateFlow` UI states (`WeatherUiState`, `GeocodingUiState`) rather than DAO flows.
- Keep writes in `viewModelScope.launch` and repository/DAO functions `suspend`.

## Navigation & UI Conventions

- Route definitions are centralized in sealed class `Routes` (`app/src/main/java/com/example/looksy/ui/navigation/Routes.kt`).
- Add new destinations in `Routes` and wire them in `NavGraph` (`app/src/main/java/com/example/looksy/ui/navigation/NavGraph.kt`).
- If route params can contain special characters, encode/decode with `Uri.encode`/`Uri.decode` (existing pattern: `SpecificCategory`).
- Shared ViewModels are created in `ScreenBlueprint` with factories from `LooksyApplication`; keep this pattern for new feature ViewModels.

## Data & Persistence Patterns

- Room DB is `ClothesDatabase` (version `3`) with `fallbackToDestructiveMigration()`; bump version when schema changes.
- Entities currently: `Clothes` and `Outfit` (`app/src/main/java/com/example/looksy/data/model/`).
- Enum persistence uses `TypeConverters` in `Converters.kt`; add converters for any new enum fields.
- Camera captures temp file in `cacheDir` (`ui/screens/Kamera.kt`) and `saveImagePermanently` copies to `filesDir/images` (`util/ImageStorage.kt`); store resulting absolute path in `Clothes.imagePath`.

## Weather Integration (Current Branch Reality)

- Weather uses OpenWeatherMap via Retrofit services: `WeatherApiService` (`/data/2.5/weather`) and `GeocodingApiService` (`/geo/1.0/direct`).
- `WeatherScreen` supports GPS + manual city fallback; permission/location state is handled in UI with `LocationProvider` and `PermissionState`.
- Ensure `WEATHER_API_KEY` exists in `local.properties` (template in `local.properties.template`) before running weather-related flows/tests.

## Developer Workflows

- Build debug APK: `./gradlew assembleDebug` (Windows: `.\gradlew assembleDebug`).
- Install on device/emulator: `./gradlew installDebug`.
- Unit tests: `./gradlew test`.
- Instrumented tests: `./gradlew connectedAndroidTest`.
- After Room/entity/converter edits, run clean build if generated code is stale: `./gradlew clean assembleDebug`.

## Source of Truth Notes

- Some legacy docs under `.copilot/` still mention old package names (`ViewModels`, `dataClassClones`, etc.); prefer actual paths under `app/src/main/java/com/example/looksy/...`.
- Use these files as primary examples: `LooksyApplication.kt`, `ScreenBlueprint.kt`, `NavGraph.kt`, `ClothesViewModel.kt`, `WeatherViewModel.kt`, `ClothesDatabase.kt`.
