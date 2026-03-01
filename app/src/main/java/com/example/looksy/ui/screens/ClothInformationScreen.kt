package com.example.looksy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.looksy.ui.components.LooksyButton
import com.example.looksy.R
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.ui.components.Header
import kotlin.math.floor

//just from same type

var allClothes = listOf(
    Clothes(
        size = Size._46,
        seasonUsage = Season.Winter,
        type = Type.Pants,
        material = Material.Wool,
        clean = true,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}"
    ),
    Clothes(
        size = Size._46,
        seasonUsage = Season.Summer,
        type = Type.Pants,
        material = Material.jeans,
        clean = true,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}"
    ),
    Clothes(
        size = Size._M,
        seasonUsage = Season.inBetween,
        type = Type.Tops,
        material = Material.Wool,
        clean = true,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.colorful_sweater}"
    )
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClothInformationScreen(
    modifier: Modifier = Modifier,
    clothesData: Clothes,
    viewModel: ClothesViewModel,
    onMoveToWashingMachine: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onConfirmOutfit: (Int) -> Unit,
    isInOutfit: Boolean,
    onDeselectOutfit: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            Header(
                onNavigateBack= onNavigateBack,
                onNavigateToRightIcon = { id ->
                    if (id != null) {
                        onNavigateToEdit(id)
                    }
                },
                clothesData = clothesData,
                headerText = "Details",
                rightIconContentDescription = "Bearbeiten",
                rightIcon = Icons.Default.Edit,
                rightIconSize = 0.7F
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .background(Color(249, 246, 242))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
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
                    WaschingInformation("Waschhinweise", clothesData.washingNotes)
                    Information("Typ", clothesData.type.displayName)
                    Information("Material", clothesData.material.displayName)
                    Information("Farbe", clothesData.color?.displayName ?: "—")
                    Information("Größe", clothesData.size.displayName)
                    Information("Saison", clothesData.seasonUsage.displayName)
                    Information(
                        "Status",
                        if (clothesData.clean) {
                            if (clothesData.wornSince == null && clothesData.daysWorn == 0) {
                                "sauber"
                            } else {
                                var daysWorn = clothesData.daysWorn
                                if (clothesData.wornSince != null) daysWorn += floor(((System.currentTimeMillis() - clothesData.wornSince) / (1000 * 60 * 60 * 24)).toDouble()).toInt() + 1
                                "$daysWorn Tag" + if (daysWorn < 2) "" else "e"
                            }
                        } else "schmutzig",
                        modifier = if (clothesData.clean && clothesData.daysWorn != 0 && !clothesData.selected) Modifier.fillMaxWidth(
                            0.3f
                        ) else Modifier
                    )

                    if (clothesData.clean && clothesData.daysWorn != 0 && !clothesData.selected) {
                        IconButton(
                            onClick = {
                                onMoveToWashingMachine()
                                if (clothesData.selected) {
                                    onDeselectOutfit(clothesData.id)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.15f)
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalLaundryService,
                                contentDescription = "zu Waschmaschine hinzufügen",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onDeselectOutfit(clothesData.id) },
                        enabled = isInOutfit,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color.Black,
                            disabledContentColor = Color.White
                        )
                    ) {
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
                    similarClothes.filter { it.clean && it.id != clothesData.id }.forEach { item ->
                        SimilarClothCard(
                            clothes = item,
                            onClick = { onNavigateToDetails(item.id) }
                        )
                    }
                }
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
fun Information(
    name: String,
    value: String,
    modifier: Modifier = Modifier,
    valueFontSize: TextUnit = 25.sp
) {
    Column(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(20))
            .fillMaxWidth(0.45f)
            .background(Color.White, shape = RoundedCornerShape(20))
            .padding(5.dp)
    )
    {
        Text(name, fontSize = 10.sp, color = Color.DarkGray)
        Text(
            text = value,
            fontSize = valueFontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

@Composable
fun WaschingInformation(name: String, washingNotes: List<WashingNotes>?) {
    var showDialog by remember { mutableStateOf(false) }
    val safeNotes = washingNotes?.filterNotNull() ?: emptyList()
    
    if (safeNotes.isNotEmpty()) {
        val hasMoreThanOne = safeNotes.size > 1
        val displayText = if (hasMoreThanOne) {
            "${safeNotes[0].displayName} ..."
        } else {
            safeNotes[0].displayName
        }

        Information(
            name = name,
            value = displayText,
            // Verkleinerte Schriftgröße bei mehreren Hinweisen, damit "..." sichtbar bleibt
            valueFontSize = if (hasMoreThanOne) 18.sp else 22.sp,
            modifier = Modifier.clickable { showDialog = true }
        )

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Schließen"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Scrollbare Liste für die Waschhinweise
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            safeNotes.forEach { note ->
                                Text(
                                    text = note.displayName,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClothInformationPreview() {
    LooksyTheme {
        //ClothInformationScreen(1)
        //ToDo: Get informaton in fun ClothInformationScreen from Backend
    }
}
