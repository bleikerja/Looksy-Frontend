# FullOutfitScreen Carousel Redesign

---

## Phase 1 — Initial Carousel Layout

### Overview

Redesigned the `FullOutfitScreen` (Home screen) from a simple vertical list of outfit parts to a **three-column carousel-based layout** with horizontal and vertical pagers, mode toggles, and separated TShirt/Pullover slots.

### Layout

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

### Files Changed

#### Model & Database

| File                                     | Change                                                              |
| ---------------------------------------- | ------------------------------------------------------------------- |
| `data/model/Outfit.kt`                   | Added `pulloverId: Int? = null` field                               |
| `data/local/database/ClothesDatabase.kt` | Bumped version 6 → 7                                                |
| `data/local/dao/OutfitDao.kt`            | Added `pulloverId` to `findMatchingOutfit` query                    |
| `data/repository/OutfitRepository.kt`    | Added `selectedPulloverId` parameter to `incrementOutfitPreference` |
| `ui/viewmodel/OutfitViewModel.kt`        | Added `selectedPulloverId` parameter forwarding                     |

#### Outfit Generation & Scoring

| File                                    | Change                                                                                                     |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `util/OutfitGenerator.kt`               | `OutfitResult` gets `pullover` field; generation separates TShirt vs Pullover selection; dress clears both |
| `util/OutfitCompatibilityCalculator.kt` | All item lists and validation rules include `outfit.pullover`                                              |

#### UI

| File                                | Change                                                                                                                                                                                                                                                                                                                                                                                                                                |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ui/screens/FullOutfitScreen.kt`    | **Complete rewrite** (~995 lines). New composables: `ModeToggleButton`, `HorizontalClothesCarousel`, `VerticalClothesCarousel`, `CarouselItemCard`, `CarouselArrowButton`. Uses `HorizontalPager`/`VerticalPager` from Compose Foundation. New parameter API: `allClothes`, `selectedTshirtId`, `selectedPantsId`, `selectedSkirtId`, `selectedDressId`, `selectedJacketId`, `selectedPulloverId`, `selectedShoesId`, `onSlotChanged` |
| `ui/screens/OutfitDetailsScreen.kt` | Added `outfitPullover` parameter; added private `OutfitPart` composable (moved from FullOutfitScreen)                                                                                                                                                                                                                                                                                                                                 |
| `ui/navigation/NavGraph.kt`         | Added `pulloverId` state var; updated `LaunchedEffect` initialization; rewired `FullOutfitScreen` call with new API and `onSlotChanged` callback; split `TShirt/Pullover` handling in `onConfirmOutfit`, `onDeselectOutfit`, `EditClothes`, `onWear` handlers; mutual exclusion logic for Dress↔Top/Bottom                                                                                                                            |

#### Tests

| File                                   | Change                                                                                                  |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `FullOutfitScreenWeatherTest.kt`       | Migrated 11 test calls from old `top=`/`pants=` API to new `allClothes=`/`selectedTshirtId=` API        |
| `OutfitCompatibilityCalculatorTest.kt` | Fixed `OutfitResult` positional args for `shoes`; updated pullover test to use `pullover =` named param |
| `OutfitGeneratorTest.kt`               | Updated pullover assertion to check `result.pullover`; broadened top-or-dress check                     |
| `OutfitRepositoryTest.kt`              | Fixed `findMatchingOutfit` mock to 7 args                                                               |

### Key Design Decisions

1. **Pullover as separate slot** — TShirt and Pullover are independent clothing categories with their own carousel columns, not merged under a single "top" slot.
2. **Mutual exclusion** — Dress selection clears TShirt, Pullover, Pants, and Skirt. Pants/Skirt are toggleable alternatives.
3. **Conditional side columns** — Jacket and Pullover columns only render when items of that type exist in the wardrobe.
4. **DB migration** — Uses `fallbackToDestructiveMigration()` (version 6 → 7), no migration code needed.
5. **No new dependencies** — `HorizontalPager`/`VerticalPager` come from `androidx.compose.foundation.pager`, already available via the Compose BOM.

### Build Status

- `assembleDebug`: **PASS**
- `test` (JVM unit tests): **PASS**

---

## Phase 2 — State-Based Layout, Merged Carousels & Image Sizing

### Overview

Replaced the text toggle buttons with **3 brick-icon state-selector buttons** representing 2, 3, or 4 outfit layers. Each state shows a different combination of carousels in the center column. The separate Pullover vertical side column was removed; pullovers now appear inside the center-column carousels. All action buttons were moved from overlay-positioned floating icons into a single in-flow `Row` at the bottom. Image whitespace was reduced.

### New Layout (state 3 — default)

```
┌──────────┬───────────────────────────────────┐
│ Jacket   │  Merged Top (TShirt+Pullover)      │
│ (vertical│  ─────────────────────────────     │
│ carousel)│  Merged Bottom (Pants+Skirt)        │
│          │  ─────────────────────────────     │
│          │  Shoes                             │
│          ├───────────────────────────────────┤
│          │ [▤][▦][▩]  Spacer  [⟳][🔖][✓]    │
└──────────┴───────────────────────────────────┘
```

- **State 2 (2 bricks)**: Dress + Shoes — full-body outfit mode
- **State 3 (3 bricks)** _(default)_: Merged Top (TShirts+Pullovers shuffled) + Merged Bottom (Pants+Skirts shuffled) + Shoes
- **State 4 (4 bricks)**: TShirt carousel + Pullover carousel + Merged Bottom + Shoes
- **Left column**: Jacket vertical carousel — preserved across all states

### Files Changed

| File                             | Change                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| -------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ui/screens/FullOutfitScreen.kt` | Removed `ModeToggleButton`, `isDressMode`, `showPants`. Added `LayoutState` enum (`TWO_LAYERS` / `THREE_LAYERS` / `FOUR_LAYERS`). Added `mergedTopItems` and `mergedBottomItems` (shuffled on `cleanClothes` key). Added `BrickIcon` (Canvas-drawn stacked rects) and `StateButton` composables. Removed right pullover vertical column. Restructured center column as `when (layoutState)`. Moved all action buttons into a single in-flow `Row`. Fixed `CarouselItemCard` to remove excess whitespace. |

