package com.example.looksy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.ui.theme.LooksyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullOutfitScreen(
    top: Clothes? = null,
    pants: Clothes? = null,
    dress: Clothes? = null,
    jacket: Clothes? = null,
    skirt: Clothes? = null,
    onClick: (Int) -> Unit = {}
) {
    if (top == null && dress == null) {
        TODO("Trow Exeption: Du kannst nicht nackt losgehen")
    }
    if (pants == null && skirt == null) {
        TODO("Trow Exeption: Du kannst nicht nackt losgehen")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(249, 246, 242))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Dein heutiges Outfit",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier
            .fillMaxSize()
            .background(Color(249, 246, 242))
            .padding(16.dp),
            ){
            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color(249, 246, 242))
                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (top != null) {
                    OutfitPart(
                        imageResId = top.imagePath,
                        { onClick(top.id) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    OutfitPart(
                        imageResId = dress!!.imagePath,
                        { onClick(dress.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pants != null) {
                    OutfitPart(
                        imageResId = pants.imagePath,
                        onClick = { onClick(pants.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                else {
                    OutfitPart(
                        imageResId = skirt!!.imagePath,
                        onClick = { onClick(skirt.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (jacket != null || (skirt != null && pants != null)) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(249, 246, 242))
                    .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    if (jacket != null) {
                        OutfitPart(
                            imageResId = jacket.imagePath,
                            { onClick(jacket.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (skirt != null && pants != null) {
                        OutfitPart(
                            imageResId = skirt.imagePath,
                            { onClick(skirt.id) },
                            modifier = Modifier.weight(1f)
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
