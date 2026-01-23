package com.example.looksy.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.looksy.ui.screens.CategoriesScreen
import com.example.looksy.ui.screens.ClothInformationScreen
import com.example.looksy.ui.screens.FullOutfitScreen
import com.example.looksy.R
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Type
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.screens.AddNewClothesScreen
import com.example.looksy.ui.screens.CameraScreenPermission
import com.example.looksy.ui.screens.Category
import com.example.looksy.ui.screens.CategoryItems
import com.example.looksy.ui.screens.SpecificCategoryScreen
import com.example.looksy.ui.screens.WashingMachineScreen
import com.example.looksy.ui.viewmodel.OutfitViewModel
import com.example.looksy.util.generateRandomOutfit
import com.example.looksy.util.saveImagePermanently
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    clothesViewModel: ClothesViewModel,
    outfitViewModel: OutfitViewModel
) {
    val allClothesFromDb by clothesViewModel.allClothes.collectAsState(initial = emptyList())
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
        if (allClothesFromDb.isEmpty()) {
            top = null
            pants = null
            jacket = null
            skirt = null
            dress = null
        } else if (top == null && dress == null) {
            val outfit = generateRandomOutfit(allClothesFromDb)
            top = outfit.top
            pants = outfit.pants
            skirt = outfit.skirt
            jacket = outfit.jacket
            dress = outfit.dress
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

            LaunchedEffect(allClothesFromDb, top, pants, skirt, jacket, dress) {
                if (allClothesFromDb.isNotEmpty() && top == null && dress == null) {
                    val outfit = generateRandomOutfit(allClothesFromDb)
                    top = outfit.top
                    pants = outfit.pants
                    skirt = outfit.skirt
                    jacket = outfit.jacket
                    dress = outfit.dress
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
                    outfitViewModel.incrementOutfitPreference(
                        currentTop?.id,
                        currentDress?.id,
                        currentSkirt?.id,
                        currentPants?.id,
                        currentJacket?.id
                    )
                    val updatedClothesList = wornClothesList.map { it.copy(clean = false) }
                    clothesViewModel.updateAll(updatedClothesList)

                    val clothesForNewOutfit = allClothesFromDb.map { cloth ->
                        updatedClothesList.find { it.id == cloth.id } ?: cloth
                    }

                    val outfit = generateRandomOutfit(clothesForNewOutfit)
                    top = outfit.top
                    pants = outfit.pants
                    skirt = outfit.skirt
                    jacket = outfit.jacket
                    dress = outfit.dress
                },
                onWashingMachine = { navController.navigate(Routes.WashingMachine.route) },
                onGenerateRandom = {
                    val outfit = generateRandomOutfit(allClothesFromDb)
                    top = outfit.top
                    pants = outfit.pants
                    skirt = outfit.skirt
                    jacket = outfit.jacket
                    dress = outfit.dress
                },
                onCamera = { navController.navigate(Routes.Scan.route) }
            )
        }

        composable(Routes.ChoseClothes.route) {
            val sampleCategories = listOf(
                Category("Shirt", R.drawable.shirt_category),
                Category("Pants", R.drawable.pants_category),
                Category("Glasses", R.drawable.glasses_category),
                Category("Shoes", R.drawable.shoes_category),
                Category("Watch", R.drawable.watch_category)
            )
            
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
                    viewModel = clothesViewModel,
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
                val clothesData by clothesViewModel.getClothesById(clothesId)
                    .collectAsState(initial = null)
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                clothesData?.let { cloth ->
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        ClothInformationScreen(
                            modifier = Modifier.padding(innerPadding),
                            clothesData = cloth,
                            viewModel = clothesViewModel,
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
                                val message = context.getString(R.string.error_cannot_deselect_last_item)
                                
                                when (cloth.type) {
                                    Type.Tops -> {
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
                viewModel = clothesViewModel,
                clothesIdToEdit = null,
                onSave = { newClothesData ->
                    val uriToSave = encodedUriString?.toUri()
                    if (uriToSave != null) {
                        val permanentPath = saveImagePermanently(context, uriToSave)
                        if (permanentPath != null) {
                            val finalClothes = newClothesData.copy(imagePath = permanentPath)
                            clothesViewModel.insert(finalClothes)
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
                    viewModel = clothesViewModel,
                    clothesIdToEdit = clothesId,
                    onSave = { updatedClothesData ->
                        clothesViewModel.update(updatedClothesData)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onDelete = {
                        scope.launch {
                            val clothesToDelete = clothesViewModel.getByIdDirect(clothesId)
                            if (clothesToDelete != null) {
                                clothesViewModel.delete(clothesToDelete)
                            }
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Home.route) { inclusive = true }
                            }
                        }
                    },
                )
            }
        }

        composable(route = Routes.WashingMachine.route) {
            val dirtyClothes = allClothesFromDb.filter { !it.clean }

            WashingMachineScreen(
                dirtyClothes = dirtyClothes,
                onNavigateBack = { navController.popBackStack() },
                onConfirmWashed = { washedClothes ->
                    washedClothes.forEach { cloth ->
                        val updatedCloth = cloth.copy(clean = true)
                        clothesViewModel.update(updatedCloth)
                    }
                }
            )
        }
    }
}
