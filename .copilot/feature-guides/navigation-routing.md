# Navigation & Routing

## Navigation System

**Framework**: Jetpack Navigation Compose (2.9.5)

**Main Files:**

- `Routes.kt` - Route definitions and NavHost setup
- `ScreenBlueprint.kt` - Bottom navigation scaffold

## Route Definitions

All routes are defined as a sealed class hierarchy:

```kotlin
sealed class Routes(override val route: String) : NavigationDestination {
    data object Home : Routes("home")
    data object Scan : Routes("scan")
    data object ChoseClothes : Routes("chose clothes")

    // Routes with arguments include helper functions
    data object Details : Routes("details/{id}") {
        fun createRoute(id: Int): String = "details/$id"
    }

    data object SpecificCategory : Routes("specific_category/{type}") {
        fun createRoute(type: String): String {
            val encodedPath = Uri.encode(type)
            return "specific_category/$encodedPath"
        }
    }

    data object AddNewClothes : Routes("add_new_clothes/{imageUri}") {
        fun createRoute(imageUri: String): String {
            return "add_new_clothes/$imageUri"
        }
    }
}
```

### Route Arguments

Arguments are defined as constants:

```kotlin
object RouteArgs {
    var TYPE = "imageType"
    const val IMAGE_URI = "imageUri"
    const val ID = "id"
}
```

## Bottom Navigation

**Implementation**: `ScreenBlueprint.kt`

### Bottom Nav Bar Structure

```kotlin
val navItems = listOf(
    Triple(Routes.ChoseClothes.route, "Chose Clothes", Icons.Default.Checkroom),
    Triple(Routes.Home.route, "Home", Icons.Default.Home),
    Triple(Routes.Scan.route, "Scan", Icons.Default.PhotoCamera),
    Triple(Routes.SavedOutfits.route, "Saved Outfits", Icons.Default.Favorite)
)
```

The `Scan` tab navigates to the camera (`Kamera.kt`). `Home` is the start destination.

### Selection Handling

```kotlin
NavigationBarItem(
    selected = currentDestination?.hierarchy?.any { it.route == route } == true,
    onClick = {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {}
            launchSingleTop = true
            restoreState = true
        }
    }
)
```

**Key Navigation Options:**

- `popUpTo(startDestinationId)` - Clear back stack to start
- `launchSingleTop = true` - Prevent duplicate destinations
- `restoreState = true` - Restore previous state when returning

## NavHost Configuration

**Location**: `NavHostContainer()` in `Routes.kt`

**Start Destination**: `Routes.Home.route`

### Destination: Home

Displays the full outfit (one top + one pants):

```kotlin
composable(Routes.Home.route) {
    val currentTop = top
    val currentPants = pants

    if (currentTop != null && currentPants != null) {
        FullOutfitScreen(
            top = currentTop,
            pants = currentPants,
            onClick = { clothesId ->
                navController.navigate(Routes.Details.createRoute(clothesId))
            }
        )
    } else {
        // Empty state
        Text("Füge Kleidung hinzu, um Outfits zu sehen!")
    }
}
```

**State Management:**

- `top` and `pants` selected using `remember(allClothesFromDb)`
- Defaults to first items of each type
- Updates when database changes

### Destination: ChoseClothes (Wardrobe)

Displays categorized clothing grid:

```kotlin
composable(Routes.ChoseClothes.route) {
    CategoriesScreen(
        categories = sampleCategories,
        categoryItems = categoryItems,
        onClick = { type ->
            val finalRoute = Routes.SpecificCategory.createRoute(type)
            navController.navigate(finalRoute)
        },
        onButtonClicked = { itemId ->
            navController.navigate(Routes.Details.createRoute(itemId))
        }
    )
}
```

**Dynamic Data:**

```kotlin
val categoryItems = allClothesFromDb
    .groupBy { it.type }
    .map { (type, items) ->
        CategoryItems(categoryName = type.name, items = items)
    }
```

### Destination: SpecificCategory

Shows all items of a specific type (e.g., all Pants):

```kotlin
composable(
    route = Routes.SpecificCategory.route,
    arguments = listOf(
        navArgument(RouteArgs.TYPE) { type = NavType.StringType }
    )
) { backStackEntry ->
    val encodedPath = backStackEntry.arguments?.getString(RouteArgs.TYPE)
    val type = encodedPath?.let { Uri.decode(it) }

    if (type != null) {
        SpecificCategoryScreen(
            type = Type.valueOf(type),
            viewModel = viewModel,
            onOpenDetails = { index ->
                navController.navigate(Routes.Details.createRoute(index))
            },
            onGoBack = {
                navController.navigate(Routes.ChoseClothes.route)
            }
        )
    }
}
```

**Argument Handling:**

1. Retrieve encoded string from backStackEntry
2. Decode URI-encoded value
3. Convert string to enum: `Type.valueOf()`

### Destination: Details

Shows detailed view of a single clothing item:

