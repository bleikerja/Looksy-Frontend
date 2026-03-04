# Architecture Guide

## Architecture Pattern: MVVM

Looksy follows the **MVVM (Model-View-ViewModel)** architecture pattern with a clear separation of concerns:

```
View (Composables) ←→ ViewModel ←→ Repository ←→ DAO ←→ Room Database
```

### Layer Breakdown

#### 1. **View Layer (UI/Screens)**

- **Location**: `screens/` package and root-level screen files
- **Technology**: Jetpack Compose
- **Responsibilities**:
  - Render UI based on ViewModel state
  - Collect StateFlow/Flow from ViewModels
  - Handle user interactions and delegate to ViewModels
  - Navigation between screens

**Key Screens** (`ui/screens/`):

- `ScreenBlueprint.kt` - Main scaffold with bottom navigation (4 tabs)
- `CategoriesScreen.kt` - Grid view of clothing categories
- `SpecificCategoryScreen.kt` - List of items for a specific `Type`
- `ClothInformationScreen.kt` - Detail view of a single clothing item
- `FullOutfitScreen.kt` - Display complete outfit with weather strip
- `ScreenAddNewClothes.kt` / `AddNewClothesScreen.kt` - Form to add new clothing
- `Kamera.kt` - Camera integration with CameraX
- `OutfitDetailsScreen.kt` - View/edit a saved outfit
- `SavedOutfitsScreen.kt` - List of all saved outfits
- `DiscardScreen.kt` - Bulk-discard clothes with undo support
- `WashingMachineScreen.kt` - Mark selected items as clean/dirty
- `WeatherScreen.kt` - Weather display with GPS + manual city fallback

#### 2. **ViewModel Layer**

- **Location**: `ui/viewmodel/`
- **ViewModels**: `ClothesViewModel`, `OutfitViewModel` (Room-backed); `WeatherViewModel`, `GeocodingViewModel` (network-backed)
- **Responsibilities**:
  - Expose UI state as StateFlow
  - Process user actions
  - Communicate with Repository
  - Maintain lifecycle-aware data streams

**Pattern Used:**

```kotlin
class ClothesViewModel(private val repository: ClothesRepository) : ViewModel() {
    // StateFlow for reactive UI updates
    val allClothes: StateFlow<List<Clothes>> = repository.allClothes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Suspend functions for data manipulation
    fun insert(clothes: Clothes) = viewModelScope.launch {
        repository.insert(clothes)
    }
}
```

**ViewModel Creation:**

- ViewModels are created using `ClothesViewModelFactory`
- Factory receives `ClothesRepository` from `ClothesApplication`
- Ensures ViewModel has proper dependencies

#### 3. **Repository Layer**

- **Location**: `Repository/ClothesRepository.kt`
- **Responsibilities**:
  - Abstraction over data sources
  - Single source of truth for data access
  - Transform data if needed between DAO and ViewModel

**Pattern:**

```kotlin
class ClothesRepository(private val clothesDao: ClothesDao) {
    val allClothes: Flow<List<Clothes>> = clothesDao.getAllClothes()

    suspend fun insert(clothes: Clothes) {
        clothesDao.insert(clothes)
    }
}
```

#### 4. **DAO Layer**

- **Location**: `dao/ClothesDao.kt`
- **Technology**: Room Database
- **Responsibilities**:
  - Define database queries
  - Return Flow for reactive queries
  - Handle CRUD operations

**Pattern:**

```kotlin
@Dao
interface ClothesDao {
    @Query("SELECT * FROM clothes_table ORDER BY id DESC")
    fun getAllClothes(): Flow<List<Clothes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clothes: Clothes)
}
```

#### 5. **Model Layer**

- **Location**: `data/model/` package
- **Entities**: `Clothes.kt`, `Outfit.kt` (both Room `@Entity`)
- **Enums**: `Size`, `Season`, `Type`, `Material`, `WashingNotes`, `ClothesColor`
- **Utilities** (`util/`): `OutfitGenerator.kt`, `OutfitCompatibilityCalculator.kt`, `ImageStorage.kt`

### Data Flow Examples

#### Reading Data (Reactive):

```
Room DB → DAO (Flow) → Repository (Flow) → ViewModel (StateFlow) → Composable (collectAsState)
```

#### Writing Data:

```
Composable (user action) → ViewModel (viewModelScope.launch) → Repository (suspend fun) → DAO (suspend fun) → Room DB
```

### Dependency Injection Pattern

**Application-level DI** (`LooksyApplication`, simple `by lazy` initialization):

