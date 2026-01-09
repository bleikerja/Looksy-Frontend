package com.example.looksy.screens

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import com.example.looksy.R
import com.example.looksy.presentation.navigation.Routes


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.looksy.application.ClothesApplication
import com.example.looksy.di.ClothesViewModelFactory
import com.example.looksy.presentation.navigation.NavGraph
import com.example.looksy.presentation.viewmodel.ClothesViewModel

@Composable
fun ScreenBlueprint(navController: NavHostController) {
    // Liste der Navigationselemente definieren.
    val navItems = listOf(
        Triple(Routes.ChoseClothes.route, "Chose Clothes", R.drawable.wardrobeicon),
        Triple(Routes.Home.route, "Home", R.drawable.clothicon),
        Triple(Routes.Scan.route, "Scan", R.drawable.cameraicon)
    )
    val application = LocalContext.current.applicationContext as ClothesApplication
    val viewModelClothes: ClothesViewModel = viewModel(
        factory = ClothesViewModelFactory(application.repository)
    )
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
                            navController.navigate(route){
                                    popUpTo(navController.graph.startDestinationId) {
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
       NavGraph(navController = navController, modifier = Modifier.padding(innerPadding), viewModel=viewModelClothes)
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarOptikPreview() {

}