### Key Design Decisions

1. **`LayoutState` enum drives the center column** — a single `when` block replaces the previous `isDressMode` boolean and `showPants` toggle. Default is `THREE_LAYERS`.
2. **Merged carousel virtual slot** — when swiping through the merged-top or merged-bottom carousel, `onSlotChanged` fires with the item's actual `Type` and simultaneously clears the sibling type's slot (e.g. selecting a Pullover clears TShirt). `NavGraph` is unchanged.
3. **Merged lists are shuffled once per `cleanClothes` change** — `remember(cleanClothes) { (a + b).shuffled() }` keeps the order stable during recomposition but refreshes when the wardrobe changes.
4. **Pullover column removed** — Pullovers appear in center-column carousels (merged in state 3, separate in state 4); no right-side vertical carousel.
5. **State transition slot clearing** — switching to `TWO_LAYERS` clears all top/bottom slots; switching away from `TWO_LAYERS` clears the dress slot; switching `FOUR_LAYERS → THREE_LAYERS` with both TShirt and Pullover selected drops Pullover (TShirt priority).
6. **Brick icons via Canvas** — `BrickIcon(n)` draws `n` stacked rounded rectangles with a 2 dp gap using `drawRoundRect`. No external assets needed.
7. **Image whitespace fix** — `CarouselItemCard` outer `Box` is now transparent; the white rounded-rect background is applied directly to `AsyncImage`. This eliminates the visible white space above/below images that don't fill the container height.
8. **No model/DB/NavGraph changes** — only `FullOutfitScreen.kt` was modified.

### Build Status

- `assembleDebug`: **PASS**
- `test` (JVM unit tests): **PASS**

---

## Phase 3 — Jacket Toggle Button

### Overview

Added a **jacket toggle button** to the bottom action row that controls visibility of the left-side jacket vertical carousel. When active, the jacket column is shown and the jacket slot participates in the outfit. When deactivated, the jacket carousel is hidden and the jacket slot is cleared.

### New Layout (bottom action row)

