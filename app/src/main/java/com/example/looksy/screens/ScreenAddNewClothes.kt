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
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Material
import com.example.looksy.dataClassClones.Season
import com.example.looksy.dataClassClones.Size
import com.example.looksy.dataClassClones.Type
import com.example.looksy.dataClassClones.WashingNotes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewClothesScreen(
    imageUriString: String,
    onSave: (newItem: Clothes, imageUri: Uri) -> Unit,
    onRetakePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Zustände für alle Formularfelder
    var size by remember { mutableStateOf<Size?>(null) }
    var season by remember { mutableStateOf<Season?>(null) }
    var type by remember { mutableStateOf<Type?>(null) }
    var material by remember { mutableStateOf<Material?>(null) }
    var washingNotes by remember { mutableStateOf<WashingNotes?>(null) }

    val isFormValid =
        size != null && season != null && type != null && material != null && washingNotes != null
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Neues Kleidungsstück",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onRetakePhoto) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Foto neu aufnehmen"
                        )
                    }
                },
                // Transparenter Hintergrund passt besser zum Design
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    // Erstelle das Clothes-Objekt, wenn das Formular gültig ist
                    val newItem = Clothes(
                        size = size!!,
                        seasonUsage = season!!,
                        type = type!!,
                        material = material!!,
                        clean = true,
                        washingNotes = washingNotes!!
                    )
                    val imageUri = try {
                        imageUriString.toUri()
                    } catch (e: IllegalArgumentException) {
                        // Gib eine leere/ungültige URI zurück, falls der String leer ist.
                        // Das sollte in der echten App nie passieren, sichert aber die Preview ab.
                        Uri.EMPTY
                    }

                    // Rufe onSave nur auf, wenn die URI gültig ist.
                    if (imageUri != Uri.EMPTY) {
                        onSave(newItem, imageUri)
                    }
                },
                enabled = isFormValid // Der Button ist nur klickbar, wenn alle Felder ausgefüllt sind
            ) {
                Text("Speichern")
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
                imageUriString = imageUriString,
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
    imageUriString: String,
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
                model = imageUriString.ifEmpty { R.drawable.clothicon },
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


@Preview(showBackground = true)
@Composable
fun PreviewAddNewClothesScreen() {
    AddNewClothesScreen(
        imageUriString = "", // Leere URI für die Vorschau
        onSave = { newItem, imageUri ->
            // In der Vorschau passiert hier nichts.
            println("Preview Save: $newItem, Uri: $imageUri")
        },
        onRetakePhoto = {}
    )
}