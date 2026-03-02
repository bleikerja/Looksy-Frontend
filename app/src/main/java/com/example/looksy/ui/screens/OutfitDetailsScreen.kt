package com.example.looksy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.ui.components.ConfirmationDialog
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.components.OutfitLayoutPreview
import kotlinx.coroutines.launch

/**
 * Screen zur Detailansicht eines gespeicherten Outfits.
 * Zeigt das Outfit im selben Layout wie der FullOutfitScreen (Carousel / Grid).
 * Bietet drei Buttons: Bearbeiten, Löschen und Tragen/Auswählen in einer Zeile am unteren Rand.
 */
@Composable
fun OutfitDetailsScreen(
    outfit: Outfit,
    allClothes: List<Clothes>,
    onClothesClick: (Int) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onWear: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Header(
                onNavigateBack = onNavigateBack,
                onNavigateToRightIcon = { },
                clothesData = null,
                headerText = "Outfit Details",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Three action buttons in a horizontal row, equal width
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(249, 246, 242))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Bearbeiten
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(200, 150, 200)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Bearbeiten",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp),
                        tint = Color.White
                    )
                    Text(
                        "Bearbeiten",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                // Button 2: Löschen
                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(220, 100, 100)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Löschen",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp),
                        tint = Color.White
                    )
                    Text(
                        "Löschen",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                // Button 3: Tragen/Auswählen
                Button(
                    onClick = {
                        onWear()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Outfit im Home angezeigt - bitte bestätigen",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(100, 180, 100)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Tragen",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp),
                        tint = Color.White
                    )
                    Text(
                        "Tragen",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(249, 246, 242))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Outfit preview using the same layout as FullOutfitScreen
            OutfitLayoutPreview(
                outfit = outfit,
                allClothes = allClothes,
                onClothesClick = onClothesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    // Lösch-Bestätigungsdialog
    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = "Outfit löschen?",
            text = "Möchtest du dieses Outfit wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
            dismissText = "Abbrechen",
            onDismiss = { showDeleteConfirmDialog = false },
            confirmText = "Löschen",
            isDeletion = true,
            onConfirm = {
                onDelete()
                showDeleteConfirmDialog = false
            }
        )
    }
}