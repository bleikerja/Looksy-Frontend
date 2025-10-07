package com.example.looksy

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.looksy.ui.theme.LooksyTheme

@Composable
fun FullOutfitScreen(modifier: Modifier = Modifier){
    Column(modifier = modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Add Outfit", modifier = Modifier.align(Alignment.CenterHorizontally))
        OutfitPart(painterResource(id = R.drawable.shirt))
        OutfitPart(painterResource(id = R.drawable.jeans))
    }
}

@Composable
fun OutfitPart(image: Painter){
    Row {
        Image(
            modifier = Modifier
                .height(300.dp),
            painter = image,
            contentDescription = ""
        )
        LooksyButton(
            { changeOutfit() },
            Modifier.align(Alignment.CenterVertically),
            { Icon(Icons.Default.Create, contentDescription = "") }
        )
    }
}

fun changeOutfit(){

}

@Preview(showBackground = true)
@Composable
fun FullOutfitPreview() {
    LooksyTheme {
        FullOutfitScreen()
    }
}