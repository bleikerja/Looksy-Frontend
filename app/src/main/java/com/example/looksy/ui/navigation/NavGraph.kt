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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.looksy.LooksyApplication
import com.example.looksy.data.location.PermissionState
import com.example.looksy.ui.screens.CategoriesScreen
import com.example.looksy.ui.screens.ClothInformationScreen
import com.example.looksy.ui.screens.FullOutfitScreen
import com.example.looksy.ui.screens.WeatherScreen
import com.example.looksy.R
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.Type
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.viewmodel.GeocodingViewModel
import com.example.looksy.ui.viewmodel.GeocodingViewModelFactory
import com.example.looksy.ui.viewmodel.WeatherViewModel
import com.example.looksy.ui.screens.AddNewClothesScreen
import com.example.looksy.ui.screens.EditPictureScreen
import com.example.looksy.ui.screens.CameraScreen
import com.example.looksy.ui.screens.Category
import com.example.looksy.ui.screens.CategoryItems
import com.example.looksy.ui.screens.DiscardScreen
import com.example.looksy.ui.screens.SavedOutfitsScreen
import com.example.looksy.ui.screens.SpecificCategoryScreen
import com.example.looksy.ui.screens.WashingMachineScreen
import com.example.looksy.ui.viewmodel.OutfitViewModel
import com.example.looksy.util.generateRandomOutfit
import com.example.looksy.util.saveImagePermanently
import kotlinx.coroutines.launch
import kotlin.math.floor
import androidx.compose.ui.res.stringResource

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    clothesViewModel: ClothesViewModel,
    outfitViewModel: OutfitViewModel,
    weatherViewModel: WeatherViewModel
) {
    val allClothesFromDb by clothesViewModel.allClothes.collectAsState(initial = emptyList())
    val allOutfitsFromDb by outfitViewModel.allOutfits.collectAsState(initial = emptyList())
    val categoryItems =
        allClothesFromDb.filter { it.clean }.groupBy { it.type }.map { (type, items) ->
            CategoryItems(category = type, items = items)
        }

    var topId by remember { mutableStateOf<Int?>(null) }
    var pantsId by remember { mutableStateOf<Int?>(null) }
    var jacketId by remember { mutableStateOf<Int?>(null) }
    var skirtId by remember { mutableStateOf<Int?>(null) }
    var dressId by remember { mutableStateOf<Int?>(null) }
    
    // Track permission and location state for weather
    var permissionState by remember { mutableStateOf(PermissionState.NOT_ASKED) }
    var isLocationEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(allClothesFromDb) {
        if (listOfNotNull(topId, pantsId, jacketId, skirtId, dressId).isEmpty()){
            topId = allClothesFromDb.find { it.type == Type.Tops && it.selected }?.id
            pantsId = allClothesFromDb.find { it.type == Type.Pants && it.selected }?.id
            jacketId = allClothesFromDb.find { it.type == Type.Jacket && it.selected }?.id
            skirtId = allClothesFromDb.find { it.type == Type.Skirt && it.selected }?.id
            dressId = allClothesFromDb.find { it.type == Type.Dress && it.selected }?.id
        }

        if (allClothesFromDb.isNotEmpty() && topId == null && dressId == null) {
            val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb)
            topId = outfit.top?.id
            pantsId = outfit.pants?.id
            skirtId = outfit.skirt?.id
            jacketId = outfit.jacket?.id
            dressId = outfit.dress?.id
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        composable(Routes.Home.route) {

            val application = LocalContext.current.applicationContext as LooksyApplication
            val weatherState by weatherViewModel.weatherState.collectAsState()
            val scope = rememberCoroutineScope()

            // Update permission and location state
            LaunchedEffect(Unit) {
                if (application.locationProvider.hasLocationPermission()) {
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE
                    isLocationEnabled = application.locationProvider.isLocationEnabled()
                } else {
                    permissionState = PermissionState.NOT_ASKED
                }
            }

            // Fetch weather on launch if location permission is granted and location is on
            LaunchedEffect(Unit) {
                if (application.locationProvider.hasLocationPermission()) {
                    isLocationEnabled = application.locationProvider.isLocationEnabled()
                    if (isLocationEnabled) {
                        application.locationProvider.getCurrentLocation().onSuccess { location ->
                            weatherViewModel.fetchWeather(location.latitude, location.longitude)
                        }
                    }
                }
            }

            FullOutfitScreen(
                top = getClothById(allClothesFromDb, topId ?: -1),
                pants = getClothById(allClothesFromDb, pantsId ?: -1),
                jacket = getClothById(allClothesFromDb, jacketId ?: -1),
                skirt = getClothById(allClothesFromDb, skirtId ?: -1),
                dress = getClothById(allClothesFromDb, dressId ?: -1),
                weatherState = weatherState,
                permissionState = permissionState,
                isLocationEnabled = isLocationEnabled,
                onWeatherClick = { navController.navigate(Routes.Weather.route) },
                onClick = { clothesId ->
                    navController.navigate(Routes.Details.createRoute(clothesId))
                },
                onConfirm = { wornClothesList ->
                    // 1. Kleidung als ausgewählt markieren
                    val updatedClothesList = wornClothesList.map { it.copy(wornSince = System.currentTimeMillis(), selected = true) }
                    clothesViewModel.updateAll(updatedClothesList)

                    // 2. Präferenzen erhöhen
                    clothesViewModel.incrementClothesPreference(updatedClothesList)
                    outfitViewModel.incrementOutfitPreference(
                        topId,
                        dressId,
                        skirtId,
                        pantsId,
                        jacketId
                    )
                    /*
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
                     */
                },
                onMoveToWashingMachine = { dirtyClothesList, cleanClothesList ->
                    topId = null
                    pantsId = null
                    jacketId = null
                    skirtId = null
                    dressId = null
                    val updatedDirtyClothesList = dirtyClothesList.map { it.copy(selected = false, clean = false) }
                    val updatedCleanClothesList = cleanClothesList.map { it.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(it)) }
                    clothesViewModel.updateAll(updatedDirtyClothesList + updatedCleanClothesList)
                },
                onWashingMachine = { navController.navigate(Routes.WashingMachine.route) },
                onGenerateRandom = {
                    clothesViewModel.updateAll(allClothesFromDb.map { it.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(it)) })
                    val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb)
                    topId = outfit.top?.id
                    pantsId = outfit.pants?.id
                    skirtId = outfit.skirt?.id
                    jacketId = outfit.jacket?.id
                    dressId = outfit.dress?.id
                },
                onCamera = { navController.navigate(Routes.Scan.createRoute(-1)) },
                onSave = {
                    val outfitToSave = Outfit(
                        dressId = dressId,
                        topsId = topId,
                        skirtId = skirtId,
                        pantsId = pantsId,
                        jacketId = jacketId,
                        isSynced = false,
                        isManuelSaved = true
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
                },
                onNavigateToDiscard = {
                    navController.navigate(Routes.Discard.route)
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
                val message = stringResource(R.string.error_cannot_deselect_last_item)

                clothesData?.let { cloth ->
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        ClothInformationScreen(
                            modifier = Modifier.padding(innerPadding),
                            clothesData = cloth,
                            viewModel = clothesViewModel,
                            onMoveToWashingMachine = {
                                val updatedCloth = cloth.copy(clean = false)
                                clothesViewModel.update(updatedCloth)
                                if(topId == cloth.id) topId = null
                                if(pantsId == cloth.id) pantsId = null
                                if(jacketId == cloth.id) jacketId = null
                                if(skirtId == cloth.id) skirtId = null
                                if(dressId == cloth.id) dressId = null
                            },
                            onNavigateToDetails = { newId ->
                                navController.navigate(Routes.Details.createRoute(newId)) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = { navController.popBackStack() },
                            onConfirmOutfit = { confirmedId ->
                                val selectedCloth = getClothById(allClothesFromDb, confirmedId)
                                val prevCloth = allClothesFromDb.find { it.type == selectedCloth?.type && it.selected }
                                if(prevCloth != null && prevCloth.id != selectedCloth?.id) {
                                    clothesViewModel.update(prevCloth.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(prevCloth)))
                                }

                                selectedCloth?.let {
                                    when (it.type) {
                                        Type.Tops -> topId = it.id
                                        Type.Pants -> pantsId = it.id
                                        Type.Jacket -> jacketId = it.id
                                        Type.Skirt -> {
                                            skirtId = it.id
                                            dressId = null
                                        }
                                        Type.Dress -> {
                                            dressId = it.id
                                            skirtId = null
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
                                        if (dressId == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            topId = null
                                            canNavigateBack = true
                                        }
                                    }
                                    Type.Pants -> {
                                        if (skirtId == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            pantsId = null
                                            canNavigateBack = true
                                        }
                                    }
                                    Type.Jacket -> {
                                        jacketId = null
                                        canNavigateBack = true
                                    }
                                    Type.Skirt -> {
                                        if (pantsId == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            skirtId = null
                                            canNavigateBack = true
                                        }
                                    }
                                    Type.Dress -> {
                                        if (topId == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        } else {
                                            dressId = null
                                            canNavigateBack = true
                                        }
                                    }
                                }
                                if (canNavigateBack) {
                                    if(cloth.selected) clothesViewModel.update(cloth.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(cloth)))
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
            CameraScreen (
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

            // Read cropped URI returned by BearbeitenScreen via SavedStateHandle
            val croppedUriFromEditor by backStackEntry.savedStateHandle
                .getStateFlow(RouteArgs.CROPPED_URI, "")
                .collectAsState()

            // Active URI: start with the route arg; override when BearbeitenScreen delivers a crop
            var activeUriString by remember { mutableStateOf(encodedUriString) }
            LaunchedEffect(croppedUriFromEditor) {
                if (croppedUriFromEditor.isNotEmpty()) {
                    activeUriString = croppedUriFromEditor
                }
            }

            AddNewClothesScreen(
                imageUriString = activeUriString,
                viewModel = clothesViewModel,
                clothesIdToEdit = null,
                onSave = { newClothesData ->
                    val uriToSave = activeUriString?.toUri()
                    if (uriToSave != null) {
                        val permanentPath = saveImagePermanently(context, uriToSave)
                        if (permanentPath != null) {
                            val finalClothes = newClothesData.copy(imagePath = permanentPath)
                            clothesViewModel.insert(finalClothes)
                            navController.popBackStack(Routes.Home.route, false)
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onEditPhoto = {
                    activeUriString?.let { uri ->
                        navController.navigate(Routes.EditPicture.createRoute(uri))
                    }
                }
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

                // Read cropped URI returned by BearbeitenScreen via SavedStateHandle
                val croppedUriFromEditor by backStackEntry.savedStateHandle
                    .getStateFlow(RouteArgs.CROPPED_URI, "")
                    .collectAsState()

                // Active URI: start with the route arg; override when BearbeitenScreen delivers a crop
                var activeUriString by remember {
                    mutableStateOf(if (encodedUriString.isNullOrEmpty()) null else encodedUriString)
                }
                LaunchedEffect(croppedUriFromEditor) {
                    if (croppedUriFromEditor.isNotEmpty()) {
                        activeUriString = croppedUriFromEditor
                    }
                }

                AddNewClothesScreen(
                    imageUriString = activeUriString,
                    viewModel = clothesViewModel,
                    clothesIdToEdit = clothesId,
                    onSave = { updatedClothesData ->
                        if (!activeUriString.isNullOrEmpty()) {
                            val uriToSave = activeUriString!!.toUri()
                            val permanentPath = saveImagePermanently(context, uriToSave)
                            if (permanentPath != null) {
                                val finalClothes = updatedClothesData.copy(imagePath = permanentPath)
                                clothesViewModel.update(finalClothes)
                                finalClothes.let {
                                    when (it.type) {
                                        Type.Tops -> if (topId == it.id) topId = it.id
                                        Type.Pants -> if (pantsId == it.id) pantsId = it.id
                                        Type.Jacket -> if (jacketId == it.id) jacketId = it.id
                                        Type.Skirt -> if (skirtId == it.id) skirtId = it.id
                                        Type.Dress -> if (dressId == it.id) dressId = it.id
                                    }
                                }
                            }
                        } else {
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
                    onEditImage = { navController.navigate(Routes.Scan.createRoute(clothesId)) },
                    onEditPhoto = {
                        activeUriString?.let { uri ->
                            navController.navigate(Routes.EditPicture.createRoute(uri))
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.EditPicture.route,
            arguments = listOf(navArgument(RouteArgs.IMAGE_URI) { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString(RouteArgs.IMAGE_URI) ?: ""
            EditPictureScreen(
                imageUriString = imageUriString,
                onSave = { croppedUriString ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(RouteArgs.CROPPED_URI, croppedUriString)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(route = Routes.WashingMachine.route) {
            val dirtyClothes = allClothesFromDb.filter { !it.clean }

            WashingMachineScreen(
                dirtyClothes = dirtyClothes,
                onNavigateBack = { navController.popBackStack() },
                onConfirmWashed = { washedClothes ->
                    washedClothes.forEach { cloth ->
                        val updatedCloth = cloth.copy(
                            clean = true, 
                            wornSince = null, 
                            daysWorn = 0,
                            lastWorn = System.currentTimeMillis()
                        )
                        clothesViewModel.update(updatedCloth)
                    }
                }
            )
        }

        composable(route = Routes.Discard.route) {
            //ToDo: für den Beta-Test unter Umständen raus nehmen
            val oneYearAgo = System.currentTimeMillis() - 31536000000L // ca. 1 Jahr (365 Tage)
            val clothesToDiscard = allClothesFromDb.filter { 
                it.lastWorn != null && it.lastWorn!! < oneYearAgo 
            }
            val canUndo = clothesViewModel.lastDiscardedClothes.value != null

            DiscardScreen(
                clothesToDiscard = clothesToDiscard,
                onNavigateBack = { navController.popBackStack() },
                onConfirmDiscard = { clothes ->
                    clothesViewModel.discardClothes(clothes)
                },
                onUndoDiscard = {
                    clothesViewModel.undoLastDiscard()
                },
                canUndo = canUndo
            )
        }

        composable(route = Routes.SavedOutfits.route) {
            val savedOutfits by outfitViewModel.allOutfits.collectAsState(initial = emptyList())
            val correctDisplayedOutfits = savedOutfits.filter { it.isManuelSaved }

            SavedOutfitsScreen(
                outfits = correctDisplayedOutfits,
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

        composable(route = Routes.Weather.route) {
            val application = LocalContext.current.applicationContext as LooksyApplication
            val geocodingViewModel: GeocodingViewModel = viewModel(
                factory = GeocodingViewModelFactory(application.geocodingRepository)
            )

            WeatherScreen(
                weatherViewModel = weatherViewModel,
                geocodingViewModel = geocodingViewModel,
                locationProvider = application.locationProvider,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun getClothById(clothes: List<Clothes>, id: Int): Clothes? {
    return clothes.find { it.id == id }
}

fun calculateDaysWorn(cloth: Clothes): Int {
    return cloth.daysWorn + if(cloth.wornSince == null) 0 else floor(((System.currentTimeMillis() - (cloth.wornSince)) / (1000 * 60 * 60 * 24)).toDouble()).toInt() + 1
}