```kotlin
class LooksyApplication : Application() {
    val database by lazy { ClothesDatabase.getDatabase(this) }
    val repository by lazy { ClothesRepository(database.clothesDao()) }
    val outfitRepository by lazy { OutfitRepository(database.outfitDao()) }
    val weatherRepository by lazy { WeatherRepository(weatherApiService, BuildConfig.WEATHER_API_KEY) }
    val geocodingRepository by lazy { GeocodingRepository(geocodingApiService, BuildConfig.WEATHER_API_KEY) }
    val locationProvider by lazy { LocationProvider(this) }
    // Retrofit services also initialized lazily here
}
```

**ViewModel creation in `ScreenBlueprint`:**

```kotlin
val application = LocalContext.current.applicationContext as LooksyApplication
val viewModelClothes: ClothesViewModel = viewModel(factory = ClothesViewModelFactory(application.repository))
val viewModelOutfit: OutfitViewModel = viewModel(factory = OutfitViewModelFactory(application.outfitRepository))
val viewModelWeather: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(application.weatherRepository))
// GeocodingViewModel is created inline in NavGraph via viewModel(factory = ...)
```

## Navigation Architecture

**Navigation System**: Jetpack Navigation Compose

**Key Components:**

- `Routes.kt` - Sealed class defining all destinations
- `ScreenBlueprint.kt` - Main scaffold with bottom navigation
- `NavHostContainer()` - NavHost configuration

**Route Pattern:**

```kotlin
sealed class Routes(override val route: String) : NavigationDestination {
    data object Home : Routes("home")
    data object Details : Routes("details/{id}") {
        fun createRoute(id: Int): String = "details/$id"
    }
}
```

**Navigation with Arguments:**

- Type-safe route creation using helper functions
- URI encoding for string parameters
- NavType specification in composable() arguments

## State Management

**Reactive Patterns:**

- `Flow` - From Room DAO for continuous data streams
- `StateFlow` - In ViewModels for UI state
- `collectAsState()` - In Composables to observe StateFlow
- `remember` / `mutableStateOf` - For local UI state

**Example:**

```kotlin
// In Composable
val allClothes by viewModel.allClothes.collectAsState()

// Automatically recomposes when data changes
LazyColumn {
    items(allClothes) { clothes ->
        ClothesItem(clothes)
    }
}
```

## Database Design

**Two entities** — bump `version` in `ClothesDatabase` and run `./gradlew clean assembleDebug` on any schema change.

**`Clothes` entity** (`clothes_table`):

```kotlin
@Entity(tableName = "clothes_table")
data class Clothes(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: Size,
    val seasonUsage: Season,
    val type: Type,
    val material: Material,
    val color: ClothesColor? = null,
    val clean: Boolean,
    val washingNotes: List<WashingNotes>,  // Gson-serialized list
    val selected: Boolean = false,
    val wornClothes: Int = 0,
    val daysWorn: Int = 0,
    val wornSince: Long? = null,
    val lastWorn: Long? = null,
    val imagePath: String = "",
    val isSynced: Boolean = false
)
```

**`Outfit` entity** (`outfits_table`):

```kotlin
@Entity(tableName = "outfits_table")
data class Outfit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dressId: Int? = null,
    val topsId: Int? = null,
    val skirtId: Int? = null,
    val pantsId: Int? = null,
    val jacketId: Int? = null,
    val shoesId: Int? = null,
    val preference: Int = 0,
    val isSynced: Boolean = false,
    val isManuelSaved: Boolean = false
)
```

**Type Converters** (`Converters.kt`):

- All simple enums → `enum.name` / `Enum.valueOf(string)`
- `List<WashingNotes>` → JSON via `Gson().toJson()` / `Gson().fromJson()`
- When adding a new enum field, always add a converter pair

**Current DB version: 6** — `fallbackToDestructiveMigration()` clears data on upgrade.

## Image Storage Strategy

- Camera (`Kamera.kt`) captures to `context.cacheDir` as a temp file.
- `saveImagePermanently(context, uri)` in `util/ImageStorage.kt` copies to `context.filesDir/images/` with a timestamped filename, returns the absolute path.
- Absolute path is stored in `Clothes.imagePath` and displayed via Coil: `AsyncImage(model = imagePath)`.

## Outfit Generation Logic (`util/`)

- `generateSingleRandomOutfit(allClothes, allOutfits)` in `OutfitGenerator.kt`:
  - 70% probability: clothes list weighted by `wornClothes + 1`; 30%: uniform.
  - 30% probability: tries to reuse a saved `Outfit` weighted by `preference`.
  - Falls back to random slot-fill if no compatible saved outfit exists.
- `OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)` returns 0–100:
  - Factors: season (30%), material (25%), type (20%), size (15%), clean (10%), color multiplier.
  - Score = 0 disqualifies an outfit (≥3 different ACCENT colors also disqualifies).
- Both are pure functions — unit-testable without Android context.

## Theming

**Material 3** with custom color scheme defined in `ui/theme/`

- Dynamic color support
- Custom typography
- Consistent spacing and shapes
