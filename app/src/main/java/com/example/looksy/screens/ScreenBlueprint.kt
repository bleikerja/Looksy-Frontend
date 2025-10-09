package com.example.looksy.screens

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.example.looksy.NavigationFlow
import com.example.looksy.R
import com.example.looksy.Routes


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.looksy.NavHostContainer


// Datenklasse, um die Informationen für jedes Navigationselement zu bündeln.
data class NavItem(
    val label: String,
    val route: Routes,
    val iconResId: Int // Resource-ID für das Icon
)

@Composable
fun ScreenBlueprint(navController: NavHostController) {
    // Liste der Navigationselemente definieren.
    val navItems = listOf(
        Triple(Routes.ChoseClothes.route, "Chose Clothes", R.drawable.wardrobeicon),
        Triple(Routes.Home.route, "Home", R.drawable.clothicon),
        Triple(Routes.Scan.route, "Scan", R.drawable.cameraicon)
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
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
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
       NavHostContainer(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarOptikPreview() {
    // Eine Fake-Instanz für die Vorschau.
    val fakeNavFlow = remember { NavigationFlow() }
    //ScreenBlueprint(navFlow = fakeNavFlow)
}
