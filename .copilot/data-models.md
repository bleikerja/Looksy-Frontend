# Data Models Reference

## Core Entity: Clothes

**File**: `dataClassClones/Clothes.kt`

```kotlin
@Entity(tableName = "clothes_table")
data class Clothes(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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

### Field Descriptions

- **id**: Auto-generated primary key, unique identifier
- **size**: Clothing size (numeric or letter-based)
- **seasonUsage**: When the item can be worn (Winter, Summer, inBetween)
- **type**: Category of clothing (Dress, Tops, Skirt, Pants, Jacket)
- **material**: Fabric type (Wool, Cotton, Polyester, etc.)
- **clean**: Boolean flag indicating if item is clean
- **washingNotes**: Care instructions (Temperature30, Hand, Dying, Dryer)
- **imagePath**: Local file path to item's photo (stored as String)
- **isSynced**: Boolean flag for future backend sync feature (currently unused)

## Enum Types

### Size

**File**: `dataClassClones/Size.kt`

```kotlin
enum class Size {
    _34, _36, _38, _40, _42, _44, _46, _48, _50, _52, _54, _56, _58, _60,
    _XS, _S, _M, _L, _XL
}
```

**Special Features:**

- Leading underscore for numeric values (Kotlin requirement)
- Letter sizes: `_XS`, `_S`, `_M`, `_L`, `_XL`
- Conversion method: `toLetterSize` property
  - Converts numeric sizes to letter equivalents
  - Example: `Size._40.toLetterSize` → `Size._M`
  - Throws `NoKnownSize` exception for unmapped sizes

**Usage:**

```kotlin
val size = Size._40
val letterSize = size.toLetterSize // Returns Size._M
```

### Season

**File**: `dataClassClones/Season.kt`

```kotlin
enum class Season {
    Winter, Summer, inBetween
}
```

**Values:**

- `Winter` - Cold weather clothing
- `Summer` - Warm weather clothing
- `inBetween` - Transitional/all-season items

### Type

**File**: `dataClassClones/Type.kt`

```kotlin
enum class Type {
    Dress, Tops, Skirt, Pants, Jacket
}
```

**Usage in Navigation:**

- Used for filtering clothes by category
- Passed as navigation argument: `Routes.SpecificCategory.createRoute(type.name)`
- Grouping key: `allClothes.groupBy { it.type }`

### Material

**File**: `dataClassClones/Material.kt`

```kotlin
enum class Material {
    Wool, Cotton, Polyester, cashmere, silk, linen, fur, jeans
}
```

**Note:** Inconsistent naming (most PascalCase, some lowercase)

- PascalCase: `Wool`, `Cotton`, `Polyester`
- lowercase: `cashmere`, `silk`, `linen`, `fur`, `jeans`

### WashingNotes

**File**: `dataClassClones/WashingNotes.kt`

```kotlin
enum class WashingNotes {
    Temperature30, Hand, Dying, Dryer
}
```

**Values:**

- `Temperature30` - Wash at 30°C
- `Hand` - Hand wash only
- `Dying` - Can be dyed (possibly meant "Drying"?)
- `Dryer` - Dryer safe

## Utility Classes

### Filter

**File**: `dataClassClones/Filter.kt`

**Purpose**: Utility class for filtering lists of `Clothes` objects

**Methods:**

```kotlin
class Filter {
    fun byType(type: Type, list: MutableList<Clothes>): MutableList<Clothes>
    fun bySeason(season: Season, list: MutableList<Clothes>): MutableList<Clothes>
    fun bySize(size: Size, list: MutableList<Clothes>): MutableList<Clothes>
    fun byMaterial(material: Material, list: MutableList<Clothes>): MutableList<Clothes>
    fun byCleanliness(clean: Boolean, list: MutableList<Clothes>): MutableList<Clothes>
}
```

**Usage Example:**

```kotlin
val filter = Filter()
val winterClothes = filter.bySeason(Season.Winter, allClothes)
val cleanWinterClothes = filter.byCleanliness(true, winterClothes)
```

**Note:**

- Currently returns new `MutableList` on each filter
- For better performance with Room, use DAO queries directly:
  ```kotlin
  @Query("SELECT * FROM clothes_table WHERE type = :type")
  fun getByType(type: Type): Flow<List<Clothes>>
  ```

## UI Helper Models

### Category

**File**: `CategoriesScreen.kt`

```kotlin
data class Category(val name: String, val iconRes: Int)
```

**Purpose**: UI model for category icons (not persisted)

**Usage:**

```kotlin
val sampleCategories = listOf(
    Category("Shirt", R.drawable.shirt_category),
    Category("Pants", R.drawable.pants_category)
)
```

### CategoryItems

**File**: `CategoriesScreen.kt`

```kotlin
data class CategoryItems(val categoryName: String, val items: List<Clothes>)
```

**Purpose**: Groups clothes by category name for display

**Creation Pattern:**

```kotlin
val categoryItems = allClothesFromDb
    .groupBy { it.type }
    .map { (type, items) ->
        CategoryItems(categoryName = type.name, items = items)
    }
```

## Type Conversions

All enums use the same conversion pattern via `Converters.kt`:

```kotlin
// To String (for database storage)
@TypeConverter
fun fromEnumType(value: EnumType): String = value.name

// From String (from database)
@TypeConverter
fun toEnumType(value: String): EnumType = EnumType.valueOf(value)
```

**Registered in Database:**

```kotlin
@TypeConverters(Converters::class)
abstract class ClothesDatabase : RoomDatabase()
```

## Data Validation

### Form Validation Pattern

**File**: `screens/ScreenAddNewClothes.kt`

```kotlin
val isFormValid = size != null &&
                  season != null &&
                  type != null &&
                  material != null &&
                  washingNotes != null

Button(enabled = isFormValid) { ... }
```

All enum fields are required (non-nullable in entity).

## Sample Data

**Location**: `ClothInformationScreen.kt` (for previews)

```kotlin
val allClothes = listOf(
    Clothes(
        size = Size._46,
        seasonUsage = Season.Winter,
        type = Type.Pants,
        material = Material.Wool,
        clean = true,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}"
    )
)
```

## Future Enhancements

**Potential additions to `Clothes` entity:**

1. **Brand**: String field for clothing brand
2. **Color**: Enum or String for primary color
3. **PurchaseDate**: Date field
4. **Price**: Double field
5. **TimesWorn**: Int counter
6. **LastWorn**: Date field
7. **Notes**: String for additional notes
8. **Tags**: List<String> for custom categorization

**Potential new enums:**

- `Color` - Primary colors
- `Pattern` - Solid, Striped, Floral, etc.
- `Fit` - Slim, Regular, Loose, etc.
