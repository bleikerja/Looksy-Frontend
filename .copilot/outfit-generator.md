# Outfit Generator — Layout-Aware Implementation

## Overview� Layout-Aware Implementation

## Overview

The generator lives in `util/OutfitGenerator.kt` and exposes two public functions:

| Function                                                          | Purpose                                                                       |
| ----------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| `generateSingleRandomOutfit(allClothes, allOutfits, layoutMode)` | Produces one random candidate respecting the active layout                    |
| `generateRandomOutfit(allClothes, allOutfits, layoutMode)`        | Runs up to 80 candidates, returns the one with the highest compatibility score |

Both accept `layoutMode: OutfitLayoutMode = OutfitLayoutMode.THREE_LAYERS` (backward-compatible default) and return an `OutfitResult`:

```
OutfitResult(
  top:      Clothes?,   // TShirt
  pullover: Clothes?,
  pants:    Clothes?,
  skirt:    Clothes?,
  jacket:   Clothes?,
  dress:    Clothes?,
  shoes:    Clothes?
)
```

---

## Algorithm � `generateSingleRandomOutfit`

### Step 1 � Saved outfit shortcut (30 % chance)

- Filters saved `Outfit` rows so (a) `outfit.layoutMode == layoutMode` **and** (b) every referenced clothes ID is clean.
- Layout filtering prevents a `FOUR_LAYERS` outfit (with both top + pullover) from being offered while the user is in `TWO_LAYERS` mode, and vice-versa.
- Weights by `preference` (popular outfits more likely).
- Returns the saved outfit if its compatibility score > 0; otherwise falls through to fresh random generation.

### Step 2 � Per-layout dispatch

Generation is split into four private helpers selected by `layoutMode`:

```kotlin
return when (layoutMode) {
    TWO_LAYERS   -> generateTwoLayers(finalClothes)
    THREE_LAYERS -> generateThreeLayers(finalClothes)
    FOUR_LAYERS  -> generateFourLayers(finalClothes)
    GRID         -> generateGrid(finalClothes)
}
```

### Step 3 � Clothes weighting (applied before step 1)

`finalClothes` is the pool passed to all helpers:

- 70 % probability: list weighted by `wornClothes + 1` (less-worn items appear more often).
- 30 % probability: flat list of clean clothes.

---

## Per-Layout Generation Rules

| `layoutMode`     | top                          | pullover                     | dress            | pants / skirt                     | jacket   | shoes    |
| ---------------- | ---------------------------- | ---------------------------- | ---------------- | --------------------------------- | -------- | -------- |
| **TWO_LAYERS**   | ? forced null               | ? forced null               | ? must have one | ? forced null                    | optional | optional |
| **THREE_LAYERS** | XOR (one of top / pullover)  | XOR                          | ? forced null   | one of pants / skirt              | optional | optional |
| **FOUR_LAYERS**  | ? independent TShirt pick   | ? independent Pullover pick | ? forced null   | one of pants / skirt              | optional | optional |
| **GRID**         | optional                     | optional                     | optional         | independent (no mutual exclusion) | optional | optional |

### `generateTwoLayers`

Forces a Dress. All top / pullover / pants / skirt slots are set to `null`. Jacket and shoes are optional.

### `generateThreeLayers`

XOR pick: if both TShirt and Pullover lists are non-empty, one is chosen at random (50/50); if only one type exists, that one is used. Dress is always `null`. Pants and skirt are mutually exclusive (one dropped at random if both roll non-null).

### `generateFourLayers`

**Both** TShirt and Pullover are selected independently from their respective lists � the key difference from `THREE_LAYERS`. If a type is absent from the wardrobe the slot stays `null` gracefully. Dress is always `null`. Pants / skirt mutual exclusion still applies.

### `generateGrid`

Original layout-agnostic logic preserved. `searchForTops = [true, false].random()` coin flip; dress still excludes tops/bottoms; pants and skirt remain mutually exclusive. All other slots are free.