```
[ J ] | [ ▤ ][ ▦ ][ ▩ ]        Spacer        [ 🔀 ] [ 🔖 ] [ ✓ / ⟳ ]
 └─ jacket toggle + vertical divider + layer-state buttons ─┘
```

### Files Changed

| File                             | Change                                                                                                                                                                                                                                                                                                                                         |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ui/screens/FullOutfitScreen.kt` | Added `showJacket` state var; added `LaunchedEffect(jacketItems)` to auto-disable when wardrobe has no jackets; gated jacket `VerticalClothesCarousel` and `centerWeight` on `showJacket`; added `JacketBrickButton` composable; restructured bottom action row to wrap jacket button + divider + `StateButton` row in a single grouped `Row`. |

### Key Design Decisions

1. **Visual language consistency** — `JacketBrickButton` uses the same `Canvas`-drawn approach as `BrickIcon`/`StateButton`: a single tall portrait-oriented rounded rect (42 % of button width, full height) that mirrors the "vertical brick" metaphor of the jacket column.
2. **Grouped with vertical divider** — the jacket button sits immediately left of the layer-state buttons, separated by a 1 dp `outlineVariant`-coloured vertical line. Both groups are wrapped in one `Row` and treated as a visual unit.
3. **Auto-disable when no jackets** — a `LaunchedEffect` watches `jacketItems`; if it becomes empty the toggle is set to `false` and `onSlotChanged(Type.Jacket, null)` is called so `NavGraph` stays consistent.
4. **Initial state follows wardrobe** — `showJacket` initialises to `jacketItems.isNotEmpty()`, matching the previous implicit behaviour where the column was shown whenever jackets existed.
5. **`centerWeight` respects toggle** — the center column expands to full width (`1f`) whenever `!showJacket || jacketItems.isEmpty()`, exactly as before when there were no jacket items.
6. **No model/DB/NavGraph changes** — only `FullOutfitScreen.kt` was modified.

### Build Status

- `assembleDebug`: **PASS**

---

## Phase 4 — GRID Super Mode

### Overview

Added a fourth layout state, **GRID**, that displays all 7 clothing categories simultaneously in a fullscreen 4×2 grid of independent horizontal carousels. Each carousel can be swiped all the way left to a "Keine Auswahl" (no selection) placeholder, allowing the user to opt out of any category. The GRID mode is mutually exclusive with the jacket toggle and layer-count buttons — activating GRID deactivates all of those, and vice versa. Save/Confirm are disabled in GRID mode until the outfit meets the minimum validity rule.

### New Layout (GRID mode)

```
┌──────────────────┬──────────────────┐
│  Jacke           │  Pullover/SW     │  row 0 (weight 1)
├──────────────────┼──────────────────┤
│  T-Shirt/LS      │                  │  row 1 (weight 1)
├──────────────────┤  Kleid           │
│  Hose            │  (weight 2)      │  row 2 (weight 1)
├──────────────────┼──────────────────┤
│  Rock            │  Schuhe          │  row 3 (weight 1)
└──────────────────┴──────────────────┘
         ↓ bottom action row ↓
[ 🔀 ]  [J|disabled] | [▤▤][▦▦][▩▩][disabled]  |  [⊞]  [ 🔖 ][ ✓/⟳ ]
                                                    └─ Grid button (active)
```

- **Left column (weight 1)**: Jacket → T-Shirt/Longsleeve → Trousers → Skirt
- **Right column (weight 1)**: Pullover/Sweatshirt → Dress (2× row weight) → Shoes
- **All carousels**: include a "Keine Auswahl" page as page 0 (swipe left from first item)
- **No jacket side column** — the jacket is part of the grid; the side column is suppressed

### Files Changed

| File                             | Change                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| -------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ui/screens/FullOutfitScreen.kt` | Added `GRID` to `LayoutState` enum. Added `onGridModeChanged: (Boolean) -> Unit` parameter. Added `gridOutfitValid` computed variable. Added `allowNone: Boolean = false` parameter to `HorizontalClothesCarousel` — when `true`, prepends a page-0 "Keine Auswahl" placeholder and reports `null` via `onItemSelected`. Added `NoneSelectionCard` composable (dashed-border rounded rect). Added `GridBrickIcon` composable (Canvas 4×2 grid of bricks). Added `GridModeButton` composable. Added `enabled` parameter to `StateButton`. Modified bottom action row: jacket + layer-count buttons are disabled in GRID mode; grid button sits right of a second divider; shuffle exits GRID before firing. Modified `LaunchedEffect(selectedDressId)` to skip auto-TWO_LAYERS when already in GRID. Added new imports: `PathEffect`, `drawscope.Stroke`. |
| `ui/navigation/NavGraph.kt`      | Added `isGridMode` state var. Modified `onSlotChanged` — the Dress↔Top/Bottom and Pants↔Skirt mutual exclusion is skipped when `isGridMode` is `true`. Wired `onGridModeChanged = { isGridMode = it }` in `FullOutfitScreen` call.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |

