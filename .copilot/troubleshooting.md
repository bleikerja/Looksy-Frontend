# Common Issues & Solutions

## Build Issues

### KSP vs KAPT

**Issue**: "Cannot find symbol" errors for Room-generated code

**Solution**: This project uses **KSP** (not KAPT)

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(libs.androidx.room.compiler) // NOT kapt()
}
```

**Clean build:**

```powershell
.\gradlew clean
.\gradlew build
```

### Compose Compiler Version

**Issue**: Incompatible Compose compiler version

**Solution**: Use Compose Compiler Plugin (Kotlin 2.0+)

```kotlin
plugins {
    alias(libs.plugins.kotlin.compose) // Handles compiler automatically
}
```

No need for manual `kotlinCompilerExtensionVersion`.

### Type Converter Not Found

**Issue**: Room error about enum types

**Solution**: Ensure `@TypeConverters` is registered on database:

```kotlin
@Database(entities = [Clothes::class], version = 1)
@TypeConverters(Converters::class) // MUST include this
abstract class ClothesDatabase : RoomDatabase()
```

## Runtime Issues

### Camera Permission Denied

**Issue**: Camera doesn't open after permission request

**Check:**

1. Manifest declaration:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

2. Permission state:

```kotlin
if (cameraPermissionState.status.isGranted) {
    // Open camera
} else {
    // Show permission UI
}
```

3. Device has camera:

```xml
<uses-feature android:name="android.hardware.camera.any" android:required="true" />
```

**Debug**: Check Logcat for permission denial messages

### Image Not Displaying

**Issue**: `AsyncImage` shows nothing

**Possible causes:**

1. **Invalid file path**

```kotlin
// Check if file exists
val file = File(imagePath)
if (!file.exists()) {
    Log.e("Image", "File not found: $imagePath")
}
```

2. **URI format issue**

```kotlin
// For file paths, ensure proper format
val uri = Uri.fromFile(File(imagePath))
```

3. **Coil not initialized**

- Ensure `coil-compose` dependency is included
- Check for Coil errors in Logcat

### Flow Not Emitting

**Issue**: UI not updating when database changes

**Check:**

1. Using `Flow` (not suspend fun) in DAO:

```kotlin
@Query("SELECT * FROM clothes_table")
fun getAllClothes(): Flow<List<Clothes>> // Correct
```

2. Converting to StateFlow in ViewModel:

```kotlin
val allClothes: StateFlow<List<Clothes>> = repository.allClothes
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

3. Collecting in Composable:

```kotlin
val clothes by viewModel.allClothes.collectAsState()
```

### Database Schema Mismatch

**Issue**: App crashes with "IllegalStateException: Room cannot verify the data integrity"

**Cause**: Database schema changed but version not incremented

**Solution:**

1. **For development** (quick fix):

```kotlin
.fallbackToDestructiveMigration()
```

This deletes and recreates the database.

2. **For production** (proper fix):

- Increment version number
- Provide migration strategy

```kotlin
@Database(entities = [Clothes::class], version = 2)
```

**Force clean:**

```powershell
.\gradlew clean
# Then uninstall app from device/emulator
```

## Navigation Issues

### Back Navigation Not Working

**Issue**: Back button doesn't navigate properly

**Check:**

1. Using correct method:

```kotlin
navController.popBackStack() // Correct
```

2. Back stack not empty:

```kotlin
if (navController.previousBackStackEntry != null) {
    navController.popBackStack()
} else {
    // At root, exit app
}
```

### Argument Null in Destination

**Issue**: `backStackEntry.arguments?.getString()` returns null

**Solution:**

1. **Encode when navigating:**

```kotlin
val encoded = Uri.encode(argument)
navController.navigate("route/$encoded")
```

2. **Declare argument in composable:**

```kotlin
composable(
    route = "route/{arg}",
    arguments = listOf(
        navArgument("arg") { type = NavType.StringType }
    )
)
```

3. **Decode when reading:**

```kotlin
val decoded = Uri.decode(backStackEntry.arguments?.getString("arg"))
```

### "No destination found" Error

**Issue**: Navigation crash with unknown destination

**Cause**: Route string doesn't match composable route

**Solution:**

- Always use `Routes.DestinationName.route`
- For arguments, use `Routes.DestinationName.createRoute(arg)`

## UI Issues

### Compose Preview Not Showing

