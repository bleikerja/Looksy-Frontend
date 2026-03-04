package com.example.looksy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.R
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.OutfitLayoutMode

/**
 * Static preview of an outfit, rendered in the same visual layout as [FullOutfitScreen].
 *
 * Used by [OutfitDetailsScreen] and [SavedOutfitsScreen] / [OutfitCard] to display
 * saved outfits consistently.
 *
 * Layout behaviour per [Outfit.layoutMode]:
 * - **TWO_LAYERS** — Dress + Shoes stacked vertically (center only).
 * - **THREE_LAYERS** — Top-or-Pullover + Bottom + Shoes (3 rows, center column).
 *   If [Outfit.isJacketVisible] and jacket exists, jacket image appears on the left.
 * - **FOUR_LAYERS** — TShirt + Pullover + Bottom + Shoes (4 rows, center column).
 *   Left jacket column if [Outfit.isJacketVisible].
 * - **GRID** — 4×2 grid: Left column = Jacket, TShirt, Pants, Skirt.
 *   Right column = Pullover, Dress (2× height), Shoes.
 *   Empty slots show a simple gray rounded placeholder.
 */
@Composable
fun OutfitLayoutPreview(
    outfit: Outfit,
    allClothes: List<Clothes>,
    onClothesClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // Resolve clothes from IDs
    val top = outfit.topsId?.let { id -> allClothes.find { it.id == id } }
    val pullover = outfit.pulloverId?.let { id -> allClothes.find { it.id == id } }
    val pants = outfit.pantsId?.let { id -> allClothes.find { it.id == id } }
    val skirt = outfit.skirtId?.let { id -> allClothes.find { it.id == id } }
    val dress = outfit.dressId?.let { id -> allClothes.find { it.id == id } }
    val jacket = outfit.jacketId?.let { id -> allClothes.find { it.id == id } }
    val shoes = outfit.shoesId?.let { id -> allClothes.find { it.id == id } }

    val containerModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    when (outfit.layoutMode) {
        OutfitLayoutMode.GRID -> GridLayout(
            jacket = jacket,
            top = top,
            pullover = pullover,
            pants = pants,
            skirt = skirt,
            dress = dress,
            shoes = shoes,
            onClothesClick = onClothesClick,
            modifier = containerModifier
        )

        OutfitLayoutMode.TWO_LAYERS -> CarouselLayout(
            jacket = if (outfit.isJacketVisible) jacket else null,
            showJacketColumn = outfit.isJacketVisible,
            centerSlots = listOf(dress, shoes),
            onClothesClick = onClothesClick,
            modifier = containerModifier
        )

        OutfitLayoutMode.THREE_LAYERS -> {
            // Merged top: show whichever is non-null (top takes precedence)
            val mergedTop = top ?: pullover
            // Merged bottom: show whichever is non-null (pants takes precedence)
            val mergedBottom = pants ?: skirt
            CarouselLayout(
                jacket = if (outfit.isJacketVisible) jacket else null,
                showJacketColumn = outfit.isJacketVisible,
                centerSlots = listOf(mergedTop, mergedBottom, shoes),
                onClothesClick = onClothesClick,
                modifier = containerModifier
            )
        }

        OutfitLayoutMode.FOUR_LAYERS -> {
            val mergedBottom = pants ?: skirt
            CarouselLayout(
                jacket = if (outfit.isJacketVisible) jacket else null,
                showJacketColumn = outfit.isJacketVisible,
                centerSlots = listOf(top, pullover, mergedBottom, shoes),
                onClothesClick = onClothesClick,
                modifier = containerModifier
            )
        }
    }
}

// ───────────────────── Carousel-style layout (2/3/4 layers) ─────────────────────

/**
 * Renders a carousel-like static layout with an optional jacket column on the left
 * and N center slots stacked vertically.
 */
@Composable
private fun CarouselLayout(
    jacket: Clothes?,
    showJacketColumn: Boolean,
    centerSlots: List<Clothes?>,
    onClothesClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left jacket column (only if flagged and jacket exists)
        if (showJacketColumn && jacket != null) {
            ClothesImageSlot(
                clothes = jacket,
                onClick = onClothesClick?.let { { it(jacket.id) } },
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .padding(end = 2.dp)
            )
        }

        // Center column: stacked slots
        val centerWeight = if (showJacketColumn && jacket != null) 0.65f else 1f
        Column(
            modifier = Modifier
                .weight(centerWeight)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            centerSlots.forEach { clothes ->
                if (clothes != null) {
                    ClothesImageSlot(
                        clothes = clothes,
                        onClick = onClothesClick?.let { { it(clothes.id) } },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                // null slots in non-GRID modes are simply skipped
            }
        }
    }
}

// ───────────────────── Grid layout (4×2) ─────────────────────

/**
 * Renders the 4×2 grid layout with empty gray placeholders for null slots.
 */
@Composable
private fun GridLayout(
    jacket: Clothes?,
    top: Clothes?,
    pullover: Clothes?,
    pants: Clothes?,
    skirt: Clothes?,
    dress: Clothes?,
    shoes: Clothes?,
    onClothesClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // LEFT column: Jacket, T-Shirt, Pants, Skirt (4 equal rows)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GridSlot(clothes = jacket, onClick = jacket?.let { onClothesClick?.let { c -> { c(jacket.id) } } }, modifier = Modifier.weight(1f).fillMaxWidth())
            GridSlot(clothes = top, onClick = top?.let { onClothesClick?.let { c -> { c(top.id) } } }, modifier = Modifier.weight(1f).fillMaxWidth())
            GridSlot(clothes = pants, onClick = pants?.let { onClothesClick?.let { c -> { c(pants.id) } } }, modifier = Modifier.weight(1f).fillMaxWidth())
            GridSlot(clothes = skirt, onClick = skirt?.let { onClothesClick?.let { c -> { c(skirt.id) } } }, modifier = Modifier.weight(1f).fillMaxWidth())
        }

        // RIGHT column: Pullover (1×), Dress (2×), Shoes (1×)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GridSlot(clothes = pullover, onClick = pullover?.let { onClothesClick?.let { c -> { c(pullover.id) } } }, modifier = Modifier.weight(1f).fillMaxWidth())
            GridSlot(clothes = dress, onClick = dress?.let { onClothesClick?.let { c -> { c(dress.id) } } }, modifier = Modifier.weight(2f).fillMaxWidth())
            GridSlot(clothes = shoes, onClick = shoes?.let { onClothesClick?.let { c -> { c(shoes.id) } } }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}

// ───────────────────── Shared slot composables ─────────────────────

/**
 * Renders a single clothes image in a rounded container.
 */
@Composable
private fun ClothesImageSlot(
    clothes: Clothes,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Box(
        modifier = clickableModifier
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = clothes.imagePath,
            contentDescription = clothes.type.displayName,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp),
            contentScale = ContentScale.Fit,
            error = painterResource(id = R.drawable.clothicon)
        )
    }
}

/**
 * Grid slot: shows the clothes image if present, or a gray placeholder if null.
 */
@Composable
private fun GridSlot(
    clothes: Clothes?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (clothes != null) {
        ClothesImageSlot(clothes = clothes, onClick = onClick, modifier = modifier)
    } else {
        // Simple gray rounded placeholder for empty grid slots
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
        )
    }
}
