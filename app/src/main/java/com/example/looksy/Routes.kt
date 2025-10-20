package com.example.looksy

import android.content.Context
import android.net.Uri
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
    var jacket by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.firstOrNull { it.type == Type.Jacket }) }
    var skirt by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.firstOrNull { it.type == Type.Skirt }) }
    var dress by remember(allClothesFromDb) { mutableStateOf(allClothesFromDb.firstOrNull { it.type == Type.Dress }) }

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
                FullOutfitScreen(
                    top = currentTop,
                    pants = currentPants,
                    jacket = currentJacket,
                    skirt = currentSkirt,
                    dress = currentDress,
                    onClick = { clothesId ->
                        navController.navigate(Routes.Details.createRoute(clothesId))
                    })
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                            dress = null
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
                                            top = null
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
                                        canNavigateBack = true}
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
                                if(canNavigateBack) {
                                    navController.popBackStack()
                                }
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
            if (encodedUriString != null) {
                val context = LocalContext.current
                AddNewClothesScreen(
                    imageUriString = encodedUriString,
                    onSave = { newClothesData, _ ->
                        val uriToSave = encodedUriString.toUri()
                        val permanentPath = saveImagePermanently(context, uriToSave)
                        if (permanentPath != null) {
                            val finalClothes = newClothesData.copy(imagePath = permanentPath)
                            viewModel.insert(finalClothes)
                            navController.navigate(Routes.Home.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onRetakePhoto = { navController.navigate(Routes.Scan.route) }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
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