**Issue**: `@Preview` doesn't render

**Common causes:**

1. **Missing theme wrapper:**

```kotlin
@Preview
@Composable
fun MyPreview() {
    LooksyTheme { // MUST wrap in theme
        MyScreen()
    }
}
```

2. **Constructor parameters:**

```kotlin
// Add default values or mock data
@Preview
@Composable
fun MyPreview() {
    LooksyTheme {
        MyScreen(
            data = mockData,
            onClick = {}
        )
    }
}
```

3. **Build not up to date:**

- Rebuild project: Build > Rebuild Project

### LazyColumn Items Not Updating

**Issue**: List doesn't refresh when data changes

**Solution:**

1. **Add key parameter:**

```kotlin
LazyColumn {
    items(clothes, key = { it.id }) { item ->
        ClothesItem(item)
    }
}
```

2. **Ensure Flow is being collected:**

```kotlin
val clothes by viewModel.allClothes.collectAsState()
```

### Bottom Navigation Flickering

**Issue**: Bottom nav selection jumps between items

**Cause**: State not properly managed

**Solution:**

```kotlin
val navBackStackEntry by navController.currentBackStackEntryAsState()
val currentDestination = navBackStackEntry?.destination

NavigationBarItem(
    selected = currentDestination?.hierarchy?.any { it.route == route } == true
)
```

## Performance Issues

### App Slow When Loading Many Images

**Solution:**

1. **Image compression:**

```kotlin
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
```

2. **Lazy loading with Coil:**

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imagePath)
        .crossfade(true)
        .size(800) // Resize
        .build(),
    contentDescription = null
)
```

3. **Pagination:**

- Implement paging for large wardrobes
- Load categories on demand

### Memory Leaks

**Issue**: App slows down over time

**Check:**

1. **ViewModel scope for coroutines:**

```kotlin
viewModelScope.launch { // NOT GlobalScope
    // Work here
}
```

2. **Lifecycle-aware observers:**

```kotlin
// In Composable, using collectAsState()
val state by viewModel.state.collectAsState()
```

## Testing Issues

### Room Tests Fail

**Issue**: In-memory database tests failing

**Solution:**

```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: ClothesDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ClothesDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        db.close()
    }
}
```

## Debugging Tips

### Enable Room Logging

In `ClothesDatabase.kt`:

```kotlin
Room.databaseBuilder(context, ClothesDatabase::class.java, "clothes_database")
    .setQueryCallback({ sqlQuery, bindArgs ->
        Log.d("RoomQuery", "Query: $sqlQuery Args: $bindArgs")
    }, Executors.newSingleThreadExecutor())
    .build()
```

### Log Navigation Events

In NavHost:

```kotlin
LaunchedEffect(navController) {
    navController.currentBackStackEntryFlow.collect { entry ->
        Log.d("Navigation", "Current: ${entry.destination.route}")
    }
}
```

### Check ViewModel State

Add logging in ViewModel:

```kotlin
val allClothes: StateFlow<List<Clothes>> = repository.allClothes
    .onEach { Log.d("ViewModel", "Clothes count: ${it.size}") }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

## Common Error Messages

### "Cannot create an instance of ClothesViewModel"

**Cause**: ViewModel not created with factory

**Solution:**

```kotlin
val application = LocalContext.current.applicationContext as ClothesApplication
val viewModel: ClothesViewModel = viewModel(
    factory = ClothesViewModelFactory(application.repository)
)
```

### "IllegalStateException: ViewModelStore should be set before making this navController available"

**Cause**: NavController used before initialization

**Solution:**

```kotlin
val navController = rememberNavController() // Create in Composable
```

### "java.lang.IllegalArgumentException: Unknown ViewModel class"

**Cause**: Factory doesn't handle ViewModel type

**Check:** `ClothesViewModelFactory.kt`:

```kotlin
if (modelClass.isAssignableFrom(ClothesViewModel::class.java)) {
    return ClothesViewModel(repository) as T
}
```

## Getting Help

**When asking for help, include:**

1. **Full error message** from Logcat
2. **Stack trace** (if crash)
3. **Code context** (relevant files)
4. **Steps to reproduce**
5. **Device/emulator info**
6. **Build configuration** (if build issue)

**Useful Logcat filters:**

```
tag:RoomQuery
tag:Navigation
tag:Camera
package:com.example.looksy
```
