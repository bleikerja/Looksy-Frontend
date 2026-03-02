package com.example.looksy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.R
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.components.OutfitLayoutPreview
import com.example.looksy.ui.theme.LooksyTheme

/**
 * Screen zur Anzeige aller gespeicherten Outfits.
 * Zeigt ein Grid von Outfits an, die scrollbar sind.
 * Bei keinen gespeicherten Outfits wird ein Alternativtext angezeigt.
 */
@Composable
fun SavedOutfitsScreen(
    outfits: List<Outfit>,
    allClothes: List<Clothes>,
    onOutfitClick: (Int) -> Unit = {}
) {
    Scaffold(
        topBar = {
            Header(
                onNavigateBack = { },
                onNavigateToRightIcon = { },
                clothesData = null,
                headerText = "Gespeicherte Outfits",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = true
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(249, 246, 242))
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp)
        ) {


            if (outfits.isEmpty()) {
                EmptyOutfitsState()
            } else {
                OutfitsGrid(
                    outfits = outfits,
                    allClothes = allClothes,
                    onOutfitClick = onOutfitClick
                )
            }
        }
    }
}

/**
 * Alternativtext wenn keine Outfits vorhanden sind.
 */
@Composable
private fun EmptyOutfitsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Checkroom,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Text(
                text = "Noch keine Outfits gespeichert",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Speichere dein erstes Outfit, um es hier zu sehen!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}

/**
 * Grid-Ansicht aller Outfits.
 * Zeigt 2 Outfits pro Reihe an, scrollbar.
 */
@Composable
private fun OutfitsGrid(
    outfits: List<Outfit>,
    allClothes: List<Clothes>,
    onOutfitClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        items(outfits) { outfit ->
            OutfitCard(
                outfit = outfit,
                allClothes = allClothes,
                onClick = { onOutfitClick(outfit.id) }
            )
        }
    }
}

/**
 * Einzelne Outfit-Karte, die eine kompakte Vorschau des Outfits zeigt.
 * Verwendet OutfitLayoutPreview für eine konsistente Darstellung mit dem Home-Screen.
 */
@Composable
private fun OutfitCard(
    outfit: Outfit,
    allClothes: List<Clothes>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        OutfitLayoutPreview(
            outfit = outfit,
            allClothes = allClothes,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SavedOutfitsScreenEmptyPreview() {
    LooksyTheme {
        SavedOutfitsScreen(
            outfits = emptyList(),
            allClothes = emptyList()
        )
    }
}


