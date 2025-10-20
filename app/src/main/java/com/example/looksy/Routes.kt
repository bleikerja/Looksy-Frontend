package com.example.looksy

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.looksy.screens.WashingMachineScreen
import kotlinx.coroutines.launch
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

    data object WashingMachine : Routes("washing_machine")
    data object Details : Routes("details/{${RouteArgs.ID}}") {
        fun createRoute(id: Int): String {
            return "details/$id"
        }
    }

    data object SpecificCategory : Routes("specific_category/{${RouteArgs.TYPE}}") {
        fun createRoute(type: String): String {
            val encodedPath = Uri.encode(type)
            return "specific_category/$encodedPath"
        }
    }

    data object AddNewClothes : Routes("add_new_clothes/{${RouteArgs.IMAGE_URI}}") {
        fun createRoute(imageUri: String): String {
            return "add_new_clothes/$imageUri"
        }
    }

    data object EditClothes : Routes("edit_clothes/{${RouteArgs.ID}}") {
        fun createRoute(id: Int) = "edit_clothes/$id"
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
    val categoryItems =
        allClothesFromDb.filter { it.clean }.groupBy { it.type }.map { (type, items) ->
            CategoryItems(category = type, items = items)
        }
    var top by remember { mutableStateOf<Clothes?>(null) }
    var pants by remember { mutableStateOf<Clothes?>(null) }
    var jacket by remember { mutableStateOf<Clothes?>(null) }
    var skirt by remember { mutableStateOf<Clothes?>(null) }
    var dress by remember { mutableStateOf<Clothes?>(null) }

    LaunchedEffect(allClothesFromDb) {
        if (allClothesFromDb.isNotEmpty() && top == null && dress == null) {
            val cleanClothes = allClothesFromDb.filter { it.clean }
            val searchForTops = listOf(true, false).random()
            var randomTop: Clothes? = null
            var randomDress: Clothes? = null
            if (searchForTops) {
                randomTop = cleanClothes.filter { it.type == Type.Tops }.randomOrNull()
            } else {
                randomDress = cleanClothes.filter { it.type == Type.Dress }.randomOrNull()
            }

            if (randomTop == null && randomDress == null) {
                if (searchForTops) {
                    randomDress = cleanClothes.filter { it.type == Type.Dress }.randomOrNull()
                } else {
                    randomTop = cleanClothes.filter { it.type == Type.Tops }.randomOrNull()
                }
            }

            val randomPants = cleanClothes.filter { it.type == Type.Pants }.randomOrNull()
            val randomSkirt = cleanClothes.filter { it.type == Type.Skirt }.randomOrNull()

            val randomJacket = cleanClothes.filter { it.type == Type.Jacket }.randomOrNull()
            val finalTop = randomTop
            val finalPants = randomPants
            var finalSkirt = randomSkirt
            val finalJacket = randomJacket
            val finalDress = randomDress

            if (finalDress != null) {
                finalSkirt = null
            }

            top = finalTop
            pants = finalPants
            skirt = finalSkirt
            jacket = finalJacket
            dress = finalDress
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        composable(Routes.Home.route) {
            val currentTop = top
            val currentPants = pants
            val currentJacket = jacket
            val currentSkirt = skirt
            val currentDress = dress

            if ((currentTop != null || currentDress != null) && (currentPants != null || currentSkirt != null)) {
                LaunchedEffect(allClothesFromDb, top, pants, skirt, jacket, dress) {
                    if (allClothesFromDb.isNotEmpty() && top == null && dress == null) {
                        // Rufe die neue Funktion auf, um das initiale Outfit zu setzen
                        generateRandomOutfit(allClothesFromDb) { newTop, newPants, newSkirt, newJacket, newDress ->
                            top = newTop
                            pants = newPants
                            skirt = newSkirt
                            jacket = newJacket
                            dress = newDress
                        }
                    }
                }
                FullOutfitScreen(
                    top = currentTop,
                    pants = currentPants,
                    jacket = currentJacket,
                    skirt = currentSkirt,
                    dress = currentDress,
                    onClick = { clothesId ->
                        navController.navigate(Routes.Details.createRoute(clothesId))
                    },
                    onConfirm = { wornClothesList ->
                        val updatedClothesList = wornClothesList.map { it.copy(clean = false) }
                        viewModel.updateAll(updatedClothesList)

                        val clothesForNewOutfit = allClothesFromDb.map { cloth ->
                            updatedClothesList.find { it.id == cloth.id } ?: cloth
                        }

                        generateRandomOutfit(clothesForNewOutfit) { newTop, newPants, newSkirt, newJacket, newDress ->
                            top = newTop
                            pants = newPants
                            skirt = newSkirt
                            jacket = newJacket
                            dress = newDress
                        }
                    },
                    onWashingMachine = { navController.navigate(Routes.WashingMachine.route) })
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = { navController.navigate(Routes.WashingMachine.route) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalLaundryService,
                            contentDescription = "Zur Waschmaschine"
                        )
                    }
                    Text("Füge Kleidung hinzu, um Outfits zu sehen!")
                }
            }
        }

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

        composable(
            route = Routes.SpecificCategory.route,
            arguments = listOf(navArgument(RouteArgs.TYPE) { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString(RouteArgs.TYPE)
            val type = encodedPath?.let { Uri.decode(it) }

            if (type != null) {
                SpecificCategoryScreen(
                    type = Type.valueOf(type),
                    viewModel = viewModel,
                    onOpenDetails = { index ->
                        val finalRoute = Routes.Details.createRoute(index)
                        navController.navigate(finalRoute)
                    },
                    onGoBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Routes.Details.route,
            arguments = listOf(navArgument(RouteArgs.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val clothesId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            if (clothesId != null) {
                val clothesData by viewModel.getClothesById(clothesId)
                    .collectAsState(initial = null)
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                clothesData?.let { cloth ->
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        ClothInformationScreen(
                            modifier = Modifier.padding(innerPadding),
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
                                        Type.Tops -> {
                                            top = it
                                        }

                                        Type.Pants -> {
                                            pants = it
                                        }

                                        Type.Jacket -> jacket = it
                                        Type.Skirt -> {
                                            skirt = it
                                            dress = null
                                        }

                                        Type.Dress -> {
                                            dress = it
                                            skirt = null
                                        }
                                    }
                                }
                                navController.navigate(Routes.Home.route) {
                                    popUpTo(Routes.Home.route) { inclusive = true }
                                }
                            },
                            onDeselectOutfit = {
                                var canNavigateBack = false
                                val message =
                                    "Du kannst nicht das letzte Ober- oder Unterteil ablegen!"
                                when (cloth.type) {
                                    Type.Tops -> {
                                        // Prevent deselecting top if no dress is selected
                                        if (dress == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            top = null
                                            canNavigateBack = true
                                        }
                                    }

                                    Type.Pants -> {
                                        // Prevent deselecting top if no dress is selected
                                        if (skirt == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            pants = null
                                            canNavigateBack = true
                                        }
                                    }

                                    Type.Jacket -> {
                                        jacket = null
                                        canNavigateBack = true
                                    }

                                    Type.Skirt -> {
                                        // Prevent deselecting top if no dress is selected
                                        if (pants == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            skirt = null
                                            canNavigateBack = true
                                        }
                                    }

                                    Type.Dress -> {
                                        // Prevent deselecting top if no dress is selected
                                        if (top == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            dress = null
                                            canNavigateBack = true
                                        }
                                    }
                                }
                                if (canNavigateBack) {
                                    navController.popBackStack()
                                }
                            },
                            onNavigateToEdit = { editId ->
                                navController.navigate(Routes.EditClothes.createRoute(editId))
                            }
                        )
                    }
                }
            }
        }

        composable(Routes.Scan.route) {
            CameraScreenPermission(
                onImageCaptured = { tempUri ->
                    val encodedUri = Uri.encode(tempUri.toString())
                    navController.navigate(Routes.AddNewClothes.createRoute(encodedUri))
                }
            )
        }

        composable(
            route = Routes.AddNewClothes.route,
            arguments = listOf(navArgument(RouteArgs.IMAGE_URI) { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUriString = backStackEntry.arguments?.getString(RouteArgs.IMAGE_URI)
            val context = LocalContext.current
            AddNewClothesScreen(
                imageUriString = encodedUriString,
                viewModel = viewModel, // ViewModel übergeben
                clothesIdToEdit = null, // Explizit sagen: Das ist der "NEU"-Modus
                onSave = { newClothesData ->
                    val uriToSave = encodedUriString?.toUri()
                    if (uriToSave != null) {
                        // Bild permanent speichern (nur bei neuen Bildern)
                        val permanentPath = saveImagePermanently(context, uriToSave)
                        if (permanentPath != null) {
                            val finalClothes = newClothesData.copy(imagePath = permanentPath)
                            viewModel.insert(finalClothes) // Wichtig: INSERT
                            // Zurück navigieren
                            navController.popBackStack(Routes.Home.route, false)
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EditClothes.route,
            arguments = listOf(navArgument(RouteArgs.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val clothesId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            val scope = rememberCoroutineScope()
            if (clothesId != null) {
                AddNewClothesScreen(
                    imageUriString = null,
                    viewModel = viewModel,
                    clothesIdToEdit = clothesId,
                    onSave = { updatedClothesData ->
                        viewModel.update(updatedClothesData)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onDelete = {
                        scope.launch {
                            // Rufe die neue suspend-Funktion auf
                            val clothesToDelete =
                                viewModel.getByIdDirect(clothesId) // Kein .value mehr!
                            if (clothesToDelete != null) {
                                viewModel.delete(clothesToDelete)
                            }
                            // Navigiere nach dem Löschen zurück zum Home-Screen
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Home.route) { inclusive = true }
                            }
                        }
                    },
                )
            }
        }

        composable(route = Routes.WashingMachine.route) {
            // Hole alle schmutzigen Kleidungsstücke aus der Datenbank
            val dirtyClothes = allClothesFromDb.filter { !it.clean }

            WashingMachineScreen(
                dirtyClothes = dirtyClothes,
                onNavigateBack = { navController.popBackStack() },
                onConfirmWashed = { washedClothes ->
                    // Markiere alle ausgewählten Teile als 'sauber'
                    washedClothes.forEach { cloth ->
                        val updatedCloth = cloth.copy(clean = true)
                        viewModel.update(updatedCloth)
                    }
                }
            )
        }

    }
}

private fun generateRandomOutfit(
    allClothes: List<Clothes>,
    onResult: (
        newTop: Clothes?,
        newPants: Clothes?,
        newSkirt: Clothes?,
        newJacket: Clothes?,
        newDress: Clothes?
    ) -> Unit
) {
    // Berücksichtige nur saubere Kleidung
    val cleanClothes = allClothes.filter { it.clean }

    // Deine bestehende Logik aus dem LaunchedEffect, hier wiederverwendet
    val searchForTops = listOf(true, false).random()
    var randomTop: Clothes? = null
    var randomDress: Clothes? = null

    if (searchForTops) {
        randomTop = cleanClothes.filter { it.type == Type.Tops }.randomOrNull()
    } else {
        randomDress = cleanClothes.filter { it.type == Type.Dress }.randomOrNull()
    }

    if (randomTop == null && randomDress == null) {
        if (searchForTops) {
            randomDress = cleanClothes.filter { it.type == Type.Dress }.randomOrNull()
        } else {
            randomTop = cleanClothes.filter { it.type == Type.Tops }.randomOrNull()
        }
    }

    val randomPants = cleanClothes.filter { it.type == Type.Pants }.randomOrNull()
    val randomSkirt = cleanClothes.filter { it.type == Type.Skirt }.randomOrNull()
    val randomJacket = cleanClothes.filter { it.type == Type.Jacket }.randomOrNull()

    // Finale Zuweisungslogik
    val finalTop = randomTop
    val finalPants = randomPants
    var finalSkirt = randomSkirt
    val finalJacket = randomJacket
    val finalDress = randomDress

    if (finalDress != null) {
        finalSkirt = null
    }

    onResult(finalTop, finalPants, finalSkirt, finalJacket, finalDress)
}

private fun saveImagePermanently(context: Context, imageUri: Uri): String? {
    val currentDate = Date()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDate)
    val fileName = "IMG_$timeStamp.jpg"
    val storageDir = File(context.filesDir, "images")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    val permanentFile = File(storageDir, fileName)
    try {
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(permanentFile).use { output ->
                input.copyTo(output)
            }
        }
        return permanentFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
