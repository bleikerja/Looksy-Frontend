package com.example.looksy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.looksy.screens.AddNewClothesScreen
import com.example.looksy.screens.CameraScreenPermission

interface NavigationDestination{
    val route:String
}

sealed class Routes(override val route: String): NavigationDestination{
    data object Home : Routes("home")
    data object Scan : Routes("scan")
    data object ChoseClothes : Routes("chose clothes")
    data object Details : Routes("details")
    data class AddNewClothes(val imageUri: String) : Routes("addNewClothes")
}

@Composable
fun NavHostContainer(
    navFlow: NavigationFlow,
    modifier: Modifier = Modifier
) {
    val currentDestination by navFlow.destination.collectAsState()
    when (val destination = currentDestination) {
        Routes.Home -> FullOutfitScreen(modifier = modifier)
        Routes.Details -> ClothInformationScreen(painterResource(id = R.drawable.shirt), "Red", "shirt", "wool", "M", "Summer", "clean")
        Routes.Scan -> CameraScreenPermission {  }
        is Routes.Scan -> {
            CameraScreenPermission(
                onImageCaptured = { uri ->
                    navFlow.navigate(Routes.AddNewClothes(imageUri = uri.toString()))
                }
            )
        }
        is Routes.AddNewClothes -> {
            AddNewClothesScreen(
                imageUriString = destination.imageUri,
                onSave = { newItem, imageUri ->
                    navFlow.navigate(Routes.Home)
                }
            )
        }
    }
}