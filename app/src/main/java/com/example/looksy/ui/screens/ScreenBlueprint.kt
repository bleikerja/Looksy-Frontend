package com.example.looksy.ui.screens

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import com.example.looksy.R
import com.example.looksy.ui.navigation.Routes


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.looksy.LooksyApplication
import com.example.looksy.ui.components.ConfirmationDialog
import com.example.looksy.ui.viewmodel.ClothesViewModelFactory
import com.example.looksy.ui.viewmodel.OutfitViewModelFactory
import com.example.looksy.ui.navigation.NavGraph
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.viewmodel.OutfitViewModel

@Composable
fun ScreenBlueprint(navController: NavHostController) {
    // Liste der Navigationselemente definieren.
    val navItems = listOf(
        Triple(Routes.ChoseClothes.route, "Chose Clothes", R.drawable.wardrobeicon),
        Triple(Routes.Home.route, "Home", R.drawable.clothicon),
        Triple(Routes.Scan.createRoute(-1), "Scan", R.drawable.cameraicon),
        Triple(Routes.SavedOutfits.route, "Saved Outfits", R.drawable.heart)
    )
    val application = LocalContext.current.applicationContext as LooksyApplication
    val viewModelClothes: ClothesViewModel = viewModel(
        factory = ClothesViewModelFactory(application.repository)
    )
    val viewModelOutfit: OutfitViewModel = viewModel(
        factory = OutfitViewModelFactory(application.outfitRepository)
    )
    var nextRoute by remember { mutableStateOf(Routes.Home.route) }
    var showBackDialog by remember { mutableStateOf(false) }
    if (showBackDialog) {
        ConfirmationDialog (
            title = "Achtung!",
            text = "mögliche Änderungen gehen verloren!",
            dismissText = "Abbrechen",
            onDismiss = { showBackDialog = false },
            confirmText = "Weiter",
            onConfirm = {
                navController.navigate(nextRoute){
                    popUpTo(navController.graph.startDestinationId) {
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                showBackDialog = false
            }
        )
    }
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { (route, label, iconResId) ->
                    NavigationBarItem(
                        // Prüfen, ob das Element zum aktuellen Ziel gehört.
                        selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                        onClick = {
                            // Navigation zum neuen Ziel auslösen.
                            if(currentDestination?.route != Routes.EditClothes.route){
                                navController.navigate(route){
                                    popUpTo(navController.graph.startDestinationId) {
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }else {
                                nextRoute = route
                                showBackDialog = true
                            }
                        },
                        label = null,
                        icon = {
                            Icon(
                                painter = painterResource(id = iconResId),
                                contentDescription = label,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        // Farben optional anpassen für einen besseren Look.
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            clothesViewModel=viewModelClothes,
            outfitViewModel = viewModelOutfit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarOptikPreview() {

}
