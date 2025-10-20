package com.example.looksy.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.looksy.R
import com.example.looksy.ViewModels.ClothesViewModel
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Material
import com.example.looksy.dataClassClones.Season
import com.example.looksy.dataClassClones.Size
import com.example.looksy.dataClassClones.Type
import com.example.looksy.dataClassClones.WashingNotes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewClothesScreen(
    imageUriString: String?,
    viewModel: ClothesViewModel,
    onSave: (newItem: Clothes) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    clothesIdToEdit: Int? = null
) {
    val clothesToEdit by if (clothesIdToEdit != null) {
        viewModel.getClothesById(clothesIdToEdit).collectAsState(initial = null)
    } else {
        // Im "Neu anlegen"-Modus haben wir kein Objekt zum Bearbeiten
        remember { mutableStateOf(null) }
    }

    // --- ANPASSUNG: Zustände mit den geladenen Daten initialisieren ---
    var size by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.size) }
    var season by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.seasonUsage) }
    var type by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.type) }
    var material by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.material) }
    var washingNotes by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.washingNotes) }

    val isFormValid =
                size != null && season != null && type != null && material != null && washingNotes != null
    val imageToShowUri = remember(clothesToEdit, imageUriString) {
        when {
            // Bearbeiten-Modus und es gibt einen Pfad
            clothesToEdit?.imagePath?.isNotEmpty() == true -> File(clothesToEdit!!.imagePath).toUri()
            // Neu-Modus mit einer neuen URI von der Kamera
            imageUriString != null -> imageUriString.toUri()
            // Fallback
            else -> null
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (clothesIdToEdit != null) "Kleidung bearbeiten" else "Neues Kleidungsstück",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Foto neu aufnehmen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    // Erstelle das Clothes-Objekt, egal ob neu oder bearbeitet
                    val clothesItem = Clothes(
                        // Wenn wir bearbeiten, behalte die ID, sonst ist sie 0 (wird von DB auto-generiert)
                        id = clothesIdToEdit ?: clothesToEdit?.id ?: 0,
                        size = size!!,
                        seasonUsage = season!!,
                        type = type!!,
                        material = material!!,
                        clean = clothesToEdit?.clean ?: true, // Behalte den alten Status oder setze auf sauber
                        washingNotes = washingNotes!!,
                        // Der imagePath wird erst in Routes.kt final gesetzt!
                        imagePath = clothesToEdit?.imagePath ?: ""
                    )
                    // Rufe die vereinfachte onSave-Funktion auf
                    onSave(clothesItem)
                },
                enabled = isFormValid // Der Button ist nur klickbar, wenn alle Felder ausgefüllt sind
            ) {
                Text(if (clothesIdToEdit != null) "Änderungen speichern" else "Speichern")
            }
        }
    ) { innerPadding ->
        // Das eigentliche Formular-Layout
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AddNewClothesForm(
                //modifier = Modifier.padding(innerPadding),
                imageUri = imageToShowUri,
                size = size,
                onSizeChange = { size = it },
                season = season,
                onSeasonChange = { season = it },
                type = type,
                onTypeChange = { type = it },
                material = material,
                onMaterialChange = { material = it },
                washingNotes = washingNotes,
                onWashingNotesChange = { washingNotes = it }
            )
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AddNewClothesForm(
    imageUri: Uri?,
    size: Size?,
    onSizeChange: (Size) -> Unit,
    season: Season?,
    onSeasonChange: (Season) -> Unit,
    type: Type?,
    onTypeChange: (Type) -> Unit,
    material: Material?,
    onMaterialChange: (Material) -> Unit,
    washingNotes: WashingNotes?,
    onWashingNotesChange: (WashingNotes) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp) // Abstand oben, unten und seitlich
    ) {
        // --- BILD-VORSCHAU ---
        item {
            AsyncImage(
                model = imageUri ?: R.drawable.clothicon,
                contentDescription = "Neues Kleidungsstück",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                // Mit dieser Konfiguration ist FIT die richtige Wahl.
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.clothicon),
                error = painterResource(id = R.drawable.wardrobe2icon),
            )
        }

        // --- EINGABEFELDER ---

        item {
            EnumDropdown(
                "Größe",
                Size.entries,
                size,
                onSizeChange,

                )
        }
        item {
            EnumDropdown(
                "Saison",
                Season.entries,
                season,
                onSeasonChange,

                )
        }
        item {
            EnumDropdown(
                "Typ",
                Type.entries,
                type,
                onTypeChange,

                )
        }
        item {
            EnumDropdown(
                "Material",
                Material.entries,
                material,
                onMaterialChange,

                )
        }
        item {
            EnumDropdown(
                "Waschhinweise",
                WashingNotes.entries,
                washingNotes,
                onWashingNotesChange
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedOption?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.toString()) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreviewAddNewClothesScreen() {
    /*
    AddNewClothesScreen(
        imageUriString = "", // Leere URI für die Vorschau
        onSave = { newItem, imageUri ->
            // In der Vorschau passiert hier nichts.
            println("Preview Save: $newItem, Uri: $imageUri")
        },
        onNavigateBack = {},
        viewModel = ClothesViewModel(
            repository =
        ),
        clothesIdToEdit = null
    )

     */
}