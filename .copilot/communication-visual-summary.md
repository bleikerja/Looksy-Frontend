# Frontend-Backend Communication - Visual Summary

## Simple Overview

```mermaid
graph TB
    subgraph Frontend["🎨 FRONTEND (ui/)"]
        S1["FullOutfitScreen<br/>displays outfit"]
        S2["CategoriesScreen<br/>shows categories"]
        S3["Other Screens"]
    end

    subgraph Bridge["🌉 BRIDGE (ViewModel)"]
        VM["ClothesViewModel<br/>Manages state<br/>Coordinates operations"]
    end

    subgraph Backend["💾 BACKEND (data/)"]
        R["Repository<br/>Data abstraction"]
        D["DAO<br/>SQL queries"]
        DB["Room Database<br/>SQLite storage"]
    end

    S1 <-->|"StateFlow<br/>(reactive)"| VM
    S2 <-->|"StateFlow<br/>(reactive)"| VM
    S3 <-->|"StateFlow<br/>(reactive)"| VM

    VM <-->|"Flow +<br/>suspend fun"| R
    R <-->|"Flow +<br/>suspend fun"| D
    D <-->|"SQL"| DB

    style Frontend fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    style Bridge fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    style Backend fill:#fff3e0,stroke:#e65100,stroke-width:2px
```

---

## Communication Types

### 1️⃣ Read Data (Automatic Updates)

```mermaid
flowchart LR
    A["💾 Database<br/>clothes_table"]
    B["🗄️ DAO<br/>Flow&lt;List&gt;"]
    C["💼 Repo<br/>Flow&lt;List&gt;"]
    D["🔄 ViewModel<br/>StateFlow&lt;List&gt;"]
    E["📱 Screen<br/>State&lt;List&gt;"]
    F["👤 User sees<br/>outfit"]

    A -->|emits| B
    B -->|passes| C
    C -->|converts| D
    D -->|observes| E
    E -->|displays| F

    style A fill:#fff3e0
    style D fill:#f3e5f5
    style F fill:#e8f5e9
```

**Key:** Data automatically flows from database to UI. When database changes, UI updates automatically! ⚡

---

### 2️⃣ Write Data (User Actions)

```mermaid
flowchart LR
    A["👤 User clicks<br/>confirm button"]
    B["📱 Screen<br/>onConfirm()"]
    C["🔄 ViewModel<br/>updateAll()"]
    D["💼 Repo<br/>updateAll()"]
    E["🗄️ DAO<br/>@Update"]
    F["💾 Database<br/>UPDATE SQL"]

    A -->|triggers| B
    B -->|calls| C
    C -->|calls| D
    D -->|calls| E
    E -->|executes| F

    F -.->|"emits new data"| A

    style A fill:#ffebee
    style C fill:#f3e5f5
    style F fill:#fff3e0
```

**Key:** User action triggers write → Database updates → Reactive flow automatically updates UI! 🔄

---

## Technology Stack

```mermaid
graph LR
    subgraph UI["Frontend Layer"]
        Compose["Jetpack Compose<br/>(UI Framework)"]
        Material["Material3<br/>(Design System)"]
    end

    subgraph State["State Management"]
        VM["ViewModel<br/>(Android Architecture)"]
        Flow["Kotlin Flow<br/>(Reactive Streams)"]
        Coroutines["Coroutines<br/>(Async Operations)"]
    end

    subgraph Data["Data Layer"]
        Room["Room Database<br/>(SQLite ORM)"]
        KSP["KSP<br/>(Code Generation)"]
    end

    Compose --> VM
    VM --> Flow
    VM --> Coroutines
    Flow --> Room
    Coroutines --> Room
    Room --> KSP
    Material --> Compose

    style UI fill:#e1f5ff
    style State fill:#f3e5f5
    style Data fill:#fff3e0
```

---

## Real Example: Outfit Display

### The Flow

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant UI as 📱 FullOutfitScreen
    participant VM as 🔄 ViewModel
    participant DB as 💾 Database

    Note over User,DB: App Launch

    User->>UI: Opens app
    UI->>VM: collectAsState()
    VM->>DB: Query all clothes
    DB-->>VM: Flow emits data
    VM-->>UI: StateFlow updates
    UI-->>User: Shows outfit

    Note over User,DB: User Confirms Outfit

    User->>UI: Clicks ✓ button
    UI->>VM: updateAll(worn clothes)
    VM->>DB: UPDATE clean = false

    Note over DB: Database changed!

    DB-->>VM: Flow emits new data
    VM-->>UI: StateFlow updates
    UI-->>User: Shows new outfit

    Note over User: All automatic! ✨
```

---

## File Organization

### Frontend Files (ui/)

```
ui/
├── screens/
│   ├── FullOutfitScreen.kt        ← Displays outfit (top + pants)
│   ├── CategoriesScreen.kt        ← Shows clothing categories
│   ├── SpecificCategoryScreen.kt  ← Lists items in category
│   ├── ClothInformationScreen.kt  ← Item details
│   ├── ScreenAddNewClothes.kt     ← Add new item form
│   ├── WashingMachineScreen.kt    ← Mark items as clean
│   └── Kamera.kt                  ← Camera integration
│
├── viewmodel/
│   ├── ClothesViewModel.kt        ← State management
│   └── ClothesViewModelFactory.kt ← ViewModel creation
│
├── navigation/
│   ├── NavGraph.kt                ← Screen routing
│   └── Routes.kt                  ← Route definitions
│
└── components/
    ├── Header.kt                  ← Reusable header
    └── LooksyButton.kt            ← Custom button
