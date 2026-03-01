# Coding Conventions & Best Practices

## Kotlin Style

### Naming Conventions

**Files:**

- PascalCase for class files: `ClothesViewModel.kt`
- Descriptive screen names: `ScreenAddNewClothes.kt`, `Kamera.kt`

**Classes & Objects:**

- PascalCase: `ClothesRepository`, `ClothesViewModel`
- Interface suffix for DAOs: `ClothesDao`
- Sealed classes for navigation: `sealed class Routes`

**Functions:**

- camelCase: `getAllClothes()`, `insert()`, `getClothesByType()`
- Boolean functions start with `is`: `isFormValid`

**Variables:**

- camelCase: `allClothes`, `navController`, `viewModel`
- Constants: ALL_CAPS_SNAKE_CASE (not commonly used in this project)

**Enums:**

- PascalCase enum name: `enum class Season`
- Enum values: PascalCase or UPPERCASE
  - This project uses **leading underscore for numeric values**: `_34`, `_36`, `_XS`, `_S`
  - Text values use PascalCase: `Winter`, `Summer`, `Cotton`, `Wool`

### Package Structure

**Package naming**: All lowercase, no underscores

- `com.example.looksy`
- `com.example.looksy.ViewModels` (Note: Typically would be lowercase `viewmodels`, but this project uses PascalCase)
- `com.example.looksy.dataClassClones`

**Unusual naming in this project:**

- `dataClassClones` - Actually contains data models and enums (not clones)
- Consider renaming to `models` or `domain` in future refactoring

## Compose Conventions

### Composable Functions

**Naming:**

- PascalCase (same as classes)
- Descriptive of what they display: `CategoriesScreen`, `OutfitPart`

**Structure:**

```kotlin
@Composable
fun ScreenName(
    // Data parameters first
    data: DataType,
    viewModel: ViewModel,
    // Callbacks
    onClick: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    // Modifier last
    modifier: Modifier = Modifier
) {
    // Composable body
}
```

**Modifier Parameter:**

- Always include `modifier: Modifier = Modifier` as last parameter
- Apply to root composable: `.then(modifier)` or directly

### Screen Structure

Every screen composable must be wrapped in `Scaffold` with a `topBar` using the shared `Header` component. This ensures consistent inset handling, back-navigation affordance, and visual uniformity across the app.

```kotlin
Scaffold(
    topBar = {
        Header(
            onNavigateBack = onNavigateBack,
            onNavigateToRightIcon = { id -> /* optional */ },
            clothesData = null,
            headerText = "Titel",
            rightIconContentDescription = null, // null hides the right icon button
            rightIcon = null,
            isFirstHeader = false   // true for bottom-nav root screens (no back arrow)
        )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }, // include only when needed
    floatingActionButton = { /* ... */ }                // include only when needed
) { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        /* screen content */
    }
}
```

**Rules:**
- `isFirstHeader = true` suppresses the back arrow; use it for bottom-nav root destinations (`Home`, `ChoseClothes`, `SavedOutfits`).
- Never place extra composables (e.g. `WeatherIconRow`) inside the `topBar` lambda; add them as the first item in the body instead.
- The only intentional exception is `Kamera.kt` — a live camera full-screen UI with no header.
- Do **not** use `statusBarsPadding()`/`navigationBarsPadding()` manually; `Scaffold` handles window insets.

### State Management

**Local State:**

```kotlin
// Use by delegates for cleaner code
var size by remember { mutableStateOf<Size?>(null) }

// Or without delegates
val (expanded, setExpanded) = remember { mutableStateOf(false) }
```

**Collecting Flow/StateFlow:**

```kotlin
val allClothes by viewModel.allClothes.collectAsState()
// NOT: collectAsState(initial = emptyList()) - initial value in StateFlow already
```

**Derived State:**

```kotlin
// Recalculates when dependency changes
val categoryItems = remember(allClothesFromDb) {
    allClothesFromDb.groupBy { it.type }
}
```

### Preview Annotations

**Usage:**

```kotlin
@Preview(showBackground = true)
@Composable
fun ScreenNamePreview() {
    LooksyTheme {
        ScreenName(
            // Mock data
        )
    }
}
```

**Theme Wrapper:**

- Always wrap in `LooksyTheme { }` for accurate preview

## ViewModel Patterns

### Initialization

**Using Factory Pattern:**

