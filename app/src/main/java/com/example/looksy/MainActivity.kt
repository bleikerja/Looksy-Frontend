package com.example.looksy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.looksy.ui.screens.ScreenBlueprint
import com.example.looksy.ui.theme.LooksyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LooksyTheme {
                val navController = rememberNavController()
                ScreenBlueprint(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    LooksyTheme {
        //ClothInformationScreen(0)
        //ImagePath:painterResource(id = R.drawable.shirt_small)
    }
}