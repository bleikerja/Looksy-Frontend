# Looksy Android App - AI Coding Agent Instructions

## Project Overview

Looksy is a wardrobe management Android app built with **Kotlin**, **Jetpack Compose**, and **MVVM architecture**. Users photograph clothing items, organize them, and plan outfits.

- **Min SDK**: 28, **Target SDK**: 36, **Java**: 11
- **Package**: `com.example.looksy`
- **Build**: Gradle with Kotlin DSL + version catalog ([gradle/libs.versions.toml](../gradle/libs.versions.toml))

## Architecture (MVVM)

### Package Structure (Updated from Legacy)

```
com.example.looksy/
├── data/
│   ├── local/
│   │   ├── dao/          # ClothesDao, OutfitDao (Room interfaces)
│   │   └── database/     # ClothesDatabase
│   ├── model/            # Clothes, Outfit entities + enums (Size, Season, Type, Material, WashingNotes)
│   └── repository/       # ClothesRepository, OutfitRepository
├── ui/
│   ├── components/       # Reusable Compose components
│   ├── navigation/       # Routes.kt - sealed class navigation
│   ├── screens/          # All @Composable screens
│   ├── theme/            # Material3 theming
│   └── viewmodel/        # ClothesViewModel + Factory
├── util/                 # Utility functions
├── LooksyApplication.kt  # DI setup (lazy database/repository)
└── MainActivity.kt       # Compose entry point
```

**Legacy Structure Note**: Old `.copilot` docs reference outdated paths like `dataClassClones/`, `ViewModels/`, `Repository/`. The **current structure** uses standard package naming: `data/model/`, `ui/viewmodel/`, `data/repository/`.

### Data Flow Pattern

```
Screen → ViewModel.stateIn() → Repository → Dao (Room) → Database
         ↓ (Flow/StateFlow)
         collectAsState() in Composable
```

**Key Patterns**:

- **DAOs return `Flow<T>`** for reactive queries (use `@Query`)
- **ViewModels convert to `StateFlow<T>`** via `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`
- **Write operations**: `suspend fun` in DAO → Repository → ViewModel wraps in `viewModelScope.launch`

## Critical Conventions

### Room Database (v2 - Two Entities)

- **Entities**: `Clothes` (clothes_table), `Outfit` (outfits_table)
- **Database**: [ClothesDatabase.kt](../app/src/main/java/com/example/looksy/data/local/database/ClothesDatabase.kt) uses **KSP** (not KAPT) for code generation
- **Type Converters**: Enums (Size, Season, Type, Material, WashingNotes) use custom TypeConverters
- **Image Storage**: File paths saved in `imagePath` field, actual files in `context.cacheDir`

**Outfit Table**: Links clothing items by ID (all nullable: `dressId`, `topsId`, `skirtId`, `pantsId`, `jacketId`)

### Dependency Injection (Simple)

```kotlin
// In LooksyApplication.kt
val database by lazy { ClothesDatabase.getDatabase(this) }
val repository by lazy { ClothesRepository(database.clothesDao()) }
val outfitRepository by lazy { OutfitRepository(database.outfitDao()) }

// In Composables
val application = LocalContext.current.applicationContext as LooksyApplication
val viewModel: ClothesViewModel = viewModel(
    factory = ClothesViewModelFactory(application.repository)
)
```

### Navigation (Type-Safe Routes)

- **File**: [ui/navigation/Routes.kt](../app/src/main/java/com/example/looksy/ui/navigation/Routes.kt)
- **Pattern**: Sealed class with `data object` destinations
- **Arguments**: Use helper functions, e.g., `Routes.Details.createRoute(id: Int) = "details/$id"`
- **Bottom Nav**: Home (FullOutfitScreen), ChoseClothes (CategoriesScreen), Scan (Kamera), WashingMachine

**Example**:

```kotlin
data object EditClothes : Routes("edit_clothes/{${RouteArgs.ID}}") {
    fun createRoute(id: Int) = "edit_clothes/$id"
}
```

### Compose Best Practices

1. **State Collection**: `val clothes by viewModel.allClothes.collectAsState()` (no initial param needed for StateFlow)
2. **Modifier Parameter**: Always last, default `Modifier = Modifier`
3. **Preview**: Wrap in `LooksyTheme { }` for Material3 theming
4. **ViewModel Scope**: Use `viewModelScope.launch` for suspend functions, never `GlobalScope`

## Key Libraries & Usage