```kotlin
composable(
    route = Routes.Details.route,
    arguments = listOf(
        navArgument(RouteArgs.ID) { type = NavType.IntType }
    )
) { backStackEntry ->
    val clothesId = backStackEntry.arguments?.getInt(RouteArgs.ID) ?: 0
    val clothesFlow = viewModel.getClothesById(clothesId)
    val clothes by clothesFlow.collectAsState(initial = null)

    clothes?.let { clothesData ->
        ClothInformationScreen(
            clothesData = clothesData,
            viewModel = viewModel,
            onNavigateToDetails = { id ->
                navController.navigate(Routes.Details.createRoute(id))
            },
            onNavigateBack = {
                navController.popBackStack()
            },
            onConfirmOutfit = { id ->
                // Logic to update outfit selection
                navController.navigate(Routes.Home.route)
            }
        )
    }
}
```

**Flow Collection:**

- Retrieve Flow from ViewModel
- Collect as State with `initial = null`
- Wait for data before rendering screen

### Destination: Scan (Camera)

Camera interface for taking photos:

```kotlin
composable(Routes.Scan.route) {
    CameraScreenPermission(
        onImageCaptured = { imageUri ->
            val encodedUri = Uri.encode(imageUri.toString())
            navController.navigate(Routes.AddNewClothes.createRoute(encodedUri))
        }
    )
}
```

**Flow:**

1. User grants camera permission
2. Takes photo
3. Navigates to AddNewClothes with image URI

### Destination: AddNewClothes

Form to input metadata for captured photo:

```kotlin
composable(
    route = Routes.AddNewClothes.route,
    arguments = listOf(
        navArgument(RouteArgs.IMAGE_URI) { type = NavType.StringType }
    )
) { backStackEntry ->
    val imageUriString = Uri.decode(
        backStackEntry.arguments?.getString(RouteArgs.IMAGE_URI)
    )

    AddNewClothesScreen(
        imageUriString = imageUriString,
        onSave = { newItem, imageUri ->
            val path = saveImagePermanently(context, imageUri)
            val clothesWithPath = newItem.copy(imagePath = path)
            viewModel.insert(clothesWithPath)
            navController.navigate(Routes.ChoseClothes.route) {
                popUpTo(Routes.Home.route) { inclusive = false }
            }
        },
        onRetakePhoto = {
            navController.popBackStack()
        }
    )
}
```

**On Save:**

1. Move image from cache to permanent storage
2. Update Clothes object with file path
3. Insert into database
4. Navigate to wardrobe (clear back stack)

**On Retake:**

- `popBackStack()` returns to camera

## Navigation Patterns

### Type-Safe Navigation

Always use helper functions:

```kotlin
// Good
navController.navigate(Routes.Details.createRoute(clothesId))

// Avoid
navController.navigate("details/$clothesId")
```

### URI Encoding

For string arguments:

```kotlin
// When creating route
val encoded = Uri.encode(stringArg)
navController.navigate("route/$encoded")

// When reading argument
val decoded = Uri.decode(encodedArg)
```

**Why encode?**

- Prevents issues with special characters
- Required for file paths with slashes

### Back Stack Management

**Clear back stack:**

```kotlin
navController.navigate(route) {
    popUpTo(Routes.Home.route) { inclusive = false }
}
```

**Single top:**

```kotlin
navController.navigate(route) {
    launchSingleTop = true
}
```

**Pop back:**

```kotlin
navController.popBackStack()
```

## Navigation Flow Diagram

```
Bottom Navigation:
┌─────────────┬─────────┬────────┐
│ ChoseClothes│  Home   │  Scan  │
└─────────────┴─────────┴────────┘

ChoseClothes (Wardrobe)
    ↓ (tap category)
SpecificCategory (e.g., all Pants)
    ↓ (tap item)
Details (ClothInformationScreen)
    ← (back button)

Scan (Camera)
    ↓ (capture photo)
AddNewClothes (Form)
    ↓ (save)
ChoseClothes (back to wardrobe)

Home (Outfit)
    ↓ (tap item)
Details
    ← (back)
```

## Common Navigation Actions

### Navigate to Details

```kotlin
onClick = { clothesId ->
    navController.navigate(Routes.Details.createRoute(clothesId))
}
```

### Return to Home

```kotlin
navController.navigate(Routes.Home.route) {
    popUpTo(Routes.Home.route) { inclusive = true }
}
```

### Go Back

```kotlin
onNavigateBack = {
    navController.popBackStack()
}
```

## ViewModel Sharing

**ViewModel created in `ScreenBlueprint`:**

```kotlin
val application = LocalContext.current.applicationContext as ClothesApplication
val viewModelClothes: ClothesViewModel = viewModel(
    factory = ClothesViewModelFactory(application.repository)
)
```

**Passed to NavHostContainer:**

```kotlin
NavHostContainer(
    navController = navController,
    viewModel = viewModelClothes
)
```

**Shared across all destinations** for consistent data access.

## Best Practices

1. **Always encode/decode string arguments**
2. **Use type-safe route helpers** (`createRoute()`)
3. **Share ViewModel at appropriate scope**
4. **Manage back stack** to prevent confusion
5. **Handle null states** when loading from database
6. **Use `launchSingleTop`** for bottom nav items
7. **Collect Flows with `collectAsState()`** in composables

## Future Enhancements

**Potential improvements:**

1. **Deep linking** - Open specific items from notifications
2. **Saved state** - Preserve form state on config change
3. **Nested navigation** - Tab navigation within wardrobe
4. **Transitions** - Custom animations between screens
5. **Type-safe args** - Use Navigation Safe Args plugin
