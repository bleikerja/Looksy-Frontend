# FullOutfitScreen Carousel Redesign

## Overview

Redesigned the `FullOutfitScreen` (Home screen) from a simple vertical list of outfit parts to a **three-column carousel-based layout** with horizontal and vertical pagers, mode toggles, and separated TShirt/Pullover slots.

## Layout

```
┌──────────┬──────────────────────┬──────────┐
│ Jacket   │  TShirt (horizontal) │ Pullover │
│ (vertical│  ───────────────────  │ (vertical│
│  carousel)│  Hose | Rock toggle  │  carousel)│
│          │  Bottom  (horizontal)│          │
│          │  ───────────────────  │          │
│          │  Shoes  (horizontal) │          │
└──────────┴──────────────────────┴──────────┘
```

- **Center column**: 3 horizontal carousels (TShirt + Bottom + Shoes) or 2 (Dress + Shoes)
- **Left column**: Jacket vertical carousel (shown when jacket items exist)
- **Right column**: Pullover vertical carousel (shown when pullover items exist)
- **Mode toggle**: "Oberteil+Unterteil" vs "Kleid" switches center column layout
- **Bottom toggle**: "Hose" | "Rock" tab switches between pants and skirt carousels

Each carousel shows the selected item centered with **dimmed quarter-peek previews** of adjacent items and **arrow buttons** for navigation.

## Files Changed

### Model & Database

| File                                     | Change                                                              |
| ---------------------------------------- | ------------------------------------------------------------------- |
| `data/model/Outfit.kt`                   | Added `pulloverId: Int? = null` field                               |
| `data/local/database/ClothesDatabase.kt` | Bumped version 6 → 7                                                |
| `data/local/dao/OutfitDao.kt`            | Added `pulloverId` to `findMatchingOutfit` query                    |
| `data/repository/OutfitRepository.kt`    | Added `selectedPulloverId` parameter to `incrementOutfitPreference` |
| `ui/viewmodel/OutfitViewModel.kt`        | Added `selectedPulloverId` parameter forwarding                     |

### Outfit Generation & Scoring

| File                                    | Change                                                                                                     |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `util/OutfitGenerator.kt`               | `OutfitResult` gets `pullover` field; generation separates TShirt vs Pullover selection; dress clears both |
| `util/OutfitCompatibilityCalculator.kt` | All item lists and validation rules include `outfit.pullover`                                              |

### UI

| File                                | Change                                                                                                                                                                                                                                                                                                                                                                                                                                |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ui/screens/FullOutfitScreen.kt`    | **Complete rewrite** (~995 lines). New composables: `ModeToggleButton`, `HorizontalClothesCarousel`, `VerticalClothesCarousel`, `CarouselItemCard`, `CarouselArrowButton`. Uses `HorizontalPager`/`VerticalPager` from Compose Foundation. New parameter API: `allClothes`, `selectedTshirtId`, `selectedPantsId`, `selectedSkirtId`, `selectedDressId`, `selectedJacketId`, `selectedPulloverId`, `selectedShoesId`, `onSlotChanged` |
| `ui/screens/OutfitDetailsScreen.kt` | Added `outfitPullover` parameter; added private `OutfitPart` composable (moved from FullOutfitScreen)                                                                                                                                                                                                                                                                                                                                 |
| `ui/navigation/NavGraph.kt`         | Added `pulloverId` state var; updated `LaunchedEffect` initialization; rewired `FullOutfitScreen` call with new API and `onSlotChanged` callback; split `TShirt/Pullover` handling in `onConfirmOutfit`, `onDeselectOutfit`, `EditClothes`, `onWear` handlers; mutual exclusion logic for Dress↔Top/Bottom                                                                                                                            |

### Tests

| File                                   | Change                                                                                                  |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `FullOutfitScreenWeatherTest.kt`       | Migrated 11 test calls from old `top=`/`pants=` API to new `allClothes=`/`selectedTshirtId=` API        |
| `OutfitCompatibilityCalculatorTest.kt` | Fixed `OutfitResult` positional args for `shoes`; updated pullover test to use `pullover =` named param |
| `OutfitGeneratorTest.kt`               | Updated pullover assertion to check `result.pullover`; broadened top-or-dress check                     |
| `OutfitRepositoryTest.kt`              | Fixed `findMatchingOutfit` mock to 7 args                                                               |

## Key Design Decisions

1. **Pullover as separate slot** — TShirt and Pullover are independent clothing categories with their own carousel columns, not merged under a single "top" slot.
2. **Mutual exclusion** — Dress selection clears TShirt, Pullover, Pants, and Skirt. Pants/Skirt are toggleable alternatives.
3. **Conditional side columns** — Jacket and Pullover columns only render when items of that type exist in the wardrobe.
4. **DB migration** — Uses `fallbackToDestructiveMigration()` (version 6 → 7), no migration code needed.
5. **No new dependencies** — `HorizontalPager`/`VerticalPager` come from `androidx.compose.foundation.pager`, already available via the Compose BOM.

## Build Status

- `assembleDebug`: **PASS**
- `test` (JVM unit tests): **PASS**
