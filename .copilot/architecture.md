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

**Key Screens:**

- `ScreenBlueprint.kt` - Main scaffold with bottom navigation
- `CategoriesScreen.kt` - Grid view of clothing categories
- `SpecificCategoryScreen.kt` - List of items for a specific type
- `ClothInformationScreen.kt` - Detail view of a single item
- `FullOutfitScreen.kt` - Display complete outfit (top + pants)
- `AddNewClothesScreen.kt` - Form to add new clothing items
- `Kamera.kt` - Camera integration with CameraX

#### 2. **ViewModel Layer**

- **Location**: `ViewModels/ClothesViewModel.kt`
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

- **Location**: `dataClassClones/` package
- **Components**:
  - `Clothes.kt` - Main entity (Room `@Entity`)
  - Enum classes: `Size`, `Season`, `Type`, `Material`, `WashingNotes`
  - `Filter.kt` - Utility class for filtering lists

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

**Application-level DI** (simple lazy initialization):

```kotlin
class ClothesApplication : Application() {
    val database by lazy { ClothesDatabase.getDatabase(this) }
    val repository by lazy { ClothesRepository(database.clothesDao()) }
}
```

**ViewModel Factory Pattern:**

```kotlin
class ClothesViewModelFactory(private val repository: ClothesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClothesViewModel::class.java)) {
            return ClothesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

**Usage in Composables:**

```kotlin
val application = LocalContext.current.applicationContext as ClothesApplication
val viewModel: ClothesViewModel = viewModel(
    factory = ClothesViewModelFactory(application.repository)
)
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

**Single Entity**: `Clothes`

**Schema:**

```kotlin
@Entity(tableName = "clothes_table")
data class Clothes(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: Size,
    val seasonUsage: Season,
    val type: Type,
    val material: Material,
    val clean: Boolean,
    val washingNotes: WashingNotes,
    val imagePath: String = "",
    val isSynced: Boolean = false
)
```

**Type Converters:**

- All enum types (Size, Season, Type, Material, WashingNotes) use `Converters.kt`
- Converts enums to String for storage: `enum.name` → String
- Converts back using: `Enum.valueOf(string)`

**Database Access Pattern:**

- Singleton pattern via `ClothesDatabase.getDatabase(context)`
- Thread-safe with `@Volatile` and `synchronized`
- `fallbackToDestructiveMigration()` - Simple migration strategy (deletes DB on schema change)

## Image Storage Strategy

**Current Approach:**

- Photos stored in app's cache directory: `context.cacheDir`
- File path stored as String in database (`imagePath` field)
- Loaded using Coil library: `AsyncImage(model = imagePath)`

**Camera Integration:**

- CameraX library for camera access
- Permission handling via Accompanist Permissions
- Temporary file created in cache, then copied to persistent storage

## Theming

**Material 3** with custom color scheme defined in `ui/theme/`

- Dynamic color support
- Custom typography
- Consistent spacing and shapes
