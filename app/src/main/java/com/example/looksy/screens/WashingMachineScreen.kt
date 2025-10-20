package com.example.looksy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.R
import com.example.looksy.dataClassClones.Clothes
import java.io.File
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WashingMachineScreen(
    dirtyClothes: List<Clothes>,
    onNavigateBack: () -> Unit,
    onConfirmWashed: (List<Clothes>) -> Unit
) {
    // Zustand, der die IDs der ausgewählten Kleidungsstücke speichert
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    val washingNotesToShow by remember(selectedIds, dirtyClothes) {
        mutableStateOf(
            dirtyClothes
                .filter { it.id in selectedIds }
                .map { it.washingNotes }
                .distinct() // Zeige jeden Waschhinweis nur einmal an
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Waschmaschine",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                actions = {
                    // "Alle auswählen" / "Auswahl aufheben"-Button
                    IconButton(onClick = {
                        if (selectedIds.size == dirtyClothes.size) {
                            // Wenn alles ausgewählt ist, Auswahl aufheben
                            selectedIds = emptySet()
                        } else {
                            // Sonst alles auswählen
                            selectedIds = dirtyClothes.map { it.id }.toSet()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Alle auswählen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (selectedIds.isNotEmpty()) {
                Button(
                    onClick = {
                        val clothesToUpdate = dirtyClothes.filter { it.id in selectedIds }
                        onConfirmWashed(clothesToUpdate)
                        selectedIds = emptySet()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Gewaschen (${selectedIds.size})")
                }
            }
        }
    ) { innerPadding ->
        // NEU: Prüfen, ob die Liste der schmutzigen Wäsche leer ist
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // NEU: Zeige die Waschhinweise an, wenn etwas ausgewählt ist
            if (washingNotesToShow.isNotEmpty()) {
                Text(
                    "Waschhinweise der Auswahl:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                // Horizontale, scrollbare Reihe für die Waschhinweise
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(washingNotesToShow) { note ->
                        // Einfacher "Chip" zur Anzeige des Hinweises
                        Text(
                            text = note.displayName,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            if (dirtyClothes.isEmpty()) {
                // Zeige eine "Alles sauber"-Nachricht an
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Alles sauber! ✨",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dirtyClothes) { clothItem ->
                        val isSelected = clothItem.id in selectedIds
                        WashingItemContainer(
                            item = clothItem,
                            isSelected = isSelected,
                            onClick = {
                                selectedIds = if (isSelected) {
                                    selectedIds - clothItem.id
                                } else {
                                    selectedIds + clothItem.id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WashingItemContainer(
    item: Clothes,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        Color(0xFF8A2BE2) // Lila Farbe für die Umrandung
    } else {
        Color.Transparent // Keine Umrandung, wenn nicht ausgewählt
    }

    AsyncImage(
        model = File(item.imagePath).toUri(),
        contentDescription = null,
        modifier = Modifier
            .size(165.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentScale = ContentScale.Fit,
        placeholder = painterResource(id = R.drawable.clothicon),
        error = painterResource(id = R.drawable.wardrobe2icon),
    )
}

