# Frontend-Backend Communication Summary

## Quick Answer

In the Looksy app, **frontend (ui/) and backend (data/) communicate through the MVVM pattern using reactive Kotlin Flows**:

```
UI Screens ←→ ViewModel ←→ Repository ←→ DAO ←→ Room Database
           (StateFlow)  (Flow)      (Flow)   (SQL)
```

---

## 🎯 Key Communication Mechanisms

### 1. **Reading Data (Reactive - Automatic Updates)**

```kotlin
// Backend (data/local/dao/ClothesDao.kt)
@Query("SELECT * FROM clothes_table")
fun getAllClothes(): Flow<List<Clothes>>  // ← Returns reactive stream

// Backend (data/repository/ClothesRepository.kt)
val allClothes: Flow<List<Clothes>> = clothesDao.getAllClothes()

// ViewModel Bridge (ui/viewmodel/ClothesViewModel.kt)
val allClothes: StateFlow<List<Clothes>> = repository.allClothes
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

// Frontend (ui/navigation/NavGraph.kt or screens/)
val allClothes by viewModel.allClothes.collectAsState()  // ← UI observes
// UI automatically updates when data changes! ✨
```

**Flow:** Database → Flow → StateFlow → UI State → Automatic Recomposition

---

### 2. **Writing Data (One-Time Actions)**

```kotlin
// Frontend (ui/screens/FullOutfitScreen.kt)
IconButton(onClick = {
    val wornClothes = listOfNotNull(top, pants, dress)
    onConfirm(wornClothes)  // ← User action
})

// Navigation/Coordinator (ui/navigation/NavGraph.kt)
onConfirm = { wornClothes ->
    val updated = wornClothes.map { it.copy(clean = false) }
    viewModel.updateAll(updated)  // ← Call ViewModel
}

// ViewModel Bridge (ui/viewmodel/ClothesViewModel.kt)
fun updateAll(clothes: List<Clothes>) = viewModelScope.launch {
    repository.updateAll(clothes)  // ← Launch coroutine
}

// Backend (data/repository/ClothesRepository.kt)
suspend fun updateAll(clothes: List<Clothes>) {
    clothesDao.updateAll(clothes)
}

// Backend (data/local/dao/ClothesDao.kt)
@Update
suspend fun updateAll(clothes: List<Clothes>)  // ← Room updates DB
```

**Flow:** User Action → Screen → NavGraph → ViewModel → Repository → DAO → Database

**Then Automatically:** Database change → Flow emits → StateFlow updates → UI recomposes

---

## 📂 Architecture Overview

```
app/src/main/java/com/example/looksy/
│
├── ui/ (FRONTEND)
│   ├── screens/              ← User-facing screens
│   │   ├── FullOutfitScreen.kt
│   │   ├── CategoriesScreen.kt
│   │   └── ...
│   ├── viewmodel/            ← State management bridge
│   │   ├── ClothesViewModel.kt
│   │   └── ClothesViewModelFactory.kt
│   ├── navigation/           ← Screen routing & coordination
│   │   ├── NavGraph.kt
│   │   └── Routes.kt
│   ├── components/           ← Reusable UI
│   └── theme/
│
└── data/ (BACKEND)
    ├── model/                ← Data entities
    │   ├── Clothes.kt
    │   ├── Outfit.kt
    │   └── [enums]
    ├── local/
    │   ├── dao/              ← Database queries
    │   │   ├── ClothesDao.kt
    │   │   └── OutfitDao.kt
    │   └── database/         ← Room setup
    │       ├── ClothesDatabase.kt
    │       └── Converters.kt
    └── repository/           ← Data layer abstraction
        ├── ClothesRepository.kt
        └── OutfitRepository.kt
```

---

## 🔄 Complete Data Flow (Real Example)

### Scenario: User Opens App and Sees Outfit

1. **NavGraph** collects data:

   ```kotlin
   val allClothes by viewModel.allClothes.collectAsState()
   ```

2. **ClothesViewModel** exposes StateFlow:

   ```kotlin
   val allClothes: StateFlow<List<Clothes>> = repository.allClothes.stateIn(...)
   ```

3. **ClothesRepository** exposes Flow:

   ```kotlin
   val allClothes: Flow<List<Clothes>> = clothesDao.getAllClothes()
   ```

4. **ClothesDao** queries database:

   ```kotlin
   @Query("SELECT * FROM clothes_table ORDER BY id DESC")
   fun getAllClothes(): Flow<List<Clothes>>
   ```

5. **Room Database** returns reactive stream
6. **NavGraph** filters and generates random outfit
7. **FullOutfitScreen** displays the outfit items

### Scenario: User Confirms Outfit (Marks as Worn)

