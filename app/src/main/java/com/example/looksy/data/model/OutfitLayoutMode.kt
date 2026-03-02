package com.example.looksy.data.model

/**
 * Describes the visual layout of an outfit, mirroring the FullOutfitScreen layout structure.
 *
 * - [TWO_LAYERS]: Dress + Shoes (2 rows).
 * - [THREE_LAYERS]: Merged top (TShirt/Pullover) + Merged bottom (Pants/Skirt) + Shoes (3 rows).
 * - [FOUR_LAYERS]: TShirt + Pullover + Merged bottom + Shoes (4 rows).
 * - [GRID]: 4×2 grid — all 7 categories shown independently, including empty slots.
 */
enum class OutfitLayoutMode {
    TWO_LAYERS,
    THREE_LAYERS,
    FOUR_LAYERS,
    GRID
}
