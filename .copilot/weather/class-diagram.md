# Weather Feature - Class Diagram

This diagram shows the structure and relationships between all weather-related classes in the Looksy app.

```mermaid
classDiagram
    %% Application Layer - Dependency Injection
    class LooksyApplication {
        +weatherApiService: WeatherApiService
        +weatherRepository: WeatherRepository
        +locationProvider: LocationProvider
    }

    %% Data Layer - Remote API (DTO)
    class WeatherApiService {
        <<interface>>
        +getWeatherByLocation(lat, lon, apiKey, units) WeatherResponse
    }

    class WeatherResponse {
        +name: String
        +main: Main
        +weather: List~WeatherInfo~
    }

    class Main {
        +temp: Double
        +feels_like: Double
        +temp_min: Double
        +temp_max: Double
        +humidity: Int
    }

    class WeatherInfo {
        +id: Int
        +main: String
        +description: String
        +icon: String
    }

    %% Data Layer - Domain Model
    class Weather {
        +locationName: String
        +temperature: Double
        +feelsLike: Double
        +description: String
        +humidity: Int
        +iconUrl: String
    }

    %% Data Layer - Location
    class LocationProvider {
        -context: Context
        -fusedLocationClient: FusedLocationProviderClient
        +hasLocationPermission() Boolean
        +getCurrentLocation() Result~Location~
    }

    class Location {
        +latitude: Double
        +longitude: Double
    }

    %% Repository Layer
    class WeatherRepository {
        -apiService: WeatherApiService
        -apiKey: String
        +getWeather(lat, lon) Flow~Result~Weather~~
    }

    %% ViewModel Layer
    class WeatherViewModel {
        -repository: WeatherRepository
        -_weatherState: MutableStateFlow~WeatherUiState~
        +weatherState: StateFlow~WeatherUiState~
        +fetchWeather(lat, lon)
    }

    class WeatherViewModelFactory {
        -repository: WeatherRepository
        +create(modelClass) ViewModel
    }

    %% UI State (Sealed Interface Pattern)
    class WeatherUiState {
        <<sealed interface>>
    }

    class Loading {
        <<data object>>
    }

    class Success {
        +weather: Weather
    }

    class Error {
        +message: String
    }

    %% Relationships - Composition and Dependencies
    LooksyApplication --> WeatherApiService : creates (lazy)
    LooksyApplication --> WeatherRepository : creates (lazy)
    LooksyApplication --> LocationProvider : creates (lazy)

    WeatherRepository --> WeatherApiService : uses
    WeatherRepository --> Weather : returns

    WeatherViewModel --> WeatherRepository : uses
    WeatherViewModel --> WeatherUiState : manages

    WeatherViewModelFactory --> WeatherRepository : requires
    WeatherViewModelFactory --> WeatherViewModel : creates

    LocationProvider --> Location : returns

    %% Data structure relationships
    WeatherResponse *-- Main : contains
    WeatherResponse *-- WeatherInfo : contains list

    WeatherApiService ..> WeatherResponse : returns

    %% UI State hierarchy
    WeatherUiState <|-- Loading : implements
    WeatherUiState <|-- Success : implements
    WeatherUiState <|-- Error : implements
    Success *-- Weather : contains

    %% Data transformation flow
    WeatherRepository ..> WeatherResponse : receives from API
    WeatherRepository ..> Weather : converts to

    %% Notes
    note for WeatherRepository "Converts DTO (WeatherResponse)\nto Domain Model (Weather)\nHandles API errors"
    note for WeatherViewModel "Exposes StateFlow for UI\nUses viewModelScope for coroutines\nFollows MVVM pattern"
    note for LooksyApplication "Lazy initialization pattern\nSingleton repository instances\nRetrofit setup with OkHttp"
```

## Key Components

### Application Layer

- **LooksyApplication**: Provides lazy-initialized dependencies for weather feature

### Data Layer

- **WeatherApiService**: Retrofit interface for OpenWeatherMap API
- **WeatherResponse/Main/WeatherInfo**: DTOs (Data Transfer Objects) from API
- **Weather**: Domain model used throughout the app
- **LocationProvider**: Handles GPS location retrieval
- **Location**: Simple latitude/longitude data class

### Repository Layer

- **WeatherRepository**: Bridges API service and ViewModel, transforms DTOs to domain models

### ViewModel Layer

- **WeatherViewModel**: Manages UI state, coordinates data fetching
- **WeatherViewModelFactory**: Creates ViewModel instances with dependencies
- **WeatherUiState**: Sealed interface representing Loading/Success/Error states

## Architecture Pattern

This follows the **MVVM (Model-View-ViewModel)** pattern with:

1. **DTO → Domain Model transformation** in the repository
2. **Sealed UI State** for type-safe state management
3. **Flow-based reactive data** for automatic UI updates
4. **Dependency injection** via LooksyApplication