1. **User** clicks checkmark button in FullOutfitScreen
2. **FullOutfitScreen** calls `onConfirm(wornClothes)`
3. **NavGraph** updates:
   ```kotlin
   val updated = wornClothes.map { it.copy(clean = false) }
   viewModel.updateAll(updated)
   ```
4. **ClothesViewModel** launches coroutine:
   ```kotlin
   viewModelScope.launch { repository.updateAll(clothes) }
   ```
5. **ClothesRepository** calls DAO:
   ```kotlin
   suspend fun updateAll(clothes: List<Clothes>) {
       clothesDao.updateAll(clothes)
   }
   ```
6. **ClothesDao** updates database:
   ```kotlin
   @Update suspend fun updateAll(clothes: List<Clothes>)
   ```
7. **Room Database** executes UPDATE SQL
8. **Flow automatically emits** new data (Step 1 repeats)
9. **UI automatically updates** with new outfit

---

## 🎨 Key Design Patterns

### Pattern 1: Reactive Queries with Flow

```kotlin
// DAOs always return Flow for queries that should auto-update
@Query("SELECT * FROM clothes_table WHERE type = :type")
fun getByType(type: Type): Flow<List<Clothes>>
```

### Pattern 2: StateFlow in ViewModel

```kotlin
// Convert Flow to StateFlow for UI consumption
val data: StateFlow<List<T>> = repository.data
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### Pattern 3: Suspend Functions for Writes

```kotlin
// All write operations are suspend functions
@Insert suspend fun insert(item: T)
@Update suspend fun update(item: T)
@Delete suspend fun delete(item: T)
```

### Pattern 4: viewModelScope for Coroutines

```kotlin
// ViewModel always uses viewModelScope, never GlobalScope
fun update(item: T) = viewModelScope.launch {
    repository.update(item)
}
```

### Pattern 5: Simple DI with Lazy

```kotlin
// LooksyApplication.kt provides singletons
class LooksyApplication : Application() {
    val database by lazy { ClothesDatabase.getDatabase(this) }
    val repository by lazy { ClothesRepository(database.clothesDao()) }
}
```

---

## 🔍 Detailed Diagrams

For detailed visual explanations with Mermaid diagrams, see:

1. **[architecture-diagrams.md](./architecture-diagrams.md)** - Complete architecture overview with 8 detailed diagrams including:
   - Overall MVVM architecture
   - Reactive data flow (reading)
   - Write operation flow
   - Dependency injection
   - Data transformation pipeline

2. **[code-flow-example.md](./code-flow-example.md)** - Real code walkthrough with line numbers showing:
   - Complete flow from database to FullOutfitScreen
   - Write operation when user confirms outfit
   - Data structure at each layer
   - Exact file locations and line numbers

---

## ⚡ Why This Architecture?

### ✅ Benefits

1. **Automatic UI Updates**: When database changes, UI updates automatically (no manual refresh)
2. **Separation of Concerns**: UI logic separate from data logic
3. **Testability**: Each layer can be tested independently
4. **Lifecycle Safety**: ViewModels survive screen rotation
5. **Type Safety**: Compile-time checks prevent errors
6. **Single Source of Truth**: Room database is the only data source

### 🔄 The Magic of Reactive Flows

```
User changes data → Room DB updates → Flow emits → StateFlow updates → UI recomposes
                                                                        ↑
                                                             All automatic! ✨
```

No need to:

- ❌ Manually refresh UI
- ❌ Track state changes
- ❌ Call `notifyDataSetChanged()`
- ❌ Use callbacks everywhere

Just observe the Flow and everything updates automatically!

---

## 📚 Quick Reference

| Layer          | Technology | Returns        | Purpose                        |
| -------------- | ---------- | -------------- | ------------------------------ |
| **DAO**        | Room       | `Flow<T>`      | SQL queries → reactive streams |
| **Repository** | Kotlin     | `Flow<T>`      | Abstract data source           |
| **ViewModel**  | Lifecycle  | `StateFlow<T>` | UI-friendly state              |
| **Screen**     | Compose    | `State<T>`     | Trigger recomposition          |

### Common Operations

```kotlin
// Get all items (reactive)
val items by viewModel.allItems.collectAsState()

// Get by ID (reactive)
val item by viewModel.getById(id).collectAsState(initial = null)

// Insert item
viewModel.insert(newItem)

// Update item
viewModel.update(modifiedItem)

// Delete item
viewModel.delete(item)
```

---

## 🎯 Summary

**Frontend (ui/)** displays data and handles user input
**↕️ Communication via:** StateFlow (reactive) and suspend functions (actions)
**Backend (data/)** stores and manages data in Room database

The ViewModel acts as a **bridge** that:

- Converts Flow → StateFlow for UI
- Wraps suspend functions in viewModelScope
- Survives configuration changes

This creates a **reactive, type-safe, lifecycle-aware** architecture where data flows smoothly between frontend and backend with automatic UI updates! 🚀
