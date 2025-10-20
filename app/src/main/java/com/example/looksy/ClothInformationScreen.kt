package com.example.looksy

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.looksy.ViewModels.ClothesViewModel
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Material
import com.example.looksy.dataClassClones.Season
import com.example.looksy.dataClassClones.Size
import com.example.looksy.dataClassClones.Type
import com.example.looksy.dataClassClones.WashingNotes
import com.example.looksy.ui.theme.LooksyTheme

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
fun ClothInformationScreen(
    modifier: Modifier = Modifier,
    clothesData: Clothes,
    viewModel: ClothesViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onConfirmOutfit: (Int) -> Unit,
    onDeselectOutfit: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(249, 246, 242))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Zurück zur Outfitansicht",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            Text(
                "Details",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onNavigateToEdit(clothesData.id) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Bearbeiten",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
        ClothImage(
            clothesData.imagePath, modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Text("Informationen", fontSize = 30.sp, modifier = Modifier.align(Alignment.Start))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Information("Waschhinweise", clothesData.washingNotes.displayName)
            Information("Typ", clothesData.type.displayName)
            Information("Material", clothesData.material.displayName)
            Information("Größe", clothesData.size.displayName)
            Information("Saison", clothesData.seasonUsage.displayName)
            Information("Status", if (clothesData.clean) "sauber" else "schmutzig")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {onDeselectOutfit(clothesData.id)}){
                Text("Aus Outfit entfernen")
            }
            Button(onClick = { onConfirmOutfit(clothesData.id) }) {
                Text("${clothesData.type} auswählen")
            }
        }
        Text(
            "siehe auch",
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        val similarClothes by viewModel
            .getClothesByType(clothesData.type)
            .collectAsState(initial = emptyList())

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .height(200.dp)
        ) {
            similarClothes.filter { it.id != clothesData.id }.forEach { item ->
                SimilarClothCard(
                    clothes = item,
                    onClick = { onNavigateToDetails(item.id) }
                )
            }
        }
    }
}

@Composable
fun SimilarClothCard(
    clothes: Clothes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LooksyButton(
        onClick = onClick,
        picture = {
            AsyncImage(
                model = clothes.imagePath,
                contentDescription = "Detailansicht des Kleidungsstücks",
                error = painterResource(id = R.drawable.clothicon)
            )
        },
        modifier = modifier
            .width(200.dp)
            .height(200.dp)
            .shadow(10.dp, RoundedCornerShape(10))
            .background(Color.White, RoundedCornerShape(10))
    )
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
        //ClothInformationScreen(1)
        //ToDo: Get informaton in fun ClothInformationScreen from Backend
    }
}
