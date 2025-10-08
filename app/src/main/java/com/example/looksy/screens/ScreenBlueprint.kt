package com.example.looksy.screens

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.looksy.NavHostContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


// Datenklasse, um die Informationen für jedes Navigationselement zu bündeln.
data class NavItem(
    val label: String,
    val route: Routes,
    val iconResId: Int // Resource-ID für das Icon
)

@Composable
fun ScreenBlueprint(navFlow: NavigationFlow) {
    // Liste der Navigationselemente definieren.
    val navItems = listOf(
        NavItem("Chose your Clothes", Routes.ChoseClothes, R.drawable.wardrobeicon),
        NavItem("Home", Routes.Home, R.drawable.clothicon),
        NavItem("Scan", Routes.Scan, R.drawable.cameraicon)
    )

    // Den aktuellen Navigationspfad aus dem Flow beobachten.
    val currentDestination by navFlow.destination.collectAsState()

    // Das Scaffold ist ein Grundgerüst für Bildschirme mit App-Bars, Navigation etc.
    Scaffold(
        // Hier wird die untere Navigationsleiste platziert.
        bottomBar = {
            NavigationBar {
                navItems.forEach { navItem ->
                    NavigationBarItem(
                        // Prüfen, ob das Element zum aktuellen Ziel gehört.
                        selected = currentDestination == navItem.route,
                        onClick = {
                            // Navigation zum neuen Ziel auslösen.
                            navFlow.navigate(navItem.route)
                        },
                        label = null,
                        icon = {
                            Icon(
                                painter = painterResource(id = navItem.iconResId),
                                contentDescription = navItem.label,
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
        NavHostContainer(navFlow = navFlow, modifier = Modifier.padding(innerPadding))
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarOptikPreview() {
    // Eine Fake-Instanz für die Vorschau.
    val fakeNavFlow = remember { NavigationFlow() }
    ScreenBlueprint(navFlow = fakeNavFlow)
}

// Wichtig: Füge Platzhalter-Icons (z.B. profile_icon.xml, settings_icon.xml)
// zu deinem `res/drawable`-Ordner hinzu, damit die Vorschau funktioniert.
// Du kannst dafür die eingebauten Vektor-Assets von Android Studio nutzen.
