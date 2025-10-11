package com.example.looksy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

interface NavigationDestination{
    val route:String
}

sealed class Routes(override val route: String): NavigationDestination{
    data object Home : Routes("home")
    data object Scan : Routes("scan")
    data object ChoseClothes : Routes("chose clothes")
    data object Details : Routes("details")
}

@Composable
fun NavHostContainer(
    navFlow: NavigationFlow,
    modifier: Modifier = Modifier // Dieser Modifier enthält das wichtige Padding
) {
    val currentDestination by navFlow.destination.collectAsState()
    when (currentDestination) {
        Routes.Home -> FullOutfitScreen(modifier = modifier)
        Routes.Details -> ClothInformationScreen(0)
    }
}