package com.example.looksy.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.ui.components.LooksyButton
import com.example.looksy.data.model.Clothes
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.theme.LooksyTheme
import kotlinx.coroutines.launch

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
    onWashingMachine: () -> Unit = {},
    onGenerateRandom: () -> Unit = {},
    onCamera: () -> Unit = {}
) {
    if ((top != null || dress != null) && (pants != null || skirt != null)) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(249, 246, 242))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Header(onNavigateBack = {},
                    onNavigateToRightIcon = { onWashingMachine() },
                    clothesData = null,
                    headerText = "Heutiges Outfit",
                    rightIconContentDescription = "Zur Waschmaschine",
                    rightIcon = Icons.Default.LocalLaundryService,
                    isFirstHeader = true)
                /*Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Dein heutiges Outfit",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onWashingMachine() }, modifier = Modifier.size(50.dp)) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Default.LocalLaundryService,
                            contentDescription = "Zur Waschmaschine"
                        )
                    }
                }
                */
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
            IconButton(
                onClick = onGenerateRandom,
                modifier = Modifier.padding(bottom = 16.dp).size(50.dp)
                    .align(Alignment.BottomStart)

            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle, // Gutes Icon für "Zufall"
                    contentDescription = "Zufälliges Outfit generieren",
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(modifier=Modifier.align(Alignment.BottomEnd).padding(16.dp).size(50.dp)
                ,onClick = {
                // Dieselbe Logik wie vorher im großen Button
                val wornClothes = listOfNotNull(top, pants, dress, jacket, skirt)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Schön, dass dir das Outfit gefällt und du es anziehst",
                        duration = SnackbarDuration.Short
                    )
                    onConfirm(wornClothes)
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Check, // Haken-Icon
                    contentDescription = "Outfit anziehen",
                    modifier = Modifier.fillMaxSize()
                )
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
                    imageVector = Icons.Default.Shuffle, // Gutes Icon für "Zufall"
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
            picture = { Icon(Icons.Default.Create, contentDescription = "") })
    }
}

/*
@Composable
private fun EmptyState(
    onAddClothesClick: () -> Unit,
    onChooseCategoryClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Füge Kleidung hinzu, um Outfits zu erstellen")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddClothesClick) {
                Text("Kleidung hinzufügen")
            }
            Button(onClick = onChooseCategoryClick) {
                Text("Kategorie auswählen")
            }
        }
    }
}
*/

@Preview(showBackground = true)
@Composable
fun FullOutfitPreview() {
    LooksyTheme {
        FullOutfitScreen(
            top = allClothes[2],
            pants = allClothes[1],
            skirt = allClothes[0],
            onClick = { }
        )
    }
}
