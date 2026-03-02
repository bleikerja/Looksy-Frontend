# Merge Resolution: Scaffold Pattern into FullOutfitScreen

**Branch**: `80-fulloutfitscreen-überarbeiten`  
**Merged from**: `76-bilder-von-kleidungsstücken-vor-dem-speichern-bearbeiten`  
**Date**: 2026-03-01

---

## Context

While branch `80` was developing the new carousel-based outfit layout for `FullOutfitScreen`, branch `76` introduced the app-wide `Scaffold + Header` convention (see `conventions.md` → _Screen Structure_). When merging `76` into `80`, 5 conflict regions appeared in `FullOutfitScreen.kt`. `NavGraph.kt` also had a brace imbalance introduced during the merge but no conflict markers.

---

## Goal

Combine both changes:

- **Keep**: the new carousel/grid layout from branch `80`
- **Adopt**: the `Scaffold + Header` screen structure from branch `76`

---

## Files Changed

### `ui/screens/FullOutfitScreen.kt`

All 5 conflict regions resolved:

| #   | Location                                             | Resolution                                                                                                                                                                                                                                                                                                                                           |
| --- | ---------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Root wrapper (~line 193)                             | Replaced the raw `Box(fillMaxSize)` root with `Scaffold { topBar = { Header(...) }, snackbarHost = { SnackbarHost(...) } }`. Kept HEAD's computed vars: `allWornItems`, `confirmedOutfit`, `gridOutfitValid` (needed by carousel logic). Discarded BRANCH's simpler `confirmedOutfit` recomputation.                                                 |
| 2   | Inline Header + WeatherIconRow (~line 244)           | Removed the inline `Header(modifier = Modifier.align(Alignment.Center))` block and its enclosing `Box(fillMaxWidth)` — the Header is now exclusively in the Scaffold `topBar`. `WeatherIconRow` stays as the first item in the body `Column`. Kept 100% of HEAD's carousel/grid layout below it. Discarded BRANCH's old `OutfitPart` stacked layout. |
| 3   | AlertDialog closing braces (~line 913)               | Kept HEAD's compact `}) { Text("Weiter") }` single-line style, discarding BRANCH's reformatted multi-line variant (identical logic).                                                                                                                                                                                                                 |
| 4   | SnackbarHost + empty state (~line 951)               | Removed the stray `SnackbarHost` inside the outer `Box` (now handled by `Scaffold snackbarHost` param). For the empty-wardrobe `else` branch, adopted BRANCH's `Scaffold { Header }` wrapper so it also follows the screen structure convention.                                                                                                     |
| 5   | Temperature display in `WeatherIconRow` (~line 1618) | Kept HEAD's live `Spacer + Text("${temperature}°C")` block. Discarded BRANCH's commented-out version.                                                                                                                                                                                                                                                |

**Additional fix**: removed one extra closing brace left over after the merge that caused the `else` branch to be at the wrong nesting depth.

### `ui/navigation/NavGraph.kt`

No conflict markers were present. Fixed a brace imbalance (extra `}` at the end of `composable(Routes.ChoseClothes.route)`) that was introduced during the merge and caused a `Syntax error: Expecting '}'` at the end of the file.

---

## Result

- `assembleDebug`: **PASS** (build completed successfully after fixes)
- Home screen renders `Header` ("Heutiges Outfit" + washing machine icon) in the top bar via `Scaffold`
- `WeatherIconRow` appears as the first body element below the header
- The full carousel/grid layout (state buttons, jacket column, 2/3/4-layer carousels, GRID mode) is preserved unchanged
- Empty wardrobe state also uses `Scaffold + Header`
- `SnackbarHost` is managed by `Scaffold`, not free-floating inside a `Box`

---

## Key Design Decisions

1. **One `Scaffold` per branch of `hasAnyClothes`** — both the populated and the empty-wardrobe paths have their own `Scaffold { Header }` wrapper.
2. **`snackbarHost` in `Scaffold` params** — avoids overlap with the bottom navigation bar and lets `Scaffold` handle insets correctly.
3. **No second `SnackbarHost` in the body** — the BRANCH had accidentally introduced a duplicate; removed.
4. **Temperature text kept active** — the BRANCH had temporarily commented it out; HEAD's live display was restored.
