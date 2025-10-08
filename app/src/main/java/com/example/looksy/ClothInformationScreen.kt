package com.example.looksy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.looksy.ui.theme.LooksyTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClothInformationScreen(image: Painter, color: String, type: String, material: String, size: String, season: String, status: String){
    Column {
        Image(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            painter = image,
            contentDescription = ""
        )

        Text("Information", fontSize = 30.sp)
        FlowRow(modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),) {
            Information("Color", color)
            Information("Type", type)
            Information("Material", material)
            Information("Size", size)
            Information("Season", season)
            Information("Status", status)
        }
    }
}

@Composable
fun Information(name: String, value: String){
    Column(modifier = Modifier.border(2.dp, Color.Black, shape = RoundedCornerShape(20))
        .fillMaxWidth(0.45f)
        .background(Color(240, 220, 189), shape = RoundedCornerShape(20))
        .padding(5.dp)
    )
        {
        Text(name, fontSize = 10.sp, color = Color.DarkGray)
        Text(value, fontSize = 25.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ClothInformationPreview() {
    LooksyTheme {
        ClothInformationScreen(painterResource(id = R.drawable.shirt), "Red", "shirt", "wool", "M", "Summer", "clean")
        //ToDo: Get informaton in fun ClothInformationScreen from Backend
    }
}