```kotlin
val application = LocalContext.current.applicationContext as ClothesApplication
val viewModel: ClothesViewModel = viewModel(
    factory = ClothesViewModelFactory(application.repository)
)
```

### Exposing State

**Use StateFlow for UI state:**

```kotlin
val allClothes: StateFlow<List<Clothes>> = repository.allClothes
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

**Return Flow directly for one-time queries:**

```kotlin
fun getClothesById(id: Int): Flow<Clothes?> {
    return repository.getClothesById(id)
}
```

### Actions

**Launch coroutines in viewModelScope:**

```kotlin
fun insert(clothes: Clothes) = viewModelScope.launch {
    repository.insert(clothes)
}
```

## Room Database Patterns

### Entity Definition

```kotlin
@Entity(tableName = "clothes_table")
data class Clothes(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // All other fields
)
```

**Key Points:**

- Use `tableName` parameter
- Auto-generate primary key
- Default value for ID: `= 0`

### DAO Patterns

**Reactive Queries (no suspend):**

```kotlin
@Query("SELECT * FROM clothes_table ORDER BY id DESC")
fun getAllClothes(): Flow<List<Clothes>>
```

**Write Operations (suspend):**

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(clothes: Clothes)
```

**Parameterized Queries:**

```kotlin
@Query("SELECT * FROM clothes_table WHERE type = :type")
fun getByType(type: Type): Flow<List<Clothes>>
```

### Type Converters

**Pattern:**

```kotlin
class Converters {
    @TypeConverter
    fun fromEnum(value: EnumType): String = value.name

    @TypeConverter
    fun toEnum(value: String): EnumType = EnumType.valueOf(value)
}
```

**Registration:**

```kotlin
@TypeConverters(Converters::class)
abstract class ClothesDatabase : RoomDatabase()
```

## Navigation Patterns

### Route Definition

```kotlin
sealed class Routes(override val route: String) : NavigationDestination {
    data object Simple : Routes("simple_route")

    data object WithArgs : Routes("route/{argName}") {
        fun createRoute(arg: String): String {
            val encoded = Uri.encode(arg)
            return "route/$encoded"
        }
    }
}
```

**Best Practices:**

- Use `data object` for routes
- Provide `createRoute()` helpers for routes with arguments
- Always encode URI arguments: `Uri.encode()`

### NavHost Setup

```kotlin
composable(
    route = Routes.WithArgs.route,
    arguments = listOf(
        navArgument("argName") { type = NavType.StringType }
    )
) { backStackEntry ->
    val arg = backStackEntry.arguments?.getString("argName")
    val decoded = Uri.decode(arg)
    // Use decoded value
}
```

## Error Handling

**Current Patterns:**

1. **Null Safety:**

   - Use nullable types: `Clothes?`
   - Safe calls: `clothes?.property`
   - Elvis operator: `clothes ?: return`

2. **Try-Catch in Camera:**

   ```kotlin
   try {
       val imageUri = imageUriString.toUri()
   } catch (e: IllegalArgumentException) {
       Uri.EMPTY
   }
   ```

3. **Form Validation:**
   ```kotlin
   val isFormValid = size != null && season != null && type != null
   Button(enabled = isFormValid) { }
   ```

## Comments

**German Comments:**

- This project contains German language comments
- Keep or translate based on team preference

**Documentation Comments:**

- Important architecture notes left in comments
- Example: ViewModel comment explaining StateFlow setup

**Commented Code:**

- Some old implementations left commented out
- Consider removing in production

## File Organization

**Screen Files:**

- Large screens can be in root package: `CategoriesScreen.kt`, `ClothInformationScreen.kt`
- OR in `screens/` package
- **Note**: This project is inconsistent - consider standardizing

**Data Classes:**

- All in `dataClassClones/` package
- Each enum in separate file

**Single Responsibility:**

- One main component per file
- Helper composables in same file if tightly coupled
- Example: `OutfitPart` in `FullOutfitScreen.kt`

## Performance Considerations

**Remember Expensive Calculations:**

```kotlin
val categoryItems = remember(allClothesFromDb) {
    allClothesFromDb.groupBy { it.type }.map { ... }
}
```

**LazyColumn Key:**

```kotlin
LazyColumn {
    items(list, key = { it.id }) { item ->
        // More efficient recomposition
    }
}
```

**Avoid Creating New Lambdas:**

```kotlin
// Good - stable lambda
onClick = { navController.navigate(...) }

// Better - if complex
val onClick = remember { { navController.navigate(...) } }
```