```

### Backend Files (data/)

```
data/
├── model/
│   ├── Clothes.kt                 ← Main entity
│   ├── Outfit.kt                  ← Outfit entity
│   ├── Size.kt                    ← Size enum
│   ├── Season.kt                  ← Season enum
│   ├── Type.kt                    ← Type enum
│   ├── Material.kt                ← Material enum
│   └── WashingNotes.kt            ← WashingNotes enum
│
├── local/
│   ├── dao/
│   │   ├── ClothesDao.kt          ← Clothes queries
│   │   └── OutfitDao.kt           ← Outfit queries
│   │
│   └── database/
│       ├── ClothesDatabase.kt     ← Database definition
│       └── Converters.kt          ← Type converters
│
└── repository/
    ├── ClothesRepository.kt       ← Clothes data access
    └── OutfitRepository.kt        ← Outfit data access
```

---

## Communication Methods

### Method 1: Reactive Queries (Automatic)

```kotlin
// In Screen
val clothes by viewModel.allClothes.collectAsState()

// Updates automatically when database changes!
```

### Method 2: User Actions (On-Demand)

```kotlin
// In Screen
IconButton(onClick = {
    viewModel.update(modifiedClothes)
})

// Triggers database update, then UI auto-updates via Flow
```

---

## Key Concepts

### 🌊 Flow vs StateFlow

| Type          | Usage                     | Lifecycle                             |
| ------------- | ------------------------- | ------------------------------------- |
| **Flow**      | Backend (DAO, Repository) | Cold - starts on collection           |
| **StateFlow** | ViewModel → UI            | Hot - always active with latest value |

### 🔄 Reactive Updates

```mermaid
graph LR
    A[Database Change] --> B[Flow Emits]
    B --> C[StateFlow Updates]
    C --> D[UI Recomposes]
    D --> E[User Sees Change]

    style A fill:#fff3e0
    style C fill:#f3e5f5
    style E fill:#e8f5e9
```

**Magic:** All automatic! No manual UI refresh needed ✨

---

## Why This Design?

### ✅ Advantages

1. **Automatic Updates**: UI always shows latest data
2. **Clean Separation**: Frontend doesn't know about SQL
3. **Testable**: Each layer tests independently
4. **Type Safe**: Compile-time error checking
5. **Lifecycle Aware**: Survives screen rotation

### 🎯 Single Responsibility

| Layer          | Responsibility                   |
| -------------- | -------------------------------- |
| **Screen**     | Display UI, handle clicks        |
| **ViewModel**  | Manage state, coordinate actions |
| **Repository** | Abstract data source             |
| **DAO**        | SQL queries                      |
| **Database**   | Store data                       |

Each layer has ONE job! This makes code easier to understand, test, and maintain.

---

## Quick Reference

### Read Data

```kotlin
// 1. Define query in DAO
@Query("SELECT * FROM clothes_table")
fun getAllClothes(): Flow<List<Clothes>>

// 2. Expose in ViewModel
val allClothes: StateFlow<List<Clothes>> =
    repository.allClothes.stateIn(...)

// 3. Observe in UI
val clothes by viewModel.allClothes.collectAsState()
```

### Write Data

```kotlin
// 1. Define operation in DAO
@Update suspend fun update(clothes: Clothes)

// 2. Wrap in ViewModel
fun update(clothes: Clothes) = viewModelScope.launch {
    repository.update(clothes)
}

// 3. Call from UI
Button(onClick = { viewModel.update(modifiedClothes) })
```

---

## Complete Picture

```mermaid
graph TB
    subgraph User["👤 User Interface"]
        U1["Sees outfit"]
        U2["Clicks confirm"]
    end

    subgraph UI["📱 Frontend (Compose)"]
        S1["FullOutfitScreen"]
        S2["collectAsState()"]
        S3["onConfirm callback"]
    end

    subgraph VM["🔄 ViewModel"]
        V1["StateFlow<br/>(read)"]
        V2["updateAll()<br/>(write)"]
    end

    subgraph Backend["💾 Backend"]
        B1["Repository"]
        B2["DAO"]
        B3["Room Database"]
    end

    U1 --> S1
    S1 --> S2
    S2 --> V1
    V1 --> B1
    B1 --> B2
    B2 --> B3

    U2 --> S3
    S3 --> V2
    V2 --> B1

    B3 -.->|"Auto-update"| B2
    B2 -.->|"Flow emits"| B1
    B1 -.->|"StateFlow"| V1
    V1 -.->|"Recompose"| S2
    S2 -.->|"New data"| U1

    style User fill:#e8f5e9
    style UI fill:#e1f5ff
    style VM fill:#f3e5f5
    style Backend fill:#fff3e0
```

---

## Summary

**Frontend (ui/)** = What user sees and interacts with  
**↕️ Communication** = Reactive Flows + Suspend functions  
**Backend (data/)** = Where data is stored and managed

**ViewModel** = The bridge that makes it all work together! 🌉

The result? A reactive, type-safe app where UI automatically updates when data changes! 🚀
