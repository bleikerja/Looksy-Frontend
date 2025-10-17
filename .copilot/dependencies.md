# Dependencies & Libraries

## Core Dependencies

### Android Core

- **androidx.core:core-ktx** (1.17.0)
  - Kotlin extensions for Android framework
- **androidx.lifecycle:lifecycle-runtime-ktx** (2.9.4)
  - Lifecycle-aware components
  - `viewModelScope` for coroutines
- **androidx.lifecycle:lifecycle-runtime-compose** (2.9.4)
  - Compose lifecycle integration
  - `collectAsState()` extensions

### Jetpack Compose

- **androidx.compose.bom** (2025.09.01)
  - Bill of Materials for Compose versions
- **androidx.compose.ui** packages:
  - `ui` - Core compose runtime
  - `ui-graphics` - Graphics primitives
  - `ui-tooling-preview` - Preview annotations
  - `ui-tooling` - Debug tools
- **androidx.compose.material3**
  - Material Design 3 components
  - `Scaffold`, `TopAppBar`, `NavigationBar`, etc.
- **androidx.compose.material:material-icons-extended** (1.7.8)
  - Extended material icon set
  - Usage: `Icons.Default.Camera`, `Icons.AutoMirrored.Filled.ArrowBack`

### Activity & Composition

- **androidx.activity:activity-compose** (1.11.0)
  - `ComponentActivity` integration with Compose
  - `setContent {}` for Compose root

### Navigation

- **androidx.navigation:navigation-compose** (2.9.5)
  - **Purpose**: Type-safe navigation for Compose
  - **Key APIs**:
    - `NavHost` - Navigation container
    - `composable()` - Define destinations
    - `NavHostController` - Navigation control
  - **Usage Pattern**: See `Routes.kt` and `NavHostContainer()`

### Room Database

- **androidx.room:room-runtime** (2.8.1)
  - Room persistence library
- **androidx.room:room-ktx** (2.8.1)
  - Kotlin extensions (Flow support)
  - Coroutines integration
- **androidx.room:room-compiler** (2.8.1)
  - **Processing**: KSP (not KAPT)
  - Generates DAO implementations

**Room Key Concepts:**

- `@Database` - Database class annotation
- `@Entity` - Table definition
- `@Dao` - Data Access Object interface
- `@TypeConverters` - Custom type converters
- Returns `Flow<T>` for reactive queries
- Suspend functions for write operations

### CameraX

- **androidx.camera:camera-core** (1.5.0)
  - Core camera APIs
- **androidx.camera:camera-camera2** (1.5.0)
  - Camera2 implementation
- **androidx.camera:camera-lifecycle** (1.5.0)
  - Lifecycle-aware camera
- **androidx.camera:camera-view** (1.5.0)
  - `PreviewView` for camera preview

**CameraX Usage:**

- See `screens/Kamera.kt`
- `ProcessCameraProvider` - Camera initialization
- `ImageCapture` - Photo capture use case
- `Preview` - Camera preview use case
- Stores images in `context.cacheDir`

### Image Loading

- **io.coil-kt:coil-compose** (2.7.0)
  - **Purpose**: Image loading library for Compose
  - **Key Composable**: `AsyncImage(model = uri/path)`
  - **Features**:
    - Loads from URI, file paths, resources
    - Automatic caching
    - Placeholder support
  - **Usage**: Primary image loader for clothing photos

### Permissions

- **com.google.accompanist:accompanist-permissions** (0.37.3)
  - **Purpose**: Permission handling in Compose
  - **Key APIs**:
    - `rememberPermissionState()`
    - `PermissionState.status.isGranted`
    - `launchPermissionRequest()`
  - **Usage**: Camera permission handling in `Kamera.kt`

### Utilities

- **com.google.guava:guava** (33.5.0-android)
  - `ListenableFuture` for CameraX
  - Utility collections and functions

## Build Tools

### Gradle Plugins

- **com.android.application** (8.13.0)
  - Android Gradle Plugin
- **org.jetbrains.kotlin.android** (2.2.20)
  - Kotlin Android plugin
- **org.jetbrains.kotlin.plugin.compose** (2.2.20)
  - Compose compiler plugin
  - **Note**: Replaces older `kotlinCompilerExtensionVersion`
- **com.google.devtools.ksp** (2.2.20-2.0.3)
  - Kotlin Symbol Processing
  - Used for Room annotation processing
  - **Note**: Project uses KSP, not KAPT

## Dependency Management Strategy

**Version Catalog** (`gradle/libs.versions.toml`):

- Centralized version management
- Type-safe accessors in build scripts
- Example: `libs.androidx.room.runtime`

**Compose BOM Usage:**

- Compose library versions managed by BOM
- Individual libraries don't specify versions
- Ensures compatible Compose versions

## Testing Dependencies

### Unit Testing

- **junit:junit** (4.13.2)
  - Unit testing framework

### Instrumented Testing

- **androidx.test.ext:junit** (1.3.0)
  - AndroidX test runner
- **androidx.test.espresso:espresso-core** (3.7.0)
  - UI testing framework
- **androidx.compose.ui:ui-test-junit4**
  - Compose testing utilities

## Missing Dependencies (Potential Additions)

Consider adding these for enhanced functionality:

1. **Retrofit / Ktor** - For future backend integration (note `isSynced` field in `Clothes`)
2. **Hilt / Koin** - Dependency injection framework (currently uses manual DI)
3. **DataStore** - For preferences (alternative to SharedPreferences)
4. **WorkManager** - For background sync operations
5. **Paging 3** - If wardrobe grows large
6. **Kotlinx Serialization** - For JSON handling
7. **Timber** - Better logging
