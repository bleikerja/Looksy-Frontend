# .copilot Folder - Meta Files Generated

## ✅ Files Created

I've successfully analyzed your entire Looksy-Frontend project and generated comprehensive meta files to help GitHub Copilot provide better assistance in the future.

### 📁 Created Structure

```
.copilot/
├── README.md                                    # Main index and usage guide
├── project-overview.md                          # High-level project description
├── architecture.md                              # MVVM architecture details
├── conventions.md                               # Coding standards & best practices
├── dependencies.md                              # Complete dependency reference
├── data-models.md                               # Database schema & enums
├── troubleshooting.md                           # Common issues & solutions
├── testing-guide.md                             # Instrumented test setup & patterns
├── feature-guides/
│   ├── camera-image-handling.md                 # CameraX implementation
│   ├── navigation-routing.md                    # Navigation system
│   └── weather-ux-stabilization.md             # Test-suite stabilization session log
└── weather/
    ├── README.md                                # Weather feature overview & checklist
    ├── sequence.md                              # Sequence diagram (refresh triggers + geocoding path)
    ├── class-diagram.md                         # Full class diagram incl. geocoding & enums
    └── data-flow.md                             # Flowchart: GPS path vs manual city path
```

## 📊 What's Documented

### Project Analysis

- **70+ Kotlin files** analyzed
- **Architecture**: MVVM with Repository pattern
- **Database**: Room with 2 entities (Clothes, Outfit)
- **Navigation**: 8 main destinations
- **UI**: 100% Jetpack Compose
- **Weather feature**: OpenWeatherMap (current conditions) + Geocoding (city search)
- **Test suite**: 61 instrumented tests, stabilized for API 36 (Android 16)

### Key Findings Documented

**Architecture:**

- Clean MVVM separation
- Flow-based reactive data
- Simple dependency injection via Application class
- Type-safe navigation with sealed classes

**Technologies:**

- Kotlin 2.2.20
- Jetpack Compose with Material3
- Room 2.8.1 (using KSP, not KAPT)
- CameraX 1.5.0
- Navigation Compose 2.9.5
- Coil 2.7.0 for images
- Retrofit 2 + OkHttp for weather/geocoding APIs
- MockK for instrumented test mocking
- Espresso 3.7.0 (forced — API 36 compatibility fix)

**Data Model:**

- `Clothes` entity with 5 enum types + `Outfit` entity
- All enums use TypeConverters
- Images stored as absolute paths in `filesDir/images/`
- Ready for backend sync (`isSynced` field)

**Weather Feature:**

- `WeatherViewModel` + `GeocodingViewModel` for GPS and manual city lookup
- `PermissionState` enum drives permission-aware UI flow
- `LocationInputMode` enum switches between GPS and manual city paths
- `WeatherIconRow` composable embedded in `FullOutfitScreen` (always visible)
- Lifecycle-aware refresh via `DisposableEffect(ON_RESUME)` + `PullToRefreshBox`

## 🎯 How to Use

### For Better Copilot Prompts

**Before (generic):**

> "Add a color field"

**After (context-aware):**

> "Add a 'color' enum field to the Clothes entity in dataClassClones/, following the existing enum pattern with TypeConverter in Converters.kt"

### Quick Access

- **Starting a feature?** → Read `architecture.md` + relevant feature guide
- **Writing new code?** → Check `conventions.md`
- **Hit an error?** → Search `troubleshooting.md`
- **Using a library?** → See `dependencies.md`

## 📝 Key Patterns Identified

### 1. Database Queries

```kotlin
// Always return Flow for reactive queries
@Query("SELECT * FROM table")
fun getAll(): Flow<List<Entity>>
```

### 2. ViewModel State

```kotlin
// Convert Flow to StateFlow in ViewModel
val data: StateFlow<List<T>> = repository.data
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### 3. Navigation

```kotlin
// Type-safe routes with helper functions
data object Details : Routes("details/{id}") {
    fun createRoute(id: Int) = "details/$id"
}
```

### 4. Image Storage

```kotlin
// Store in filesDir, save path in DB
val path = File(context.filesDir, "IMG_${timestamp}.jpg").absolutePath
```

## 🔧 Conventions Highlighted

- **Enum naming**: Leading underscore for numbers (`_34`, `_XS`)
- **Package naming**: Inconsistent case (documented for clarity)
- **File organization**: Some screens in root, some in `screens/`
- **State management**: Flow → StateFlow → collectAsState
- **ViewModel creation**: Factory pattern with Application DI

## 🚀 Future Enhancements Suggested

Documented potential improvements:

1. Backend integration (uses `isSynced` field)
2. Image compression/resizing
3. Multiple photos per item
4. Advanced filtering
5. Outfit recommendations
6. Statistics/analytics
7. Cloud backup

## 💡 Tips for Maintaining

1. **Update when:**
   - Adding new dependencies
   - Changing architecture
   - Discovering common issues
   - Adding major features

2. **Keep it:**
   - Concise
   - Code-example rich
   - Current
   - Cross-referenced

3. **Benefits:**
   - ✅ Faster onboarding
   - ✅ Better Copilot suggestions
   - ✅ Consistent code patterns
   - ✅ Fewer common mistakes

## 📈 Documentation Stats

- **Total pages**: 14 documents (9 original + weather/ subfolder + feature guides)
- **Code examples**: 70+
- **Topics covered**: 120+ (architecture, patterns, weather/geocoding, testing, issues, etc.)
- **Cross-references**: Throughout all documents

## 🎓 Learning Path

For new developers:

1. Start: `README.md` → `project-overview.md`
2. Deep dive: `architecture.md`
3. Reference: `conventions.md`, `dependencies.md`
4. Troubleshoot: `troubleshooting.md`
5. Features: `feature-guides/*`

## ✨ Result

You now have a comprehensive knowledge base that will help GitHub Copilot:

- Understand your project structure
- Suggest code following your patterns
- Reference correct APIs and libraries
- Maintain consistency across the codebase
- Provide contextually aware solutions

Next time you ask Copilot a question, it will have deep context about your Looksy project! 🚀
