package com.example.looksy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DomainDisabled
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.looksy.R
import com.example.looksy.data.location.PermissionState
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.components.LooksyButton
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.ui.viewmodel.WeatherUiState
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// ───────────────────────────────── Layout State ─────────────────────────────────

private enum class LayoutState {
    TWO_LAYERS,    // Dress + Shoes (2 bricks)
    THREE_LAYERS,  // Merged Top + Merged Bottom + Shoes (3 bricks)
    FOUR_LAYERS,   // TShirt + Pullover + Merged Bottom + Shoes (4 bricks)
    GRID           // 4×2 grid: all 7 categories shown independently
}

// ───────────────────────────────── Main Screen ─────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullOutfitScreen(
    allClothes: List<Clothes> = emptyList(),
    selectedTshirtId: Int? = null,
    selectedPantsId: Int? = null,
    selectedSkirtId: Int? = null,
    selectedDressId: Int? = null,
    selectedJacketId: Int? = null,
    selectedPulloverId: Int? = null,
    selectedShoesId: Int? = null,
    onSlotChanged: (Type, Int?) -> Unit = { _, _ -> },
    onClick: (Int) -> Unit = {},
    onConfirm: (List<Clothes>) -> Unit = {},
    onMoveToWashingMachine: (List<Clothes>, List<Clothes>) -> Unit = { _, _ -> },
    onWashingMachine: () -> Unit = {},
    onGenerateRandom: () -> Unit = {},
    onCamera: () -> Unit = {},
    onSave: () -> Unit = {},
    onGridModeChanged: (Boolean) -> Unit = {},
    weatherState: WeatherUiState = WeatherUiState.Loading,
    permissionState: PermissionState = PermissionState.NOT_ASKED,
    isLocationEnabled: Boolean = true,
    onWeatherClick: () -> Unit = {}
) {
    val cleanClothes = allClothes.filter { it.clean }

    // Per-category lists
    val tshirtItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.TShirt } }
    val pulloverItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.Pullover } }
    val pantsItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.Pants } }
    val skirtItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.Skirt } }
    val dressItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.Dress } }
    val jacketItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.Jacket } }
    val shoesItems = remember(cleanClothes) { cleanClothes.filter { it.type == Type.Shoes } }

    // Merged lists for combined carousels
    val mergedTopItems = remember(cleanClothes) { (tshirtItems + pulloverItems).shuffled() }
    val mergedBottomItems = remember(cleanClothes) { (pantsItems + skirtItems).shuffled() }

    // Layout state: 2/3/4 layers
    var layoutState by remember {
        mutableStateOf(
            when {
                selectedDressId != null -> LayoutState.TWO_LAYERS
                selectedTshirtId != null && selectedPulloverId != null -> LayoutState.FOUR_LAYERS
                else -> LayoutState.THREE_LAYERS
            }
        )
    }

    // Jacket toggle state: on by default when jacket items exist
    var showJacket by remember { mutableStateOf(jacketItems.isNotEmpty()) }

    // Auto-disable jacket toggle when all jackets are removed from wardrobe
    LaunchedEffect(jacketItems) {
        if (jacketItems.isEmpty() && showJacket) {
            showJacket = false
            onSlotChanged(Type.Jacket, null)
        }
    }

    // Sync layout state from external changes (e.g. random generation)
    LaunchedEffect(selectedDressId) {
        if (selectedDressId != null && layoutState != LayoutState.GRID) {
            layoutState = LayoutState.TWO_LAYERS
        }
    }

    // Resolve selected clothes
    val currentTop = selectedTshirtId?.let { id -> tshirtItems.find { it.id == id } }
    val currentPullover = selectedPulloverId?.let { id -> pulloverItems.find { it.id == id } }
    val currentPants = selectedPantsId?.let { id -> pantsItems.find { it.id == id } }
    val currentSkirt = selectedSkirtId?.let { id -> skirtItems.find { it.id == id } }
    val currentDress = selectedDressId?.let { id -> dressItems.find { it.id == id } }
    val currentJacket = selectedJacketId?.let { id -> jacketItems.find { it.id == id } }
    val currentShoes = selectedShoesId?.let { id -> shoesItems.find { it.id == id } }

    val hasAnyClothes = allClothes.isNotEmpty()

    if (hasAnyClothes) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val allWornItems = listOfNotNull(currentTop, currentPullover, currentPants, currentSkirt, currentDress, currentJacket, currentShoes)
        val confirmedOutfit = allWornItems.isNotEmpty() && allWornItems.any { !it.selected }

        // In GRID mode, outfit is valid only if: (top OR pullover) AND (pants OR skirt), OR dress
        val gridOutfitValid = currentDress != null ||
                ((currentTop != null || currentPullover != null) &&
                 (currentPants != null || currentSkirt != null))

        Scaffold(
            topBar = {
                FullOutfitTopBar(
                    weatherState = weatherState,
                    permissionState = permissionState,
                    isLocationEnabled = isLocationEnabled,
                    onWeatherClick = onWeatherClick,
                    onWashingMachine = onWashingMachine
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(249, 246, 242))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {




                    // ──── Outfit area: jacket column + center carousels ────
                if (layoutState == LayoutState.GRID) {
                    // ──── GRID mode: 4×2 grid of independent carousels ────
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // LEFT column: Jacket, T-Shirt, Trousers, Skirt
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HorizontalClothesCarousel(
                                items = jacketItems,
                                selectedId = selectedJacketId,
                                onItemSelected = { id -> onSlotChanged(Type.Jacket, id) },
                                onItemClick = onClick,
                                categoryName = "Jacke",
                                allowNone = true,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            HorizontalClothesCarousel(
                                items = tshirtItems,
                                selectedId = selectedTshirtId,
                                onItemSelected = { id -> onSlotChanged(Type.TShirt, id) },
                                onItemClick = onClick,
                                categoryName = "T-Shirt/Longsleeve",
                                allowNone = true,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            HorizontalClothesCarousel(
                                items = pantsItems,
                                selectedId = selectedPantsId,
                                onItemSelected = { id -> onSlotChanged(Type.Pants, id) },
                                onItemClick = onClick,
                                categoryName = "Hose",
                                allowNone = true,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            HorizontalClothesCarousel(
                                items = skirtItems,
                                selectedId = selectedSkirtId,
                                onItemSelected = { id -> onSlotChanged(Type.Skirt, id) },
                                onItemClick = onClick,
                                categoryName = "Rock",
                                allowNone = true,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                        // RIGHT column: Pullover, Dress (2-row span), Shoes
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HorizontalClothesCarousel(
                                items = pulloverItems,
                                selectedId = selectedPulloverId,
                                onItemSelected = { id -> onSlotChanged(Type.Pullover, id) },
                                onItemClick = onClick,
                                categoryName = "Pullover/Sweatshirt",
                                allowNone = true,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            HorizontalClothesCarousel(
                                items = dressItems,
                                selectedId = selectedDressId,
                                onItemSelected = { id -> onSlotChanged(Type.Dress, id) },
                                onItemClick = onClick,
                                categoryName = "Kleid",
                                allowNone = true,
                                modifier = Modifier.weight(2f).fillMaxWidth()
                            )
                            HorizontalClothesCarousel(
                                items = shoesItems,
                                selectedId = selectedShoesId,
                                onItemSelected = { id -> onSlotChanged(Type.Shoes, id) },
                                onItemClick = onClick,
                                categoryName = "Schuhe",
                                allowNone = true,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                    }
                } else {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT column: Jacket (vertical carousel)
                    if (showJacket && jacketItems.isNotEmpty()) {
                        VerticalClothesCarousel(
                            items = jacketItems,
                            selectedId = selectedJacketId,
                            onItemSelected = { id -> onSlotChanged(Type.Jacket, id) },
                            onItemClick = onClick,
                            categoryName = "Jacke",
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .padding(end = 4.dp)
                        )
                    }

                    // CENTER column: carousels based on layout state
                    val centerWeight = if (!showJacket || jacketItems.isEmpty()) 1f else 0.75f
                    Column(
                        modifier = Modifier
                            .weight(centerWeight)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        when (layoutState) {
                            LayoutState.TWO_LAYERS -> {
                                // Dress + Shoes
                                HorizontalClothesCarousel(
                                    items = dressItems,
                                    selectedId = selectedDressId,
                                    onItemSelected = { id -> onSlotChanged(Type.Dress, id) },
                                    onItemClick = onClick,
                                    categoryName = "Kleid",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalClothesCarousel(
                                    items = shoesItems,
                                    selectedId = selectedShoesId,
                                    onItemSelected = { id -> onSlotChanged(Type.Shoes, id) },
                                    onItemClick = onClick,
                                    categoryName = "Schuhe",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                            }

                            LayoutState.THREE_LAYERS -> {
                                // Merged Top (TShirts + Pullovers) + Merged Bottom (Pants + Skirts) + Shoes
                                val mergedTopSelectedId = selectedTshirtId ?: selectedPulloverId
                                HorizontalClothesCarousel(
                                    items = mergedTopItems,
                                    selectedId = mergedTopSelectedId,
                                    onItemSelected = { id ->
                                        val item = mergedTopItems.find { it.id == id }
                                        if (item != null) {
                                            when (item.type) {
                                                Type.TShirt -> {
                                                    onSlotChanged(Type.TShirt, id)
                                                    onSlotChanged(Type.Pullover, null)
                                                }
                                                Type.Pullover -> {
                                                    onSlotChanged(Type.Pullover, id)
                                                    onSlotChanged(Type.TShirt, null)
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    onItemClick = onClick,
                                    categoryName = "Oberteil",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val mergedBottomSelectedId3 = selectedPantsId ?: selectedSkirtId
                                HorizontalClothesCarousel(
                                    items = mergedBottomItems,
                                    selectedId = mergedBottomSelectedId3,
                                    onItemSelected = { id ->
                                        val item = mergedBottomItems.find { it.id == id }
                                        if (item != null) {
                                            when (item.type) {
                                                Type.Pants -> {
                                                    onSlotChanged(Type.Pants, id)
                                                    onSlotChanged(Type.Skirt, null)
                                                }
                                                Type.Skirt -> {
                                                    onSlotChanged(Type.Skirt, id)
                                                    onSlotChanged(Type.Pants, null)
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    onItemClick = onClick,
                                    categoryName = "Unterteil",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                HorizontalClothesCarousel(
                                    items = shoesItems,
                                    selectedId = selectedShoesId,
                                    onItemSelected = { id -> onSlotChanged(Type.Shoes, id) },
                                    onItemClick = onClick,
                                    categoryName = "Schuhe",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                            }

                            LayoutState.FOUR_LAYERS -> {
                                // TShirt + Pullover + Merged Bottom + Shoes
                                HorizontalClothesCarousel(
                                    items = tshirtItems,
                                    selectedId = selectedTshirtId,
                                    onItemSelected = { id -> onSlotChanged(Type.TShirt, id) },
                                    onItemClick = onClick,
                                    categoryName = "T-Shirt/Longsleeve",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                HorizontalClothesCarousel(
                                    items = pulloverItems,
                                    selectedId = selectedPulloverId,
                                    onItemSelected = { id -> onSlotChanged(Type.Pullover, id) },
                                    onItemClick = onClick,
                                    categoryName = "Pullover/Sweatshirt",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val mergedBottomSelectedId4 = selectedPantsId ?: selectedSkirtId
                                HorizontalClothesCarousel(
                                    items = mergedBottomItems,
                                    selectedId = mergedBottomSelectedId4,
                                    onItemSelected = { id ->
                                        val item = mergedBottomItems.find { it.id == id }
                                        if (item != null) {
                                            when (item.type) {
                                                Type.Pants -> {
                                                    onSlotChanged(Type.Pants, id)
                                                    onSlotChanged(Type.Skirt, null)
                                                }
                                                Type.Skirt -> {
                                                    onSlotChanged(Type.Skirt, id)
                                                    onSlotChanged(Type.Pants, null)
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    onItemClick = onClick,
                                    categoryName = "Unterteil",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                HorizontalClothesCarousel(
                                    items = shoesItems,
                                    selectedId = selectedShoesId,
                                    onItemSelected = { id -> onSlotChanged(Type.Shoes, id) },
                                    onItemClick = onClick,
                                    categoryName = "Schuhe",
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                )
                            }

                            LayoutState.GRID -> {
                                // Handled by the if-branch above; unreachable here
                            }
                        }
                    }
                }
                }  // end else (non-GRID)

                Spacer(modifier = Modifier.height(8.dp))

                // ──── Bottom action row ────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Shuffle button (always visible)
                    IconButton(
                        onClick = {
                            if (layoutState == LayoutState.GRID) {
                                // Exit GRID mode, then generate random
                                layoutState = LayoutState.THREE_LAYERS
                                onGridModeChanged(false)
                            }
                            onGenerateRandom()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Zufälliges Outfit generieren",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Jacket toggle + State selector buttons + Grid button (grouped)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        val isGrid = layoutState == LayoutState.GRID
                        // Jacket vertical-brick toggle
                        JacketBrickButton(
                            selected = showJacket && !isGrid,
                            enabled = jacketItems.isNotEmpty() && !isGrid,
                            onClick = {
                                if (showJacket) onSlotChanged(Type.Jacket, null)
                                showJacket = !showJacket
                            }
                        )
                        // Thin vertical divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        // Layer-count state buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            StateButton(
                                brickCount = 2,
                                selected = layoutState == LayoutState.TWO_LAYERS,
                                enabled = !isGrid,
                                onClick = {
                                    if (layoutState != LayoutState.TWO_LAYERS) {
                                        onSlotChanged(Type.TShirt, null)
                                        onSlotChanged(Type.Pullover, null)
                                        onSlotChanged(Type.Pants, null)
                                        onSlotChanged(Type.Skirt, null)
                                        layoutState = LayoutState.TWO_LAYERS
                                    }
                                }
                            )
                            StateButton(
                                brickCount = 3,
                                selected = layoutState == LayoutState.THREE_LAYERS,
                                enabled = !isGrid,
                                onClick = {
                                    if (layoutState != LayoutState.THREE_LAYERS) {
                                        if (layoutState == LayoutState.TWO_LAYERS) {
                                            onSlotChanged(Type.Dress, null)
                                        }
                                        if (layoutState == LayoutState.FOUR_LAYERS &&
                                            selectedTshirtId != null && selectedPulloverId != null
                                        ) {
                                            onSlotChanged(Type.Pullover, null)
                                        }
                                        layoutState = LayoutState.THREE_LAYERS
                                    }
                                }
                            )
                            StateButton(
                                brickCount = 4,
                                selected = layoutState == LayoutState.FOUR_LAYERS,
                                enabled = !isGrid,
                                onClick = {
                                    if (layoutState != LayoutState.FOUR_LAYERS) {
                                        if (layoutState == LayoutState.TWO_LAYERS) {
                                            onSlotChanged(Type.Dress, null)
                                        }
                                        layoutState = LayoutState.FOUR_LAYERS
                                    }
                                }
                            )
                        }
                        // Thin vertical divider before Grid button
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        // Grid mode toggle
                        GridModeButton(
                            selected = isGrid,
                            onClick = {
                                if (isGrid) {
                                    // Leave GRID → default to THREE_LAYERS
                                    // Cleanup: if both dress and top are selected, clear dress
                                    if (selectedDressId != null && (selectedTshirtId != null || selectedPulloverId != null)) {
                                        onSlotChanged(Type.Dress, null)
                                    }
                                    layoutState = LayoutState.THREE_LAYERS
                                    onGridModeChanged(false)
                                } else {
                                    // Enter GRID mode
                                    layoutState = LayoutState.GRID
                                    onGridModeChanged(true)
                                }
                            }
                        )
                    }

                    // 3. Save + confirm/refresh buttons (right)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val isGridMode = layoutState == LayoutState.GRID
                        val buttonsEnabled = !isGridMode || gridOutfitValid
                        IconButton(
                            onClick = {
                                if (buttonsEnabled) {
                                    onSave()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Outfit gespeichert",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .alpha(if (buttonsEnabled) 1f else 0.3f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Outfit speichern",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (confirmedOutfit) {
                            IconButton(
                                onClick = {
                                    if (buttonsEnabled) {
                                        onConfirm(allWornItems)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Schön, dass dir das Outfit gefällt und du es anziehst",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .alpha(if (buttonsEnabled) 1f else 0.3f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Outfit anziehen",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            var showConfirmDialog by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Neues Outfit",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (showConfirmDialog) {
                                val wornClothes = allWornItems
                                var selectedIds by remember {
                                    mutableStateOf(wornClothes.map { it.id }.toSet())
                                }
                                AlertDialog(
                                    onDismissRequest = { showConfirmDialog = false },
                                    title = { Text(text = "Neues Outfit") },
                                    text = {
                                        Column {
                                            Text(
                                                text = "Welche Kleider sollen als schmutzig markiert werden?",
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            )
                                            LazyVerticalGrid(
                                                columns = GridCells.Fixed(2),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                items(wornClothes) { clothItem ->
                                                    val isSelected = clothItem.id in selectedIds
                                                    WashingItemContainer(
                                                        item = clothItem,
                                                        isSelected = isSelected,
                                                        onClick = {
                                                            selectedIds =
                                                                if (isSelected) selectedIds - clothItem.id
                                                                else selectedIds + clothItem.id
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            onMoveToWashingMachine(
                                                wornClothes.filter { it.id in selectedIds },
                                                wornClothes.filter { it.id !in selectedIds }
                                            )
                                            showConfirmDialog = false
                                        }) { Text("Weiter") }
                                    },
                                    dismissButton = {
                                        Button(onClick = { showConfirmDialog = false }) {
                                            Text("Abbrechen")
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } }else {
        Scaffold(
            topBar = {
                Header(
                    onNavigateBack = {},
                    onNavigateToRightIcon = {},
                    clothesData = null,
                    headerText = "Heutiges Outfit",
                    rightIconContentDescription = null,
                    rightIcon = null,
                    isFirstHeader = true
                )
            }
        ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            WeatherIconRow(
                weatherState = weatherState,
                permissionState = permissionState,
                isLocationEnabled = isLocationEnabled,
                onClick = onWeatherClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
            )
            IconButton(
                onClick = onGenerateRandom,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Zufälliges Outfit generieren",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Kleidung hinzufügen oder waschen, um Outfits zu sehen!"
                )
                Row {
                    IconButton(onClick = { onWashingMachine() }, modifier = Modifier.size(75.dp)) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            imageVector = Icons.Default.LocalLaundryService,
                            contentDescription = "Zur Waschmaschine"
                        )
                    }
                    IconButton(onClick = { onCamera() }, modifier = Modifier.size(75.dp)) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Zur Kamera"
                        )
                    }
                }
            }
        }
        } // closes Scaffold lambda
    }
}

// ───────────────────── Horizontal Clothes Carousel ─────────────────────

@Composable
fun HorizontalClothesCarousel(
    items: List<Clothes>,
    selectedId: Int?,
    onItemSelected: (Int?) -> Unit,
    onItemClick: (Int) -> Unit,
    categoryName: String,
    allowNone: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty() && !allowNone) {
        // Empty placeholder
        Box(
            modifier = modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Keine $categoryName",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // When allowNone: page 0 = "Keine Auswahl", pages 1..N = items
    // When !allowNone: pages 0..N-1 = items
    val totalPageCount = if (allowNone) items.size + 1 else items.size

    // If items is empty and allowNone, show only the "Keine Auswahl" page
    if (totalPageCount == 0) return

    val scope = rememberCoroutineScope()
    val initialPage = remember(items, selectedId, allowNone) {
        if (allowNone) {
            if (selectedId == null) 0
            else (items.indexOfFirst { it.id == selectedId } + 1).coerceAtLeast(0)
        } else {
            items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
        }
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { totalPageCount }

    // Sync pager when selectedId changes externally
    LaunchedEffect(selectedId, items, allowNone) {
        if (allowNone) {
            val targetIndex = if (selectedId == null) 0
                              else (items.indexOfFirst { it.id == selectedId } + 1).coerceAtLeast(0)
            if (targetIndex != pagerState.currentPage) {
                pagerState.scrollToPage(targetIndex)
            }
        } else {
            val targetIndex = items.indexOfFirst { it.id == selectedId }
            if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
                pagerState.scrollToPage(targetIndex)
            }
        }
    }

    // Notify parent when user swipes to a new page
    LaunchedEffect(pagerState, allowNone) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (allowNone) {
                if (page == 0) {
                    if (selectedId != null) onItemSelected(null)
                } else {
                    val itemIndex = page - 1
                    if (itemIndex in items.indices) {
                        val newId = items[itemIndex].id
                        if (newId != selectedId) onItemSelected(newId)
                    }
                }
            } else {
                if (page in items.indices) {
                    val newId = items[page].id
                    if (newId != selectedId) onItemSelected(newId)
                }
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Pager fills full space
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            pageSpacing = 4.dp
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction).absoluteValue

            if (allowNone && page == 0) {
                // "Keine Auswahl" placeholder page
                NoneSelectionCard(
                    categoryName = categoryName,
                    dimFactor = pageOffset.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val itemIndex = if (allowNone) page - 1 else page
                if (itemIndex in items.indices) {
                    CarouselItemCard(
                        clothes = items[itemIndex],
                        onClick = { onItemClick(items[itemIndex].id) },
                        dimFactor = pageOffset.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Left arrow overlay
        CarouselArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Vorheriges $categoryName",
            enabled = pagerState.currentPage > 0,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp)
        )

        // Right arrow overlay
        CarouselArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Nächstes $categoryName",
            enabled = pagerState.currentPage < totalPageCount - 1,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp)
        )
    }
}

// ───────────────────── Vertical Clothes Carousel ─────────────────────

@Composable
private fun VerticalClothesCarousel(
    items: List<Clothes>,
    selectedId: Int?,
    onItemSelected: (Int?) -> Unit,
    onItemClick: (Int) -> Unit,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Keine $categoryName",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val scope = rememberCoroutineScope()
    val initialPage = remember(items, selectedId) {
        items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { items.size }

    // Sync pager when selectedId changes externally
    LaunchedEffect(selectedId, items) {
        val targetIndex = items.indexOfFirst { it.id == selectedId }
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    // Notify parent when user swipes to a new page
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page in items.indices) {
                val newId = items[page].id
                if (newId != selectedId) {
                    onItemSelected(newId)
                }
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Pager fills full space
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            pageSpacing = 4.dp
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction).absoluteValue

            CarouselItemCard(
                clothes = items[page],
                onClick = { onItemClick(items[page].id) },
                dimFactor = pageOffset.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Up arrow overlay
        CarouselArrowButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Vorheriges $categoryName",
            enabled = pagerState.currentPage > 0,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(32.dp)
        )

        // Down arrow overlay
        CarouselArrowButton(
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = "Nächstes $categoryName",
            enabled = pagerState.currentPage < items.size - 1,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(32.dp)
        )
    }
}

// ─────────────────────── Carousel Item Card ───────────────────────

@Composable
private fun CarouselItemCard(
    clothes: Clothes,
    onClick: () -> Unit,
    dimFactor: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .graphicsLayer {
                alpha = 1f - (dimFactor * 0.6f)
            }
            .clickable(onClick = onClick),
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

// ─────────────────────── None Selection Card ───────────────────────

@Composable
private fun NoneSelectionCard(
    categoryName: String,
    dimFactor: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .graphicsLayer {
                alpha = 1f - (dimFactor * 0.6f)
            },
        contentAlignment = Alignment.Center
    ) {
        val dashedColor = Color.Gray.copy(alpha = 0.5f)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        ) {
            drawRoundRect(
                color = dashedColor,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(10.dp.toPx(), 6.dp.toPx()),
                        0f
                    )
                )
            )
        }
        Text(
            text = "Keine\nAuswahl",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────── Arrow Button ───────────────────────

@Composable
private fun CarouselArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LooksyButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier.alpha(if (enabled) 1f else 0.3f),
        picture = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    )
}

// ─────────────────────── Brick Icon ───────────────────────

@Composable
private fun BrickIcon(
    brickCount: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val totalHeight = size.height
        val totalWidth = size.width
        val gap = 2.dp.toPx()
        val brickHeight = (totalHeight - (brickCount - 1) * gap) / brickCount
        val cornerRadius = 3.dp.toPx()

        for (i in 0 until brickCount) {
            val top = i * (brickHeight + gap)
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, top),
                size = Size(totalWidth, brickHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}

// ─────────────────────── Jacket Brick Button ───────────────────────

@Composable
private fun JacketBrickButton(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                  else Color.Transparent
    val iconColor = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    IconButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val cornerRadius = 3.dp.toPx()
            val brickWidth = size.width * 0.42f
            drawRoundRect(
                color = iconColor,
                topLeft = Offset((size.width - brickWidth) / 2f, 0f),
                size = Size(brickWidth, size.height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}

// ─────────────────────── State Button ───────────────────────

@Composable
private fun StateButton(
    brickCount: Int,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                  else Color.Transparent
    val iconColor = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    IconButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
    ) {
        BrickIcon(brickCount = brickCount, color = iconColor)
    }
}

// ─────────────────────── Grid Brick Icon ───────────────────────

@Composable
private fun GridBrickIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val totalHeight = size.height
        val totalWidth = size.width
        val gap = 2.dp.toPx()
        val rows = 4
        val cols = 2
        val brickHeight = (totalHeight - (rows - 1) * gap) / rows
        val brickWidth = (totalWidth - (cols - 1) * gap) / cols
        val cornerRadius = 3.dp.toPx()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val top = row * (brickHeight + gap)
                val left = col * (brickWidth + gap)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(brickWidth, brickHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }
    }
}

// ─────────────────────── Grid Mode Button ───────────────────────

@Composable
private fun GridModeButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                  else Color.Transparent
    val iconColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
    ) {
        GridBrickIcon(color = iconColor)
    }
}

// ─────────────────────── Weather Icon Row ───────────────────────

@Composable
private fun WeatherIconRow(
    weatherState: WeatherUiState,
    permissionState: PermissionState,
    isLocationEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            // Always show weather data when it is available, regardless of permission state.
            // The user may have entered a city manually without granting GPS permission.
            weatherState is WeatherUiState.Success -> {
                Text(
                    text = getWeatherEmoji(weatherState.weather.iconUrl),
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${weatherState.weather.temperature.roundToInt()}°C",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            weatherState is WeatherUiState.Loading &&
            permissionState != PermissionState.NOT_ASKED -> {
                Spacer(modifier = Modifier.width(20.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("weather_loading"),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            weatherState is WeatherUiState.Error &&
            (permissionState == PermissionState.GRANTED_WHILE_IN_USE ||
             permissionState == PermissionState.GRANTED_ONCE) -> {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Weather unavailable",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wetter nicht verfügbar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            permissionState == PermissionState.NOT_ASKED -> {
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Default.DomainDisabled,
                    contentDescription = "Standortzugriff erforderlich",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            (permissionState == PermissionState.GRANTED_WHILE_IN_USE ||
             permissionState == PermissionState.GRANTED_ONCE) &&
            !isLocationEnabled -> {
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = "Standort aktivieren",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            permissionState == PermissionState.DENIED -> {
                Spacer(modifier = Modifier.width(20.dp))
                Text(text = "📍❌", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wetter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            else -> {
                // Loading with no permission and no GPS — show nothing
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Details anzeigen",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun FullOutfitTopBar(
    weatherState: WeatherUiState,
    permissionState: PermissionState,
    isLocationEnabled: Boolean,
    onWeatherClick: () -> Unit,
    onWashingMachine: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Header(
            onNavigateBack = {},
            onNavigateToRightIcon = { onWashingMachine() },
            clothesData = null,
            headerText = "Heutiges Outfit",
            rightIconContentDescription = "Zur Waschmaschine",
            rightIcon = Icons.Default.LocalLaundryService,
            isFirstHeader = true
        )
        WeatherIconRow(
            weatherState = weatherState,
            permissionState = permissionState,
            isLocationEnabled = isLocationEnabled,
            onClick = onWeatherClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullOutfitPreview() {
    LooksyTheme {
        FullOutfitScreen(
            allClothes = emptyList(),
            onClick = { }
        )
    }
}