### `allowNone` Carousel Behaviour

| Condition                               | Page 0                                           | Pages 1..N                         |
| --------------------------------------- | ------------------------------------------------ | ---------------------------------- |
| `allowNone = true`, `selectedId = null` | pager syncs to page 0, `NoneSelectionCard` shown | clothing items                     |
| `allowNone = true`, `selectedId = X`    | `NoneSelectionCard` (dimmed if not current)      | item with `id == X` synced to view |
| user swipes to page 0                   | `onItemSelected(null)` fired                     | —                                  |
| `allowNone = false` (default)           | unchanged from previous behaviour                | unchanged                          |

### Outfit Validity in GRID Mode

```
gridOutfitValid = currentDress != null
              || ((currentTop != null || currentPullover != null)
                  && (currentPants != null || currentSkirt != null))
```

Save (bookmark) and Confirm (check) buttons render at 30 % alpha and block clicks when `isGridMode && !gridOutfitValid`. No shoes or jacket required.

### State Transition Rules

| Action                                         | Result                                                                                                        |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| Tap Grid button (off → on)                     | `layoutState = GRID`; jacket + layer buttons disabled; `onGridModeChanged(true)`                              |
| Tap Grid button (on → off)                     | `layoutState = THREE_LAYERS`; if dress + top were both selected, dress is cleared; `onGridModeChanged(false)` |
| Tap Jacket or layer-count button while in GRID | no-op (buttons disabled, 30 % alpha)                                                                          |
| Tap Shuffle while in GRID                      | `layoutState = THREE_LAYERS`, `onGridModeChanged(false)`, then `onGenerateRandom()`                           |
| External dress selection (random gen)          | auto-switch to `TWO_LAYERS` is **skipped** when already in GRID                                               |

### Key Design Decisions

1. **GRID is a fourth orthogonal mode** — it replaces the jacket-side-column + center-column layout entirely with a uniform 2-column grid. The jacket toggle and layer-count buttons are visually disabled rather than hidden to preserve spatial memory.
2. **No mutual exclusion in GRID mode** — the user can simultaneously have a dress, a T-Shirt, trousers, and a skirt in the grid. `NavGraph.onSlotChanged` skips its Dress↔Top/Bottom and Pants↔Skirt cleanup when `isGridMode` is `true`.
3. **"Keine Auswahl" as page 0** — implemented as `allowNone` on the existing `HorizontalClothesCarousel` rather than a separate component, keeping the pager abstraction intact. No-selection is the first swipe-left position, matching native list-start UX.
4. **Dress spans 2 rows** — achieved with `Modifier.weight(2f)` on the dress carousel inside the right column, giving it twice the vertical space of single-category rows to accommodate portrait dress images.
5. **Grid button icon** — `GridBrickIcon` draws a 4-row × 2-column grid of rounded rectangles in a 24 dp Canvas, using the same 2 dp gap and 3 dp corner radius as `BrickIcon`, maintaining visual consistency.
6. **`onGridModeChanged` callback** — instead of lifting `layoutState` to `NavGraph`, a lightweight boolean callback is used so `NavGraph` only needs to know about the GRID/non-GRID distinction for mutual exclusion, keeping all layout logic inside the composable.
7. **No model/DB changes** — GRID mode uses the same slot IDs and `Outfit` entity as the other modes.

### Build Status

- `assembleDebug`: **PASS**
- `test` (JVM unit tests): **PASS**
