package com.example.looksy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue  // ✅ WICHTIG: Für by delegate
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue  // ✅ WICHTIG: Für by delegate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.ui.components.ConfirmationDialog
import com.example.looksy.ui.components.Header
import kotlinx.coroutines.launch

/**
 * Screen zur Detailansicht eines gespeicherten Outfits.
 * Zeigt das Outfit ähnlich wie der Home-Screen an.
 * Bietet drei Buttons: Bearbeiten, Löschen und Tragen/Auswählen.
 */
@Composable
fun OutfitDetailsScreen(
    outfit: Outfit,
    outfitTop: Clothes? = null,
    outfitPants: Clothes? = null,
    outfitDress: Clothes? = null,
    outfitJacket: Clothes? = null,
    outfitSkirt: Clothes? = null,
    outfitShoes: Clothes? = null,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onWear: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(249, 246, 242))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header ähnlich wie FullOutfitScreen
            Header(
                onNavigateBack = onNavigateBack,
                onNavigateToRightIcon = { },
                clothesData = null,
                headerText = "Outfit Details",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zeige alle Kleidungsstücke des Outfits
            // Ähnlich wie im FullOutfitScreen
            outfitJacket?.let {
                OutfitPart(
                    imageResId = it.imagePath,
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 12.dp)
                )
            }

            outfitTop?.let {
                OutfitPart(
                    imageResId = it.imagePath,
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 12.dp)
                )
            }

            outfitDress?.let {
                OutfitPart(
                    imageResId = it.imagePath,
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 12.dp)
                )
            }

            outfitSkirt?.let {
                OutfitPart(
                    imageResId = it.imagePath,
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 12.dp)
                )
            }

            outfitPants?.let {
                OutfitPart(
                    imageResId = it.imagePath,
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            outfitShoes?.let {
                OutfitPart(
                    imageResId = it.imagePath,
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Drei Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: Bearbeiten
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
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
                            .padding(end = 8.dp),
                        tint = Color.White
                    )
                    Text(
                        "Bearbeiten",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // Button 2: Löschen
                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
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
                            .padding(end = 8.dp),
                        tint = Color.White
                    )
                    Text(
                        "Löschen",
                        color = Color.White,
                        fontSize = 16.sp
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
                        .fillMaxWidth()
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
                            .padding(end = 8.dp),
                        tint = Color.White
                    )
                    Text(
                        "Tragen/Auswählen",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}