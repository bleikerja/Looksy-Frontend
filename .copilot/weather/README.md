# Weather Feature Documentation

This folder contains comprehensive documentation for the weather integration feature in Looksy.

## 📁 Files in This Folder

- **[class-diagram.md](class-diagram.md)** - Complete class structure and relationships
- **[data-flow.md](data-flow.md)** - Simplified data flow visualization
- **[sequence.md](sequence.md)** - Detailed interaction sequence with timing
- **README.md** (this file) - Overview and quick reference

## 🏗️ Architecture Overview

The weather feature follows the **MVVM architecture pattern** with clean separation of concerns:

```
UI (Composable)
    ↓
ViewModel (WeatherViewModel)
    ↓
Repository (WeatherRepository)
    ↓
API Service (WeatherApiService - Retrofit)
    ↓
OpenWeatherMap API
```

### Supporting Components

- **LocationProvider**: Handles GPS location retrieval
- **LooksyApplication**: Provides dependency injection
- **WeatherViewModelFactory**: Creates ViewModel with dependencies

## 📦 Package Structure

```
com.example.looksy/
├── data/
│   ├── location/
│   │   ├── LocationProvider.kt          # GPS location access
│   │   ├── Location.kt                  # lat/lon data class
│   │   ├── LocationInputMode.kt         # GPS vs MANUAL_CITY enum
│   │   └── PermissionState.kt           # NOT_ASKED / GRANTED_* / DENIED
│   ├── model/
│   │   └── Weather.kt                   # Domain model
│   ├── remote/
│   │   ├── api/
│   │   │   ├── WeatherApiService.kt     # Retrofit: /data/2.5/weather
│   │   │   └── GeocodingApiService.kt   # Retrofit: /geo/1.0/direct
│   │   └── dto/
│   │       ├── WeatherResponse.kt       # OpenWeatherMap DTOs
│   │       └── GeocodingResponse.kt     # Geocoding API DTOs
│   └── repository/
│       ├── WeatherRepository.kt         # Weather data coordination
│       └── GeocodingRepository.kt       # City → coordinates
└── ui/
    ├── screens/
    │   └── WeatherScreen.kt             # Full weather detail screen
    └── viewmodel/
        ├── WeatherViewModel.kt          # weatherState: StateFlow<WeatherUiState>
        ├── WeatherViewModelFactory.kt   # DI factory
        ├── GeocodingViewModel.kt        # geocodingState: StateFlow<GeocodingUiState>
        └── GeocodingViewModelFactory.kt # DI factory
```

## 🔑 Key Components

### Data Models

**Weather** (Domain Model)

```kotlin
data class Weather(
    val locationName: String,
    val temperature: Double,      // Celsius
    val feelsLike: Double,
    val description: String,
    val humidity: Int,            // Percentage
    val iconUrl: String           // Weather icon URL
)
```

**WeatherUiState** (UI State)

```kotlin
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
```

### API Integration

- **Service**: OpenWeatherMap API v2.5
- **Base URL**: `https://api.openweathermap.org/data/2.5/`
- **Endpoint**: `GET /weather`
- **Auth**: API key in query parameter
- **Units**: Metric (Celsius)

### Dependencies

```toml
retrofit = "2.9.0"
okhttp = "4.12.0"
play-services-location = "21.0.1"
```

## 🚀 Usage Example

```kotlin
// WeatherScreen is wired in NavGraph — it owns permission + location logic internally.
// FullOutfitScreen receives already-resolved weatherState from NavGraph:
@Composable
fun FullOutfitScreen(
    weatherState: WeatherUiState = WeatherUiState.Loading,
    permissionState: PermissionState = PermissionState.NOT_ASKED,
    isLocationEnabled: Boolean = true,
    onWeatherClick: () -> Unit = {}
) {
    // Weather row top-left — always visible in both outfit and empty-closet state
    WeatherIconRow(
        weatherState = weatherState,
        permissionState = permissionState,
        isLocationEnabled = isLocationEnabled,
        onClick = onWeatherClick
    )
}

// WeatherScreen refresh entry point (called by LaunchedEffect, ON_RESUME, and pull-to-refresh):
fun refreshWeatherState() {
    if (isRefreshing) return
    scope.launch {
        isRefreshing = true
        val hasPermission = locationProvider.hasLocationPermission()
        isLocationEnabled = locationProvider.isLocationEnabled()
        if (hasPermission && isLocationEnabled) {
            locationProvider.getCurrentLocation().onSuccess { location ->
                weatherViewModel.fetchWeather(location.latitude, location.longitude)
            }
        }
        isRefreshing = false
    }
}
```

## 🔒 Setup Requirements

1. **API Key**: Add to `local.properties`:

   ```properties
   WEATHER_API_KEY=your_openweathermap_api_key
   ```

   Get a free key at: https://openweathermap.org/api

2. **Permissions**: Already added in AndroidManifest.xml
   - `INTERNET`
   - `ACCESS_COARSE_LOCATION`
   - `ACCESS_FINE_LOCATION`

3. **Permission Request**: Add UI to request location permission (similar to camera permission flow)

## 📊 Diagrams

For detailed visualizations, see:

- [Class relationships](class-diagram.md) - Structure and dependencies
- [Data flow](data-flow.md) - High-level flow overview
- [Sequence](sequence.md) - Complete interaction timeline

## ✅ Implementation Checklist

- [x] API service interface (Retrofit)
- [x] DTO models (WeatherResponse, Main, WeatherInfo)
- [x] Domain model (Weather)
- [x] Location provider
- [x] Repository with DTO→Domain transformation
- [x] ViewModel with StateFlow
- [x] ViewModelFactory
- [x] Dependency injection in LooksyApplication
- [x] Gradle dependencies
- [x] AndroidManifest permissions
- [x] BuildConfig API key setup
- [x] WeatherScreen UI (GPS + manual city fallback)
- [x] GeocodingScreen integration (city search → coordinates)
- [x] Unified `refreshWeatherState()` — single entry point for load/resume/swipe
- [x] Lifecycle-aware resume reload (`DisposableEffect` + `ON_RESUME`)
- [x] Swipe-to-refresh via `PullToRefreshBox` (Material3 experimental)
- [x] Android-only permission dialog (no custom bottom sheet)
- [x] WeatherIconRow in FullOutfitScreen (both outfit and empty-closet states)
- [x] Semantic `testTag("weather_loading")` on loading indicator
- [x] Material icon for "no permission" state (`Icons.Default.DomainDisabled`)
- [x] Duplicate header removed from FullOutfitScreen
- [x] Full JVM unit test coverage (GeocodingRepository, GeocodingViewModel, integration)
- [x] Instrumented test suite stabilized for API 36 (espresso-core 3.7.0 forced)

## 🎯 Design Patterns Used

1. **MVVM**: Separation of UI, business logic, and data
2. **Repository Pattern**: Abstract data sources from ViewModel
3. **DTO Pattern**: Separate API models from domain models
4. **Factory Pattern**: Create ViewModels with dependencies
5. **Sealed Interface**: Type-safe UI state representation
6. **Flow**: Reactive data streams
7. **Result wrapper**: Type-safe error handling
8. **Lazy initialization**: Efficient dependency creation

## 🔗 Related Documentation

- [Main architecture guide](../architecture.md)
- [Data models overview](../data-models.md)
- [Project conventions](../conventions.md)
