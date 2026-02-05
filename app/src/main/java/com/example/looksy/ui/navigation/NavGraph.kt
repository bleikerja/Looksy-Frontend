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
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.Type
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.screens.AddNewClothesScreen
import com.example.looksy.ui.screens.CameraScreenPermission
import com.example.looksy.ui.screens.Category
import com.example.looksy.ui.screens.CategoryItems
import com.example.looksy.ui.screens.SavedOutfitsScreen
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
    val allOutfitsFromDb by outfitViewModel.allOutfits.collectAsState(initial = emptyList())
    val categoryItems =
        allClothesFromDb.filter { it.clean }.groupBy { it.type }.map { (type, items) ->
            CategoryItems(category = type, items = items)
        }
    
    var top by remember { mutableStateOf<Clothes?>(null) }
    var pants by remember { mutableStateOf<Clothes?>(null) }
    var jacket by remember { mutableStateOf<Clothes?>(null) }
    var skirt by remember { mutableStateOf<Clothes?>(null) }
    var dress by remember { mutableStateOf<Clothes?>(null) }

    // Zentraler Effekt zur Synchronisierung des Outfits mit der Datenbank.
    // Stellt sicher, dass gelöschte oder schmutzige Kleidung sofort aus dem Outfit entfernt wird.
    LaunchedEffect(allClothesFromDb) {
        top = top?.let { t -> allClothesFromDb.find { it.id == t.id && it.clean && it.type == Type.Tops } }
        pants = pants?.let { p -> allClothesFromDb.find { it.id == p.id && it.clean && it.type == Type.Pants } }
        jacket = jacket?.let { j -> allClothesFromDb.find { it.id == j.id && it.clean && it.type == Type.Jacket } }
        skirt = skirt?.let { s -> allClothesFromDb.find { it.id == s.id && it.clean && it.type == Type.Skirt } }
        dress = dress?.let { d -> allClothesFromDb.find { it.id == d.id && it.clean && it.type == Type.Dress } }

        // Falls das Outfit unvollständig wird (kein Oberteil/Kleid), generiere ein neues.
        if (allClothesFromDb.any { it.clean } && top == null && dress == null) {
            val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb)
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
            FullOutfitScreen(
                top = top,
                pants = pants,
                jacket = jacket,
                skirt = skirt,
                dress = dress,
                onClick = { clothesId ->
                    navController.navigate(Routes.Details.createRoute(clothesId))
                },
                onConfirm = { wornClothesList ->
                    // 1. Präferenzen erhöhen
                    outfitViewModel.incrementOutfitPreference(
                        top?.id,
                        dress?.id,
                        skirt?.id,
                        pants?.id,
                        jacket?.id
                    )
                    clothesViewModel.incrementClothesPreference(wornClothesList)

                    // 2. Kleidung als schmutzig markieren
                    val updatedClothesList = wornClothesList.map { it.copy(clean = false) }
                    clothesViewModel.updateAll(updatedClothesList)

                    // 3. Neues Outfit generieren (aus den verbleibenden sauberen Sachen)
                    val remainingClean = allClothesFromDb.filter { cloth ->
                        updatedClothesList.none { it.id == cloth.id } && cloth.clean 
                    }
                    val outfit = generateRandomOutfit(remainingClean, allOutfitsFromDb)
                    top = outfit.top
                    pants = outfit.pants
                    skirt = outfit.skirt
                    jacket = outfit.jacket
                    dress = outfit.dress
                },
                onWashingMachine = { navController.navigate(Routes.WashingMachine.route) },
                onGenerateRandom = {
                    val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb)
                    top = outfit.top
                    pants = outfit.pants
                    skirt = outfit.skirt
                    jacket = outfit.jacket
                    dress = outfit.dress
                },
                onCamera = { navController.navigate(Routes.Scan.createRoute(-1)) },
                onSave = {
                    val outfitToSave = Outfit(
                        dressId = dress?.id,
                        topsId = top?.id,
                        skirtId = skirt?.id,
                        pantsId = pants?.id,
                        jacketId = jacket?.id,
                        isSynced = false
                    )
                    outfitViewModel.insert(outfitToSave)
                }
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
                val message = context.getString(R.string.error_cannot_deselect_last_item)

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
                                navController.navigate(Routes.EditClothes.createRoute(editId, ""))
                            }
                        )
                    }
                }
            }
        }

        composable(
            route = Routes.Scan.route,
            arguments = listOf(navArgument(RouteArgs.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val clothesId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            CameraScreenPermission(
                onImageCaptured = { tempUri ->
                    val encodedUri = Uri.encode(tempUri.toString())
                    if(clothesId == -1 || clothesId == null){
                        navController.navigate(Routes.AddNewClothes.createRoute(encodedUri))
                    } else {
                        navController.navigate(Routes.EditClothes.createRoute(clothesId, encodedUri)) {
                            popUpTo(Routes.EditClothes.route) {
                                inclusive = true
                            }
                        }
                    }
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
            arguments = listOf(
                navArgument(RouteArgs.ID) { type = NavType.IntType },
                navArgument(RouteArgs.IMAGE_URI) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clothesId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            val encodedUriString = backStackEntry.arguments?.getString(RouteArgs.IMAGE_URI)
            val scope = rememberCoroutineScope()
            if (clothesId != null) {
                val context = LocalContext.current
                AddNewClothesScreen(
                    imageUriString = if(encodedUriString.isNullOrEmpty()) null else encodedUriString,
                    viewModel = clothesViewModel,
                    clothesIdToEdit = clothesId,
                    onSave = { updatedClothesData ->
                        if(!encodedUriString.isNullOrEmpty()){
                            val uriToSave = encodedUriString.toUri()
                            val permanentPath = saveImagePermanently(context, uriToSave)
                            if (permanentPath != null) {
                                val finalClothes = updatedClothesData.copy(imagePath = permanentPath)
                                clothesViewModel.update(finalClothes)
                                finalClothes.let {
                                    when (it.type) {
                                        Type.Tops -> if (top?.id == it.id) top = it
                                        Type.Pants -> if (pants?.id == it.id) pants = it
                                        Type.Jacket -> if (jacket?.id == it.id) jacket = it
                                        Type.Skirt -> if (skirt?.id == it.id) skirt = it
                                        Type.Dress -> if (dress?.id == it.id) dress = it
                                    }
                                }

                            }
                        } else{
                            clothesViewModel.update(updatedClothesData)
                        }
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
                    onEditImage = { navController.navigate(Routes.Scan.createRoute(clothesId)) }
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

        composable(route = Routes.SavedOutfits.route) {
            val savedOutfits by outfitViewModel.allOutfits.collectAsState(initial = emptyList())

            SavedOutfitsScreen(
                outfits = savedOutfits,
                allClothes = allClothesFromDb,
                onOutfitClick = { outfitId ->
                    navController.navigate(Routes.OutfitDetails.createRoute(outfitId))
                }
            )
        }

        composable(
            route = Routes.OutfitDetails.route,
            arguments = listOf(navArgument(RouteArgs.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            if (outfitId != null) {
                val outfitData by outfitViewModel.getOutfitById(outfitId)
                    .collectAsState(initial = null)

                outfitData?.let { outfit ->
                    val outfitTop = outfit.topsId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitPants = outfit.pantsId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitDress = outfit.dressId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitJacket = outfit.jacketId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitSkirt = outfit.skirtId?.let { id -> allClothesFromDb.find { it.id == id } }

                    FullOutfitScreen(
                        top = outfitTop,
                        pants = outfitPants,
                        jacket = outfitJacket,
                        skirt = outfitSkirt,
                        dress = outfitDress,
                        onClick = { clothesId ->
                            navController.navigate(Routes.Details.createRoute(clothesId))
                        },
                        onConfirm = { wornClothesList ->
                            val updatedClothesList = wornClothesList.map { it.copy(clean = false) }
                            clothesViewModel.updateAll(updatedClothesList)
                            navController.popBackStack()
                        },
                        onWashingMachine = { navController.navigate(Routes.WashingMachine.route) },
                        onGenerateRandom = { },
                        onCamera = { navController.navigate(Routes.Scan.createRoute(-1)) }
                    )
                }
            }
        }
    }
}
