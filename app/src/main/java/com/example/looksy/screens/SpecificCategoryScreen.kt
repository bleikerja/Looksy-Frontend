package com.example.looksy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.LooksyButton
import com.example.looksy.R
import com.example.looksy.allClothes
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Filter
import com.example.looksy.dataClassClones.Material
import com.example.looksy.dataClassClones.Season
import com.example.looksy.dataClassClones.Size
import com.example.looksy.dataClassClones.Type

var categoryClothes: List<Clothes> = emptyList()
val filter = Filter()

@Composable
fun ClothImage(cloth: Clothes, onClick: (Int) -> Unit){
    LooksyButton(
        onClick = { onClick(allClothes.indexOf(cloth)) },
        picture = {
            AsyncImage(
                model = cloth.imagePath,
                contentDescription = "Detailansicht des Kleidungsstücks",
                error = painterResource(id = R.drawable.clothicon)
            )
        },
        modifier = Modifier
            .height(200.dp)
            .shadow(10.dp, RoundedCornerShape(10))
            .background(Color.White, RoundedCornerShape(10))
            .fillMaxWidth(0.45f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificCategoryScreen(type: Type, onOpenDetails: (Int) -> Unit = {}, onGoBack: () -> Unit = {}){
    categoryClothes = filter.byType(type, allClothes.toMutableList()) //TODO take from backend
    val filteredClothes: MutableList<Clothes> = remember { categoryClothes.toMutableStateList() }
    var size by remember { mutableStateOf<Size?>(null) }
    var season by remember { mutableStateOf<Season?>(null) }
    var material by remember { mutableStateOf<Material?>(null) }

    Column(
        modifier = Modifier.padding(20.dp)
    ){
        TopAppBar(
            title = {
                Text(
                    type.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())){
            EnumDropdown(
                "Größe",
                categoryClothes.map { it.size }.distinct().sortedBy { it.ordinal },
                size,
                {
                    size = it
                    filterCloth(filteredClothes, size, season, material)
                },
                Modifier.width(300.dp)
            )
            EnumDropdown(
                "Saison",
                categoryClothes.map { it.seasonUsage }.distinct().sortedBy { it.ordinal },
                season,
                {
                    season = it
                    filterCloth(filteredClothes, size, season, material)
                },
                Modifier.width(300.dp)
            )
            EnumDropdown(
                "Material",
                categoryClothes.map { it.material }.distinct().sortedBy { it.ordinal },
                material,
                {
                    material = it
                    filterCloth(filteredClothes, size, season, material)
                },
                Modifier.width(300.dp)
            )
        }

        Button(onClick = {
            size = null
            season = null
            material = null
            filterCloth(filteredClothes, null, null, null)
        }) {
            Text("Zurücksetzen")
        }

        LazyVerticalGrid (
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
        ) {
            items(filteredClothes.size ) { i ->
                ClothImage(filteredClothes[i], onOpenDetails)
            }
        }
    }
}

fun filterCloth(filteredClothes: MutableList<Clothes>, size: Size?, season: Season?, material: Material?) {
    filteredClothes.clear()
    var newFilteredClothes = categoryClothes.toMutableList()
    if (size != null) newFilteredClothes = filter.bySize(size, newFilteredClothes)
    if (season != null) newFilteredClothes = filter.bySeason(season, newFilteredClothes)
    if (material != null) newFilteredClothes = filter.byMaterial(material, newFilteredClothes)
    filteredClothes.addAll(newFilteredClothes)
}

@Preview
@Composable
fun ScreenPreview(){
    SpecificCategoryScreen(Type.Pants)
}