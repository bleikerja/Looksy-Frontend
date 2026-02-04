package com.example.looksy.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.looksy.ui.components.LooksyButton
import com.example.looksy.data.model.Clothes
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.ui.viewmodel.WeatherUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullOutfitScreen(
    top: Clothes? = null,
    pants: Clothes? = null,
    dress: Clothes? = null,
    jacket: Clothes? = null,
    skirt: Clothes? = null,
    onClick: (Int) -> Unit = {},
    onConfirm: (List<Clothes>) -> Unit = {},
    onMoveToWashingMachine: (List<Clothes>, List<Clothes>) -> Unit = { _, _ -> },
    onWashingMachine: () -> Unit = {},
    onGenerateRandom: () -> Unit = {},
    onCamera: () -> Unit = {},
    onSave: () -> Unit = {},
    weatherState: WeatherUiState = WeatherUiState.Loading,
    onWeatherClick: () -> Unit = {}
) {
    if ((top != null || dress != null) && (pants != null || skirt != null)) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier.fillMaxSize()) {
            val confirmedOutfit =
                listOfNotNull(top, pants, dress, jacket, skirt).any { !it.selected }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(249, 246, 242))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    // Weather Icon Row
                    WeatherIconRow(
                        weatherState = weatherState,
                        onClick = onWeatherClick,
                        // modifier = Modifier.fillMaxWidth(0.2f)
                    )

                    // Spacer(modifier = Modifier.width(8.dp))

                    Header(
                        onNavigateBack = {},
                        onNavigateToRightIcon = { onWashingMachine() },
                        clothesData = null,
                        headerText = "Heutiges Outfit",
                        rightIconContentDescription = "Zur Waschmaschine",
                        rightIcon = Icons.Default.LocalLaundryService,
                        isFirstHeader = true,
                    )
                }
                   
                Spacer(modifier = Modifier.height(16.dp))

                jacket?.let {
                    OutfitPart(
                        imageResId = it.imagePath,
                        onClick = { onClick(it.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                dress?.let {
                    OutfitPart(
                        imageResId = it.imagePath,
                        onClick = { onClick(it.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                top?.let {
                    OutfitPart(
                        imageResId = it.imagePath,
                        onClick = { onClick(it.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                skirt?.let {
                    OutfitPart(
                        imageResId = it.imagePath,
                        onClick = { onClick(it.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                pants?.let {
                    OutfitPart(
                        imageResId = it.imagePath,
                        onClick = { onClick(it.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (confirmedOutfit) {
                IconButton(
                    onClick = onGenerateRandom,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp)
                        .size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Zufälliges Outfit generieren",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        onSave()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Outfit gespeichert",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier.size(50.dp)
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
                            val wornClothes = listOfNotNull(top, pants, dress, jacket, skirt)
                            onConfirm(wornClothes)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Schön, dass dir das Outfit gefällt und du es anziehst",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier.size(50.dp)
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
                        onClick = {
                            showConfirmDialog = true
                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Neues Outfit",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (showConfirmDialog) {
                        val wornClothes = listOfNotNull(top, pants, dress, jacket, skirt)
                        var selectedIds by remember {
                            mutableStateOf(wornClothes.map { it.id }.toSet())
                        }
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = {
                                Text(text = "Neues Outfit")
                            },
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
                                Button(
                                    onClick = {
                                        onMoveToWashingMachine(
                                            wornClothes.filter { it.id in selectedIds },
                                            wornClothes.filter { it.id !in selectedIds }
                                        )

                                        showConfirmDialog = false
                                    }
                                ) {
                                    Text("Weiter")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showConfirmDialog = false }
                                ) {
                                    Text("Abbrechen")
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                Text(textAlign = TextAlign.Center, text = "Kleidung hizufügen oder waschen, um Outfits zu sehen!")
                Row {
                    IconButton(onClick = { onWashingMachine() }, modifier = Modifier.size(75.dp)) {
                        Icon(
                            modifier = Modifier.fillMaxSize().padding(5.dp),
                            imageVector = Icons.Default.LocalLaundryService,
                            contentDescription = "Zur Waschmaschine"
                        )
                    }
                    IconButton(onClick = { onCamera() }, modifier = Modifier.size(75.dp)) {
                        Icon(
                            modifier = Modifier.fillMaxSize().padding(5.dp),
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Zur Kamera"
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun OutfitPart(imageResId: Any?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = imageResId,
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 16.dp),
            contentDescription = "Kleidungsstück",
        )
        LooksyButton(
            onClick = onClick,
            modifier = Modifier.align(Alignment.CenterVertically),
            picture = { Icon(Icons.Default.Create, contentDescription = "Bearbeiten") })
    }
}

@Composable
private fun WeatherIconRow(
    weatherState: WeatherUiState,
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
        when (weatherState) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wetter laden...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            is WeatherUiState.Success -> {
                // Weather icon based on conditions
                Text(
                    text = getWeatherEmoji(weatherState.weather.description),
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Temperature
                Text(
                    text = "${weatherState.weather.temperature.roundToInt()}°C",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))

//                // Location hint
//                Icon(
//                    imageVector = Icons.Default.LocationOn,
//                    contentDescription = null,
//                    modifier = Modifier.size(16.dp),
//                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
            }

            is WeatherUiState.Error -> {
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
        }

        // Subtle indicator to click
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Details anzeigen",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// Helper function to map weather descriptions to emojis
private fun getWeatherEmoji(description: String): String {
    return when {
        description.contains("clear", ignoreCase = true) -> "☀️"
        description.contains("cloud", ignoreCase = true) -> "☁️"
        description.contains("rain", ignoreCase = true) -> "🌧️"
        description.contains("drizzle", ignoreCase = true) -> "🌦️"
        description.contains("thunder", ignoreCase = true) -> "⛈️"
        description.contains("snow", ignoreCase = true) -> "❄️"
        description.contains("mist", ignoreCase = true) ||
        description.contains("fog", ignoreCase = true) -> "🌫️"
        else -> "🌤️"
    }
}

@Preview(showBackground = true)
@Composable
fun FullOutfitPreview() {
    LooksyTheme {
        FullOutfitScreen(
            top = null,
            pants = null,
            skirt = null,
            onClick = { }
        )
    }
}
