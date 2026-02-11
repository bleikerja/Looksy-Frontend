package com.example.looksy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.looksy.data.model.Clothes
import com.example.looksy.ui.components.Header
import java.io.File
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscardScreen(
    clothesToDiscard: List<Clothes>,
    onNavigateBack: () -> Unit,
    onConfirmDiscard: (List<Clothes>) -> Unit,
    onUndoDiscard: () -> Unit,
    canUndo: Boolean
) {
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            Header(
                onNavigateBack = onNavigateBack,
                onNavigateToRightIcon = {
                    if (selectedIds.size == clothesToDiscard.size) {
                        selectedIds = emptySet()
                    } else {
                        selectedIds = clothesToDiscard.map { it.id }.toSet()
                    }
                },
                clothesData = null,
                headerText = "Aussortieren",
                rightIconContentDescription = "Alle auswählen",
                rightIcon = Icons.Default.Checklist
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canUndo) {
                    Button(
                        onClick = onUndoDiscard,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null)
                        Text("Rückgängig", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                if (selectedIds.isNotEmpty()) {
                    Button(
                        onClick = {
                            val clothesToUpdate = clothesToDiscard.filter { it.id in selectedIds }
                            onConfirmDiscard(clothesToUpdate)
                            selectedIds = emptySet()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Aussortieren (${selectedIds.size})")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (clothesToDiscard.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Nichts zum Aussortieren! 😊",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        if (canUndo) {
                            Button(onClick = onUndoDiscard, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Letztes Aussortieren rückgängig machen")
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Diese Sachen hast du seit über einem Jahr nicht mehr getragen:",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clothesToDiscard) { clothItem ->
                        val isSelected = clothItem.id in selectedIds
                        DiscardItemContainer(
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
fun DiscardItemContainer(
    item: Clothes,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        Color.Red // Rot für Aussortieren
    } else {
        Color.Transparent
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