| Library                     | Version | Usage in Looksy                                                                                                                   |
| --------------------------- | ------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **Room**                    | 2.8.1   | Local DB with KSP. Check [ClothesDao.kt](../app/src/main/java/com/example/looksy/data/local/dao/ClothesDao.kt) for query patterns |
| **CameraX**                 | 1.5.0   | [Kamera.kt](../app/src/main/java/com/example/looksy/ui/screens/Kamera.kt) - capture photos, store in `cacheDir`, save paths       |
| **Navigation Compose**      | 2.9.5   | Type-safe with sealed classes. NavHost in [Routes.kt](../app/src/main/java/com/example/looksy/ui/navigation/Routes.kt)            |
| **Coil**                    | 2.7.0   | `AsyncImage(model = filePath)` for image loading                                                                                  |
| **Accompanist Permissions** | 0.37.3  | Camera permissions - see `CameraScreenPermission` in Kamera.kt                                                                    |
| **Material Icons Extended** | 1.7.8   | `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Camera`                                                                     |

## Development Workflows

### Build & Run

```bash
# Command line (Windows)
.\gradlew assembleDebug    # Build APK
.\gradlew installDebug     # Install on device/emulator

# Or use Android Studio: Run > Run 'app'
```

### Database Changes

1. Update entity in [data/model/](../app/src/main/java/com/example/looksy/data/model/)
2. Add TypeConverter if needed (check existing enums)
3. **Increment database version** in `@Database` annotation
4. KSP auto-generates DAOs on build (no manual annotation processing)

### Adding Screens

1. Create `@Composable` in [ui/screens/](../app/src/main/java/com/example/looksy/ui/screens/)
2. Add route to `Routes` sealed class with helper function if arguments needed
3. Register in `NavHost` (search for `composable(Routes.X.route)`)
4. Update bottom nav in [ScreenBlueprint.kt](../app/src/main/java/com/example/looksy/ui/screens/ScreenBlueprint.kt) if needed

### Testing

```bash
.\gradlew test              # Unit tests
.\gradlew connectedAndroidTest  # Instrumented tests
```

**Test Dependencies**: JUnit, MockK, Coroutines Test

## Common Patterns to Follow

### Query with Filtering

```kotlin
// DAO
@Query("SELECT * FROM clothes_table WHERE type = :type AND seasonUsage = :season")
fun getFilteredClothes(type: Type, season: Season): Flow<List<Clothes>>

// ViewModel - expose as StateFlow
fun getFilteredClothes(type: Type, season: Season): StateFlow<List<Clothes>> =
    repository.getFilteredClothes(type, season)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### Image Handling (CameraX Pattern)

```kotlin
// Capture in Kamera.kt
val photoFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
// Save path to database: clothes.copy(imagePath = photoFile.absolutePath)
// Display in UI: AsyncImage(model = clothes.imagePath)
```

### Enum with TypeConverter (Existing Pattern)

```kotlin
// In data/model/
enum class NewEnum { VALUE_ONE, VALUE_TWO }

// In database/Converters.kt
class Converters {
    @TypeConverter
    fun fromNewEnum(value: NewEnum): String = value.name
    @TypeConverter
    fun toNewEnum(value: String): NewEnum = enumValueOf(value)
}
// Add to @TypeConverters in @Database annotation
```

## Known Issues & Debugging

- **"Unresolved reference" after Room changes**: Clean build (`.\gradlew clean`), sync Gradle
- **Navigation argument errors**: Ensure `Uri.encode()` for strings with special chars (see `SpecificCategory.createRoute`)
- **Image not displaying**: Check `imagePath` exists in `cacheDir` (files cleared on app uninstall)
- **StateFlow not updating UI**: Verify `collectAsState()` called inside `@Composable`, not in `remember` block

## Quick Reference Files

- **Main Routes**: [ui/navigation/Routes.kt](../app/src/main/java/com/example/looksy/ui/navigation/Routes.kt)
- **Database Schema**: [data/model/Clothes.kt](../app/src/main/java/com/example/looksy/data/model/Clothes.kt), [data/model/Outfit.kt](../app/src/main/java/com/example/looksy/data/model/Outfit.kt)
- **ViewModel Pattern**: [ui/viewmodel/ClothesViewModel.kt](../app/src/main/java/com/example/looksy/ui/viewmodel/ClothesViewModel.kt)
- **Dependencies**: [gradle/libs.versions.toml](../gradle/libs.versions.toml)
