package com.example.looksy

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.looksy.ViewModels.ClothesViewModel
import com.example.looksy.dataClassClones.Type
import com.example.looksy.screens.AddNewClothesScreen
import com.example.looksy.screens.CameraScreenPermission
import com.example.looksy.screens.saveImagePermanently

interface NavigationDestination {
    val route: String
}

object RouteArgs {
    const val IMAGE_URI = "imageUri"
    const val IMAGE_PATH = "imagePath"
}

sealed class Routes(override val route: String) : NavigationDestination {
    data object Home : Routes("home")
    data object Scan : Routes("scan")
    data object ChoseClothes : Routes("chose clothes")
    data object Details : Routes("details/{${RouteArgs.IMAGE_PATH}}") {
        fun createRoute(imagePath: String): String {
            // Wichtig: Pfade enthalten oft Slashes '/', die in URLs Probleme machen.
            // Wir müssen den Pfad kodieren, bevor wir ihn übergeben.
            val encodedPath = Uri.encode(imagePath)
            return "details/$encodedPath"
        }
    }

    // Ziel mit Argument. Der String definiert den Pfad und den Platzhalter für das Argument.
    data object AddNewClothes : Routes("add_new_clothes/{${RouteArgs.IMAGE_URI}}") {
        // Hilfsfunktion, um die vollständige Route sicher zu erstellen
        fun createRoute(imageUri: String): String {
            return "add_new_clothes/$imageUri"
        }
    }
}

val sampleCategories = listOf(
    Category("Shirt", R.drawable.shirt_category),
    Category("Pants", R.drawable.pants_category),
    Category("Glasses", R.drawable.glasses_category),
    Category("Shoes", R.drawable.shoes_category),
    Category("Watch", R.drawable.watch_category)
)
val sampleItems1 = listOf(
    Item("Black T-shirt", R.drawable.black_t_shirt),
    Item("Grey T-shirt", R.drawable.white_t_shirt)
)

val sampleItems2 = listOf(
    Item("Orange Cardigan", R.drawable.orange_cardigan),
    Item("Colorful Sweater", R.drawable.colorful_sweater)
)
val sampleCategoryItems = listOf(
    CategoryItems("T-shirts", sampleItems1),
    CategoryItems("Sweaters", sampleItems2)
)

@Composable
fun NavHostContainer(
    navController: NavHostController, // <- Der offizielle Controller
    modifier: Modifier = Modifier,
    viewModel: ClothesViewModel
) {
    val allClothesFromDb by viewModel.allClothes.collectAsState()
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        // Entspricht: Routes.Home
        composable(Routes.Home.route) {
            if (allClothesFromDb.count { it.type == Type.Tops } >= 1 &&
                allClothesFromDb.count { it.type == Type.Pants } >= 1) {

                var top by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.first { it.type == Type.Tops }) }
                var pants by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.first { it.type == Type.Pants }) }

                FullOutfitScreen(
                    modifier = modifier,
                    top = top,
                    pants = pants,
                    onClick = { imagePath ->
                        val finalRoute = Routes.Details.createRoute(imagePath)
                        navController.navigate(finalRoute)
                    })
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Füge Kleidung hinzu, um Outfits zu sehen!")
                }
            }
        }

        // Entspricht: Routes.ChoseClothes
        composable(Routes.ChoseClothes.route) {
            CategoriesScreen(
                categories = sampleCategories,
                categoryItems = sampleCategoryItems,
                navBar = { }
            )
        }

        // Entspricht: Routes.Details
        composable(
            route = "details/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            // 1. Argument auslesen und dekodieren
            val clothesId = backStackEntry.arguments?.getInt("id")
            if (clothesId != null) {
                val clothesData by viewModel.getClothesById(clothesId).collectAsState(initial = null)

                clothesData?.let {
                    ClothInformationScreen(
                        clothesData = it,
                        viewModel = viewModel,
                        onNavigateToDetails = { newId ->
                            navController.navigate("details/$newId") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
        /*
        composable("details/{id}", ...) { backStackEntry ->
            val clothesId = backStackEntry.arguments?.getInt("id")
            if (clothesId != null) {
                // Wir holen das EINE Kleidungsstück direkt vom ViewModel
                val clothesData by viewModel.getClothesById(clothesId).collectAsState(initial = null)

                // Zeige den Screen an, sobald das Item geladen ist.
                clothesData?.let {
                    ClothInformationScreen(
                        clothesData = it,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
         */

        // Entspricht: Routes.Scan
        composable(Routes.Scan.route) {
            CameraScreenPermission(
                onImageCaptured = { tempUri ->
                    // 1. Kodieren der Uri für eine sichere URL-Übergabe
                    val encodedUri = Uri.encode(tempUri.toString())
                    // 2. Navigation zum AddNewClothes-Screen mit der Uri als Argument
                    navController.navigate(Routes.AddNewClothes.createRoute(encodedUri))
                }
            )
        }

        // Entspricht: is Routes.AddNewClothes
        composable(
            route = Routes.AddNewClothes.route,
            arguments = listOf(navArgument(RouteArgs.IMAGE_URI) { type = NavType.StringType })
        ) { backStackEntry ->
            // Hier holen wir das Argument sicher aus dem Navigations-Aufruf
            val encodedUriString = backStackEntry.arguments?.getString(RouteArgs.IMAGE_URI)

            if (encodedUriString != null) {
                val context = LocalContext.current
                val viewModel: ClothesViewModel = viewModel

                AddNewClothesScreen(
                    imageUriString = encodedUriString, // Die Uri wird direkt übergeben
                    onSave = { newClothesData, imageUriStr ->
                        // Deine Speicherlogik bleibt identisch
                        val uriToSave = encodedUriString.toUri()
                        val permanentPath = saveImagePermanently(context, uriToSave)
                        if (permanentPath != null) {
                            val finalClothes = newClothesData.copy(imagePath = permanentPath)
                            viewModel.insert(finalClothes)

                            // Navigiere zurück zum Home-Screen
                            navController.navigate(Routes.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop
                            }
                        }
                    },
                    onRetakePhoto = { navController.navigate(Routes.Scan.route) }
                )
            } else {
                // Sicherheits-Fallback: Wenn die Uri fehlt, gehe einfach zurück.
                navController.popBackStack()
            }
        }
        /*
        composable(Routes.AddNewClothes.route, ...) {
            // ...
            AddNewClothesScreen(
                // ...
                onSave = { newClothesData, imageUriStr ->
                    // ... (deine Logik zum Speichern des Bildes) ...
                    if (permanentPath != null) {
                        val finalClothes = newClothesData.copy(imagePath = permanentPath)

                        // ===============================================
                        // HIER PASSIERT DIE VERKNÜPFUNG BEIM SPEICHERN
                        viewModel.insert(finalClothes) // Ruft die Methode im ViewModel auf
                        // ===============================================

                        // ... (deine Navigationslogik zurück zu Home) ...
                    }
                },
                // ...
            )
        }
         */
    }
}