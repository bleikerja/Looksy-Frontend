package com.example.looksy

import android.content.Context
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
import com.example.looksy.dataClassClones.*
import com.example.looksy.ViewModels.ClothesViewModel
import com.example.looksy.dataClassClones.Type
import com.example.looksy.screens.AddNewClothesScreen
import com.example.looksy.screens.CameraScreenPermission
import com.example.looksy.screens.SpecificCategoryScreen
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface NavigationDestination {
    val route: String
}

object RouteArgs {
    var TYPE = "imageType"
    const val IMAGE_URI = "imageUri"
    const val ID = "id"
}

sealed class Routes(override val route: String) : NavigationDestination {
    data object Home : Routes("home")
    data object Scan : Routes("scan")
    data object ChoseClothes : Routes("chose clothes")
    data object Details : Routes("details/{${RouteArgs.ID}}") {
        fun createRoute(id: Int): String {
            return "details/$id"
        }
    }

    data object SpecificCategory : Routes("specific_category/{${RouteArgs.TYPE}}") {
        fun createRoute(type: String): String {
            // Wichtig: Pfade enthalten oft Slashes '/', die in URLs Probleme machen.
            // Wir müssen den Pfad kodieren, bevor wir ihn übergeben.
            val encodedPath = Uri.encode(type)
            return "specific_category/$encodedPath"
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

@Composable
fun NavHostContainer(
    navController: NavHostController, // <- Der offizielle Controller
    modifier: Modifier = Modifier,
    viewModel: ClothesViewModel
) {
    val allClothesFromDb by viewModel.allClothes.collectAsState(initial = emptyList())
    val categoryItems = allClothesFromDb.groupBy { it.type }.map { (type, items) ->
        CategoryItems(category = type, items = items)
    }

    var top by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.firstOrNull { it.type == Type.Tops }) }
    var pants by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.firstOrNull { it.type == Type.Pants }) }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        // Entspricht: Routes.Home
        composable(Routes.Home.route) {
            val currentTop = top
            val currentPants = pants
            if (currentTop != null && currentPants != null) {
                FullOutfitScreen(
                    top = currentTop,
                    pants = currentPants,
                    onClick = { clothesId ->
                        navController.navigate(Routes.Details.createRoute(clothesId))
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
                categoryItems = categoryItems,
                onClick = { type ->
                    val finalRoute = Routes.SpecificCategory.createRoute(type)
                    navController.navigate(finalRoute)
                },
                onButtonClicked = { itemId ->
                    navController.navigate(Routes.Details.createRoute(itemId))
                }
            )

        }

        // Entspricht: Routes.SpecificCategory
        composable(
            route = Routes.SpecificCategory.route,
            arguments = listOf(navArgument(RouteArgs.TYPE) { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString(RouteArgs.TYPE)
            val type = encodedPath?.let { Uri.decode(it) }

            if (type != null /* && clothesData != null */) {
                SpecificCategoryScreen(
                    type = Type.valueOf(type),
                    viewModel = viewModel,
                    onOpenDetails = { index ->
                        val finalRoute = Routes.Details.createRoute(index)
                        navController.navigate(finalRoute)
                    },
                    onGoBack = {
                        navController.navigate(Routes.ChoseClothes.route)
                    }
                )
            }
        }

        // Entspricht: Routes.Details
        composable(
            route = Routes.Details.route,
            arguments = listOf(navArgument(RouteArgs.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            // 1. Argument auslesen und dekodieren
            val clothesId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            if (clothesId != null) {
                val clothesData by viewModel.getClothesById(clothesId)
                    .collectAsState(initial = null)

                clothesData?.let { cloth ->
                    ClothInformationScreen(
                        clothesData = cloth,
                        viewModel = viewModel,
                        onNavigateToDetails = { newId ->
                            navController.navigate(Routes.Details.createRoute(newId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = { navController.popBackStack() },
                        onConfirmOutfit = { confirmedId ->
                            val selectedCloth = allClothesFromDb.find { it.id == confirmedId }
                            selectedCloth?.let {
                                when (it.type) {
                                    Type.Tops -> top = it
                                    Type.Pants -> pants = it
                                    else -> {}
                                }
                            }
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
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
    }
}

private fun saveImagePermanently(context: Context, imageUri: Uri): String? {
    // Die Zeile "val imageUri = Uri.parse(tempUri)" ist jetzt überflüssig.

    val currentDate = Date()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDate)
    val fileName = "IMG_$timeStamp.jpg"

    val storageDir = File(context.filesDir, "images")

    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }

    val permanentFile = File(storageDir, fileName)

    try {
        // Öffne einen Input-Stream direkt von der übergebenen URI
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val outputStream = FileOutputStream(permanentFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return permanentFile.absolutePath

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
