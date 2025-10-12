package com.example.looksy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.ui.theme.LooksyTheme

@Composable
fun FullOutfitScreen(modifier: Modifier = Modifier,
                     top: Clothes,
                     pants:Clothes,
                     onClick: (Int) ->Unit = {}) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dein heutiges Outfit", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(32.dp))
        OutfitPart(imageResId = top.imagePath, { onClick(allClothes.indexOf(top)) })
        OutfitPart(imageResId = pants.imagePath, { onClick(allClothes.indexOf(pants)) })
    }

}
@Composable
fun OutfitPart(imageResId: Any?, onClick: ()-> Unit) {
    Row {
        AsyncImage(
            model = imageResId,
            modifier = Modifier.height(300.dp),
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
            onClick = { }
        )
    }
}