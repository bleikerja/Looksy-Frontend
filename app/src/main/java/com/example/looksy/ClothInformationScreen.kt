package com.example.looksy

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Material
import com.example.looksy.dataClassClones.Season
import com.example.looksy.dataClassClones.Size
import com.example.looksy.dataClassClones.Type
import com.example.looksy.dataClassClones.WashingNotes
import coil.compose.AsyncImage
import com.example.looksy.ui.theme.LooksyTheme

//ToDo: Get informaton in fun ClothInformationScreen from Backend
//just from same type
var allClothes = listOf(
    Clothes(
        size = Size._46,
        seasonUsage = Season.Winter,
        type = Type.Pants,
        material = Material.Wool,
        clean = true,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}"
    ),
    Clothes(
        size = Size._46,
        seasonUsage = Season.Summer,
        type = Type.Pants,
        material = Material.jeans,
        clean = true,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}"
    ),
    Clothes(
        size = Size._M,
        seasonUsage = Season.inBetween,
        type = Type.Tops,
        material = Material.Wool,
        clean = true,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.colorful_sweater}"
    )
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClothInformationScreen(selectedClothIndex: Int) {
    var currentClothIndex by remember { mutableIntStateOf(selectedClothIndex) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(249, 246, 242))
            .padding(30.dp)
    ) {
        //ToDo: painterResource auf die Aktuellen Begebenheiten anpassen
        ClothImage(
            allClothes[currentClothIndex].imagePath, modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Text("Information", fontSize = 30.sp, modifier = Modifier.align(Alignment.Start))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            //TODO: add Color to Cloth class
            Information("Color", "-")
            Information("Type", allClothes[currentClothIndex].type.toString())
            Information("Material", allClothes[currentClothIndex].material.toString())
            Information("Size", allClothes[currentClothIndex].size.toString())
            Information("Season", allClothes[currentClothIndex].seasonUsage.toString())
            Information("Status", if (allClothes[currentClothIndex].clean) "clean" else "dirty")
        }

        Text(
            "other ${allClothes[currentClothIndex].type}",
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .height(200.dp)
        ) {
            for (i in allClothes.indices) {
                LooksyButton(
                    onClick = { currentClothIndex = i },
                    picture = {
                        AsyncImage(
                            model = allClothes[i].imagePath,
                            contentDescription = "Detailansicht des Kleidungsstücks",
                            error = painterResource(id = R.drawable.clothicon)
                        )
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)
                        .shadow(10.dp, RoundedCornerShape(10))
                        .background(Color.White, RoundedCornerShape(10))
                )
            }
        }
    }
}

@Composable
fun Information(name: String, value: String) {
    Column(
        modifier = Modifier
            .shadow(10.dp, RoundedCornerShape(20))
            .fillMaxWidth(0.45f)
            .background(Color.White, shape = RoundedCornerShape(20))
            .padding(5.dp)
    )
    {
        Text(name, fontSize = 10.sp, color = Color.DarkGray)
        Text(value, fontSize = 25.sp)
    }
}

@Composable
fun ClothImage(image: Any?, modifier: Modifier) {
    AsyncImage(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(10))
            .background(Color.White, RoundedCornerShape(10)),
        model = image,
        contentDescription = "Detailansicht des Kleidungsstücks",
        error = painterResource(id = R.drawable.clothicon)
    )
}

@Preview(showBackground = true)
@Composable
fun ClothInformationPreview() {
    LooksyTheme {
        ClothInformationScreen(1)
        //ToDo: Get informaton in fun ClothInformationScreen from Backend
    }
}