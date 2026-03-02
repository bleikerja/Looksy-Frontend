# Data Models Reference

## Core Entities

### Clothes (`clothes_table`)

**File**: `app/src/main/java/com/example/looksy/data/model/Clothes.kt`

```kotlin
@Entity(tableName = "clothes_table")
data class Clothes(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: Size,
    val seasonUsage: Season,
    val type: Type,
    val material: Material,
    val color: ClothesColor? = null,       // nullable — unknown/unset
    val wornSince: Long? = null,           // epoch ms
    val lastWorn: Long? = null,            // epoch ms
    val daysWorn: Int = 0,
    val clean: Boolean,
    val washingNotes: List<WashingNotes>,  // stored as JSON via Gson
    val selected: Boolean = false,         // active in current outfit
    val imagePath: String = "",            // absolute path in filesDir/images/
    val isSynced: Boolean = false,         // reserved for future backend sync
    val wornClothes: Int = 0              // outfit-generation weight
)
```

**Key field notes:**
- **`selected`**: marks which item occupies a slot in the current outfit (set by generator / manual slot swap).
- **`wornClothes`**: incremented on outfit confirmation; used to weight random selection (`wornClothes + 1`).
- **`clean`**: gates outfit generation — only clean items enter `OutfitGenerator`.
- **`imagePath`**: set by `saveImagePermanently()` (`util/ImageStorage.kt`).

---

### Outfit (`outfits_table`)

**File**: `app/src/main/java/com/example/looksy/data/model/Outfit.kt`

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
    val preference: Int = 0,              // weight for saved-outfit reuse
    val isSynced: Boolean = false,
    val isManuelSaved: Boolean = false    // true when saved by user manually
)
```

All slot IDs are nullable. An outfit always uses **either** `topsId` **or** `dressId`; other slots are optional. `preference` is weighted in the 30% saved-outfit path of `OutfitGenerator`.

---

## Enum Types

### Type

**File**: `data/model/Type.kt`

```kotlin
enum class Type(val displayName: String) {
    Dress("Kleid"), TShirt("T-Shirt/Longsleeve"), Pullover("Pullover/Sweatshirt"),
    Skirt("Rock"), Pants("Hose"), Jacket("Jacke"), Shoes("Schuhe");

    companion object {
        /** Types that fill the "top" slot in an outfit. */
        val topTypes: Set<Type> = setOf(TShirt, Pullover)
    }
}
```

`Type.topTypes` is used in `OutfitGenerator`, `NavGraph` slot resolution, and `ClothesDao.getByType()`.

---

### Season

```kotlin
enum class Season(val displayName: String) {
    Winter("Winter"), Summer("Sommer"), inBetween("Übergang")
}
```

---

### ClothesColor

**File**: `data/model/ClothesColor.kt`

```kotlin
enum class ClothesColor(val displayName: String, val kind: Kind) {
    // NEUTRAL — always compatible
    Black("Schwarz", Kind.NEUTRAL), White("Weiss", Kind.NEUTRAL),
    Grey("Grau", Kind.NEUTRAL), Navy("Navy", Kind.NEUTRAL),
    // EARTH — compatible with anything
    Beige("Beige", Kind.EARTH), Brown("Braun", Kind.EARTH), Olive("Olivegruen", Kind.EARTH),
    // ACCENT — 3+ different ACCENT colors disqualify the outfit (score → 0)
    Blue("Blau", Kind.ACCENT), LightBlue("Hellblau", Kind.ACCENT),
    Green("Gruen", Kind.ACCENT), Red("Rot", Kind.ACCENT),
    Burgundy("Burgunderrot", Kind.ACCENT), Pink("Pink/Rosa", Kind.ACCENT),
    Purple("Lila/Violett", Kind.ACCENT), Yellow("Gelb", Kind.ACCENT), Orange("Orange", Kind.ACCENT);

    enum class Kind { NEUTRAL, EARTH, ACCENT }
}
```

`OutfitCompatibilityCalculator.isOutfitColorCompatible()` rejects outfits with ≥3 distinct ACCENT colors.

---

### Size

```kotlin
enum class Size { _34, _36, _38, _40, _42, _44, _46, _48, _50, _52, _54, _56, _58, _60, _XS, _S, _M, _L, _XL }
```

Leading underscore required for Kotlin enum identifiers starting with a digit.

---

### Material

Enum with `displayName` property covering common fabrics (Wool, Cotton, Polyester, etc.).

---

### WashingNotes

```kotlin
enum class WashingNotes { Temperature30, Hand, Dying, Dryer }
```

Stored as `List<WashingNotes>` in `Clothes.washingNotes`. `Converters.kt` uses `Gson` for JSON serialization and falls back gracefully for legacy single-value strings.

---

## Type Converters (`Converters.kt`)

**File**: `app/src/main/java/com/example/looksy/data/local/database/Converters.kt`

Pattern for simple enums:
```kotlin
@TypeConverter fun fromType(type: Type): String = type.name
@TypeConverter fun toType(s: String): Type = Type.valueOf(s)
```

`List<WashingNotes>` uses Gson:
```kotlin
@TypeConverter fun fromWashingNotesList(v: List<WashingNotes>): String = Gson().toJson(v)
@TypeConverter fun toWashingNotesList(v: String): List<WashingNotes> { /* Gson + fallback */ }
```

**Rule**: Add a `@TypeConverter` pair for every new enum-typed or collection-typed field, then bump `ClothesDatabase.version`.

---

## UI Helper Data Classes

### OutfitResult (`util/OutfitGenerator.kt`)

```kotlin
data class OutfitResult(val top: Clothes?, val pants: Clothes?, val skirt: Clothes?,
                        val jacket: Clothes?, val dress: Clothes?, val shoes: Clothes? = null)
```

Transient — never persisted. Passed between `OutfitGenerator` and `OutfitCompatibilityCalculator`.

### CategoryItems (`ui/screens/CategoriesScreen.kt`)

```kotlin
data class CategoryItems(val category: Type, val items: List<Clothes>)
```

Built in `NavGraph`:
```kotlin
allClothesFromDb.filter { it.clean }.groupBy { it.type }.map { (type, items) -> CategoryItems(type, items) }
```

---

## Database Version History

| Version | Notes |
|---------|-------|
| 6       | Current — production schema (Feb 2026) |
| < 6     | Earlier iterations (destructive migration clears data) |

`fallbackToDestructiveMigration()` is **active** — all local data is lost on version bump.