---

## How `generateRandomOutfit` Works

1. Calls `generateSingleRandomOutfit` 80 times (passing `layoutMode` through).
2. Keeps only candidates with compatibility score > 0 (color-compatible).
3. Returns the highest-scoring candidate; if none are compatible, returns a random candidate anyway.

---

## The Four Layout Modes and Their Active Slots

| Layout           | Center carousels                                                   | Side              | Notes                                               |
| ---------------- | ------------------------------------------------------------------ | ----------------- | --------------------------------------------------- |
| **TWO_LAYERS**   | Dress � Shoes                                                      | Jacket (optional) | Top/Pullover/Pants/Skirt hidden                     |
| **THREE_LAYERS** | merged Top (TShirt+Pullover) � merged Bottom (Pants/Skirt) � Shoes | Jacket (optional) | One top slot; bottom is Pants **or** Skirt          |
| **FOUR_LAYERS**  | TShirt � Pullover � merged Bottom � Shoes                          | Jacket (optional) | Both top slots shown & **independently selectable** |
| **GRID**         | All 7 slots as 4�2 grid                                            | �                 | No mutual exclusion enforced by UI                  |

The active `layoutState` lives inside `FullOutfitScreen` (local `var layoutState`); NavGraph tracks it in `currentLayoutMode`.

---

## NavGraph Integration

Both `generateRandomOutfit` call sites in `NavGraph.kt` pass `currentLayoutMode`:

```kotlin
// Initial generation in LaunchedEffect(allClothesFromDb)
val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb, currentLayoutMode)

// onGenerateRandom lambda (shuffle button)
val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb, currentLayoutMode)
```

---

## Shuffle Button (`FullOutfitScreen`)

The shuffle button no longer forces an exit from GRID mode before calling `onGenerateRandom`. Users can shuffle freely in any layout; the generator produces results appropriate for the active mode.

```kotlin
IconButton(onClick = {
    // Generator now respects the current layout mode, including GRID
    onGenerateRandom()
})
```

---

## Compatibility Score Impact

`OutfitCompatibilityCalculator.calculateCompatibilityScore` handles all `OutfitResult` combinations correctly (including both top + pullover together). No changes were needed there.

`validateOutfitRules` requires `top != null || pullover != null || dress != null`, which is satisfied by all modes. `FOUR_LAYERS` (both top + pullover) passes because the rule checks _at least one_ is present.

---

## Test Coverage

`OutfitGeneratorTest` contains 18 tests (0 failures). Layout-aware tests added:

| Test | Assertion |
| ---- | --------- |
| `FOUR_LAYERS generates both TShirt and Pullover independently` | = 160/200 runs have both `top != null` and `pullover != null` |
| `TWO_LAYERS always generates a dress and never top or bottom` | 200/200 runs: `dress != null`, `top/pullover/pants/skirt == null` |
| `TWO_LAYERS never uses a saved outfit from a different layout mode` | 500 runs in TWO_LAYERS with a THREE_LAYERS saved outfit ? `top` always `null` |
| `THREE_LAYERS never generates a dress even when one exists` | 200/200 runs: `dress == null` |

---

## Files Changed

| File                             | Change                                                                                                                                                                 |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `util/OutfitGenerator.kt`        | Added `layoutMode` param to both functions; split generation into `generateTwoLayers`, `generateThreeLayers`, `generateFourLayers`, `generateGrid`; saved-outfit shortcut filters by `layoutMode` |
| `ui/navigation/NavGraph.kt`      | Both `generateRandomOutfit` calls now pass `currentLayoutMode`                                                                                                         |
| `ui/screens/FullOutfitScreen.kt` | Removed forced `layoutState = THREE_LAYERS` reset from shuffle button `onClick`                                                                                        |
| `test/OutfitGeneratorTest.kt`    | Added 4 layout-aware test cases; added `OutfitLayoutMode` import                                                                                                       |
