package com.example.looksy

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocalLaundryService
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.dataClassClones.Clothes
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
) {
    if (top == null && dress == null) {
        throw NotImplementedError("Trow Exeption: Du kannst nicht nackt losgehen")
    }
    if (pants == null && skirt == null) {
        throw NotImplementedError("Trow Exeption: Du kannst nicht nackt losgehen")
    }
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
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Dein heutiges Outfit",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onWashingMachine() }) {
                    Icon(
                        imageVector = Icons.Default.LocalLaundryService,
                        contentDescription = "Zur Waschmaschine"
                    )
                }
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
        Button(
            onClick = {
                val wornClothes = listOfNotNull(top, pants, dress, jacket, skirt)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Schön, dass dir das Outfit gefällt und du es anziehst",
                        duration = SnackbarDuration.Short
                    )
                    onConfirm(wornClothes)
                }
            },
            content = { Text("Outfit anziehen") },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Positioniert ihn unten in der Mitte der Box
                .padding(bottom = 12.dp)      // Gibt ihm etwas Abstand vom unteren Rand
                .fillMaxWidth(0.5f)
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
fun OutfitPart(imageResId: Any?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
