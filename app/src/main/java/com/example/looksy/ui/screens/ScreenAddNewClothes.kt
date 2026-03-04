package com.example.looksy.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.looksy.R
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.ClothesColor
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.ui.components.ConfirmationDialog
import com.example.looksy.ui.components.MultiSelectDropdown
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewClothesScreen(
    imageUriString: String?,
    viewModel: ClothesViewModel,
    onSave: (newItem: Clothes) -> Unit,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    clothesIdToEdit: Int? = null,
    onEditImage: () -> Unit = {},
    onCropPhoto: () -> Unit = {}
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
    var color by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.color) }
    var washingNotes by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.washingNotes ?: emptyList()) }
    var clean by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.clean ?: true) }
    var brand by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.brand ?: "") }
    var comment by remember(clothesToEdit) { mutableStateOf(clothesToEdit?.comment ?: "") }

    // Reset size when switching between shoe and non-shoe to avoid incompatible sizes staying
    val previousTypeIsShoe = remember { mutableStateOf(clothesToEdit?.type == Type.Shoes) }
    LaunchedEffect(type) {
        val currentTypeIsShoe = type == Type.Shoes
        if (currentTypeIsShoe != previousTypeIsShoe.value) {
            size = null
            previousTypeIsShoe.value = currentTypeIsShoe
        }
    }

    val isFormValid =
                size != null && season != null && type != null && washingNotes.isNotEmpty()
    var edited by remember { mutableStateOf(false) }

    val imageToShowUri = remember(clothesToEdit, imageUriString) {
        when {
            // Neu-Modus mit einer neuen URI von der Kamera
            imageUriString != null -> imageUriString.toUri()
            // Bearbeiten-Modus und es gibt einen Pfad
            clothesToEdit?.imagePath?.isNotEmpty() == true -> File(clothesToEdit!!.imagePath).toUri()
            // Fallback
            else -> null
        }
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBackDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if(edited) {
            showBackDialog = true
        } else {
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog (
            title = "Löschen bestätigen",
            text = "Möchtest du dieses Kleidungsstück wirklich endgültig löschen?",
            dismissText = "Abbrechen",
            onDismiss = { showDeleteDialog = false },
            confirmText = "Löschen",
            isDeletion = true,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
    if (showBackDialog) {
        ConfirmationDialog (
            title = "Zurück?",
            text = "Möchtest du wirklich zurück? Änderungen gehen verloren!",
            dismissText = "Nein",
            onDismiss = { showBackDialog = false },
            confirmText = "Ja",
            isDeletion = false,
            onConfirm = {
                onNavigateBack()
                showBackDialog = false
            }
        )
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { Header(
            onNavigateBack = {
                if(edited) {
                    showBackDialog = true
                } else {
                    onNavigateBack()
                }
            },
            onNavigateToRightIcon = { _ -> showDeleteDialog = true },
            clothesData = clothesToEdit,
            headerText = if (clothesIdToEdit != null) "Bearbeiten" else "Hinzufügen",
            rightIconContentDescription = if (clothesIdToEdit != null) "Löschen" else null,
            rightIcon = if (clothesIdToEdit != null) Icons.Default.Delete else null,
            rightIconSize = 0.7F,
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
                        material = material,
                        color = color,
                        brand = brand.takeIf { it.isNotBlank() },
                        comment = comment.takeIf { it.isNotBlank() },
                        clean = clean, // Behalte den alten Status oder setze auf sauber
                        washingNotes = washingNotes,
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
                onEditImage = { onEditImage(); edited = true },
                onEditPhoto = { onCropPhoto(); edited = true },
                type = type,
                onTypeChange = { type = it; edited = true },
                size = size,
                onSizeChange = { size = it; edited = true },
                brand = brand,
                onBrandChange = { brand = it; edited = true },
                season = season,
                onSeasonChange = { season = it; edited = true },
                color = color,
                onColorChange = { color = it; edited = true },
                material = material,
                onMaterialChange = { material = it; edited = true },
                washingNotes = washingNotes,
                onWashingNotesChange = { note ->
                    washingNotes = if (washingNotes.contains(note)) {
                        washingNotes - note
                    } else {
                        washingNotes + note
                    }
                    edited = true
                },
                comment = comment,
                onCommentChange = { comment = it; edited = true },
                clean = clean,
                onCleanChange = { clean = it; edited = true },
                edit = (clothesIdToEdit != null)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AddNewClothesForm(
    imageUri: Uri?,
    onEditImage: () -> Unit,
    onEditPhoto: () -> Unit = {},
    type: Type?,
    onTypeChange: (Type) -> Unit,
    size: Size?,
    onSizeChange: (Size) -> Unit,
    brand: String,
    onBrandChange: (String) -> Unit,
    season: Season?,
    onSeasonChange: (Season) -> Unit,
    color: ClothesColor?,
    onColorChange: (ClothesColor?) -> Unit,
    material: Material?,
    onMaterialChange: (Material?) -> Unit,
    washingNotes: List<WashingNotes>,
    onWashingNotesChange: (WashingNotes) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    clean: Boolean,
    onCleanChange: (Boolean) -> Unit,
    edit: Boolean,
    modifier: Modifier = Modifier
) {
    val halfScreenHeight = (LocalConfiguration.current.screenHeightDp / 3).dp

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // --- BILD-VORSCHAU ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = halfScreenHeight)
            ) {
                AsyncImage(
                    model = imageUri ?: R.drawable.clothicon,
                    contentDescription = "Neues Kleidungsstück",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = halfScreenHeight)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.clothicon),
                    error = painterResource(id = R.drawable.wardrobe2icon),
                )

                // Camera button (always shown)
                IconButton(
                    onClick = { onEditImage() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp),
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Zur Kamera"
                    )
                }

                // Edit / crop button (only when a photo exists)
                if (imageUri != null) {
                    IconButton(
                        onClick = { onEditPhoto() },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Bearbeiten"
                        )
                    }
                }
            }
        }

        // --- EINGABEFELDER ---

        // 1. Typ
        item {
            EnumDropdown(
                label = "Typ",
                options = Type.entries,
                selectedOption = type,
                onOptionSelected = onTypeChange,
            )
        }

        // 2. Größe — only enabled after type is chosen; list depends on type
        item {
            val sizeOptions = if (type == Type.Shoes) Size.shoeSizes else Size.standardSizes
            EnumDropdown(
                label = if (type == null) "Größe (erst Typ wählen)" else "Größe",
                options = sizeOptions,
                selectedOption = size,
                onOptionSelected = onSizeChange,
                enabled = type != null,
            )
        }

        // 3. Marke (optional free-text)
        item {
            TextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("Marke (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        // 4. Saison
        item {
            EnumDropdown(
                label = "Saison",
                options = Season.entries,
                selectedOption = season,
                onOptionSelected = onSeasonChange,
            )
        }

        // 5. Farbe (optional)
        item {
            OptionalEnumDropdown(
                label = "Farbe (optional)",
                options = ClothesColor.entries,
                selectedOption = color,
                onOptionSelected = onColorChange,
                optionDisplayName = { it.displayName }
            )
        }

        // 6. Material (optional)
        item {
            OptionalEnumDropdown(
                label = "Material (optional)",
                options = Material.entries,
                selectedOption = material,
                onOptionSelected = onMaterialChange,
                optionDisplayName = { it.displayName }
            )
        }

        // 7. Waschhinweise
        item {
            MultiSelectDropdown(
                label = "Waschhinweise",
                options = WashingNotes.entries,
                selectedOptions = washingNotes,
                onOptionSelected = { note ->
                    onWashingNotesChange(note)
                },
            )
        }

        // 8. Kommentar (optional free-text)
        item {
            TextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("Kommentar (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
            )
        }

        // Sauberkeit (nur im Bearbeitungs-Modus)
        if (edit) {
            item {
                var expanded by remember { mutableStateOf(false) }
                val options = listOf("Sauber", "Schmutzig")
                val selectedText = if (clean) "Sauber" else "Schmutzig"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedText,
                        onValueChange = {},
                        label = { Text("Sauberkeit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    onCleanChange(selectionOption == "Sauber")
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            enabled = enabled,
            value = selectedOption?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
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

/** Dropdown that allows no selection: first option "—" sets null. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OptionalEnumDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    optionDisplayName: (T) -> String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = selectedOption?.let { optionDisplayName(it) } ?: "—"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = displayValue,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("—") },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionDisplayName(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
