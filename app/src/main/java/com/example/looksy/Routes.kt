package com.example.looksy

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.looksy.screens.AddNewClothesScreen
import com.example.looksy.screens.CameraScreenPermission
import com.example.looksy.screens.saveImagePermanently

interface NavigationDestination{
    val route:String
}
object RouteArgs {
    const val IMAGE_URI = "imageUri"
    const val IMAGE_PATH = "imagePath"
}

sealed class Routes(override val route: String): NavigationDestination{
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

@Composable
fun NavHostContainer(
    navController: NavHostController, // <- Der offizielle Controller
    modifier: Modifier = Modifier
) {
    // Es gibt nur noch den NavHost. Der `when`-Block und `navFlow` sind komplett weg.
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route, // Dein Startbildschirm
        modifier = modifier
    ) {
        // Entspricht: Routes.Home
        composable(Routes.Home.route) {
            FullOutfitScreen()
        }

        // Entspricht: Routes.ChoseClothes
        composable(Routes.ChoseClothes.route) {
            // Dein ChoseClothesScreen kommt hierher
            // Placeholder:
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chose Clothes Screen") }
        }

        // Entspricht: Routes.Details
        composable(
            route = Routes.Details.route,
            arguments = listOf(navArgument(RouteArgs.IMAGE_PATH) { type = NavType.StringType })
        ) { backStackEntry ->
            // 1. Argument auslesen und dekodieren
            val encodedPath = backStackEntry.arguments?.getString(RouteArgs.IMAGE_PATH)
            val imagePath = encodedPath?.let { Uri.decode(it) }

            // 2. Daten für diesen Pfad laden (aus einer Datenbank oder ViewModel)
            // val clothesData = viewModel.getClothesByPath(imagePath)

            // Wenn wir die Daten haben, rufen wir den Screen auf
            if (imagePath != null /* && clothesData != null */) {
                ClothInformationScreen(
                    imagePath = imagePath, // <<< HIER IST DEIN KAMERABILD-PFAD!
                    color = "Rot", // clothesData.color
                    type = "Shirt", // clothesData.type
                    material = "Wolle", // clothesData.material
                    // ... etc.
                    size = "M",
                    season = "Sommer",
                    status = "sauber"
                )
            }
        }

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
                // val viewModel: ClothesViewModel = viewModel()

                AddNewClothesScreen(
                    imageUriString = encodedUriString, // Die Uri wird direkt übergeben
                    onSave = { newClothesData, imageUriStr ->
                        // Deine Speicherlogik bleibt identisch
                        val uriToSave = encodedUriString.toUri()
                        val permanentPath = saveImagePermanently(context, uriToSave)
                        if (permanentPath != null) {
                            val finalClothes = newClothesData.copy(imagePath = permanentPath)
                            // viewModel.insert(finalClothes)

                            // Navigiere zurück zum Home-Screen
                            navController.navigate(Routes.Home.route)
                        }
                    },
                    onRetakePhoto = { navController.navigate(Routes.Scan.route) }
                )
            } else {
                // Sicherheits-Fallback: Wenn die Uri fehlt, gehe einfach zurück.
                navController.popBackStack()
            }
        }
    }
}