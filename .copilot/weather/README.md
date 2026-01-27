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
│   │   └── LocationProvider.kt          # GPS location access
│   ├── model/
│   │   └── Weather.kt                   # Domain model
│   ├── remote/
│   │   ├── api/
│   │   │   └── WeatherApiService.kt     # Retrofit interface
│   │   └── dto/
│   │       └── WeatherResponse.kt       # API DTOs
│   └── repository/
│       └── WeatherRepository.kt         # Data coordination
└── ui/
    └── viewmodel/
        ├── WeatherViewModel.kt           # State management
        └── WeatherViewModelFactory.kt    # DI factory
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
@Composable
fun WeatherScreen() {
    val application = LocalContext.current.applicationContext as LooksyApplication
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(application.weatherRepository)
    )
    val locationProvider = remember { application.locationProvider }
    val weatherState by weatherViewModel.weatherState.collectAsState()

    // Request location and fetch weather
    LaunchedEffect(Unit) {
        if (locationProvider.hasLocationPermission()) {
            locationProvider.getCurrentLocation().onSuccess { location ->
                weatherViewModel.fetchWeather(location.latitude, location.longitude)
            }
        }
    }

    when (val state = weatherState) {
        is WeatherUiState.Loading -> CircularProgressIndicator()
        is WeatherUiState.Success -> {
            Column {
                Text("${state.weather.locationName}")
                Text("${state.weather.temperature}°C")
                Text(state.weather.description)
                AsyncImage(
                    model = state.weather.iconUrl,
                    contentDescription = "Weather icon"
                )
            }
        }
        is WeatherUiState.Error -> Text("Error: ${state.message}")
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
- [ ] UI screen implementation
- [ ] Location permission request UI
- [ ] Error handling UI
- [ ] Loading state UI

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
