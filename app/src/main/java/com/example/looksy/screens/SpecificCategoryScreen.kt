package com.example.looksy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.looksy.LooksyButton
import com.example.looksy.R
import com.example.looksy.Header
import com.example.looksy.presentation.viewmodel.ClothesViewModel
import com.example.looksy.model.Clothes
import com.example.looksy.model.Material
import com.example.looksy.model.Season
import com.example.looksy.model.Size
import com.example.looksy.model.Type

@Composable
fun ClothImage(cloth: Clothes, onClick: (Int) -> Unit) {
    LooksyButton(
        onClick = { onClick(cloth.id) },
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
fun SpecificCategoryScreen(
    type: Type,
    viewModel: ClothesViewModel,
    onOpenDetails: (Int) -> Unit = {},
    onGoBack: () -> Unit = {}
) {
    val categoryClothes by viewModel.getClothesByType(type).collectAsState(initial = emptyList())
    var size by remember { mutableStateOf<Size?>(null) }
    var season by remember { mutableStateOf<Season?>(null) }
    var material by remember { mutableStateOf<Material?>(null) }

    val filteredClothes = remember(categoryClothes, size, season, material) {
        categoryClothes.filter { cloth ->
            (size == null || cloth.size == size) &&
                    (season == null || cloth.seasonUsage == season) &&
                    (material == null || cloth.material == material)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Header(onNavigateBack = onGoBack,
            onNavigateToRightIcon = {},
            clothesData = null,
            headerText = type.toString(),
            rightIconContentDescription = null,
            rightIcon = null)

        Spacer(modifier = Modifier.height(20.dp))
//        TopAppBar(
//            title = {
//
//            },
//            navigationIcon = {
//
//            },
//            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//        )

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            EnumDropdown(
                "Größe",
                categoryClothes.map { it.size }.distinct().sortedBy { it.ordinal },
                size,
                { size = it },
                Modifier.width(300.dp)
            )
            EnumDropdown(
                "Saison",
                categoryClothes.map { it.seasonUsage }.distinct().sortedBy { it.ordinal },
                season,
                { season = it },
                Modifier.width(300.dp)
            )
            EnumDropdown(
                "Material",
                categoryClothes.map { it.material }.distinct().sortedBy { it.ordinal },
                material,
                { material = it },
                Modifier.width(300.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            size = null
            season = null
            material = null
        }) {
            Text("Zurücksetzen")
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth()
        ) {
            items(filteredClothes) { cloth ->
                ClothImage(cloth, onOpenDetails)
            }
        }
    }
}

@Preview
@Composable
fun ScreenPreview() {
    //SpecificCategoryScreen(Type.Pants)
}
