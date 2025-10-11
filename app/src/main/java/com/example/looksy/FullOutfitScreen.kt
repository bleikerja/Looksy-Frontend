package com.example.looksy

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.ui.theme.LooksyTheme

@Composable
fun FullOutfitScreen(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dein heutiges Outfit", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(32.dp))
        OutfitPart(imageResId = R.drawable.shirt_small)
        OutfitPart(imageResId = R.drawable.jeans)
    }

}
@Composable
fun OutfitPart(imageResId: Int) {
    Row {
        AsyncImage(
            model = imageResId,
            modifier = Modifier.height(300.dp),
            contentDescription = "Kleidungsstück",
        )
        LooksyButton(
            onClick = { changeOutfit() },
            modifier = Modifier.align(Alignment.CenterVertically),
            picture = { Icon(Icons.Default.Create, contentDescription = "") })
    }
}

fun changeOutfit() {
    /* wird benötigt beim aufrufen, des Bildes, aktuell weiß ich jedoch nicht was da für ein pfad hin muss
// Irgendwo in deinem FullOutfitScreen, wenn auf ein Bild geklickt wird:
    val pathToImage =
        "/data/user/0/com.example.looksy/files/IMG_20251009_103000.jpg" // Beispiel-Pfad aus saveImagePermanently

// Navigiere zum Details-Screen mit diesem Pfad
     */
}

@Preview(showBackground = true)
@Composable
fun FullOutfitPreview() {
    LooksyTheme {
        FullOutfitScreen()
    }
}