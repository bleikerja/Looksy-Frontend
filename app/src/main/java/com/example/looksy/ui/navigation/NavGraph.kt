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
import com.example.looksy.ui.screens.OutfitDetailsScreen
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
    var pulloverId by remember { mutableStateOf<Int?>(null) }
    var pantsId by remember { mutableStateOf<Int?>(null) }
    var jacketId by remember { mutableStateOf<Int?>(null) }
    var skirtId by remember { mutableStateOf<Int?>(null) }
    var dressId by remember { mutableStateOf<Int?>(null) }
    var shoesId by remember { mutableStateOf<Int?>(null) }

    // Track permission and location state for weather
    var permissionState by remember { mutableStateOf(PermissionState.NOT_ASKED) }
    var isLocationEnabled by remember { mutableStateOf(true) }

    var editingOutfitId by remember { mutableStateOf<Int?>(null) }

    // Track whether the FullOutfitScreen is in GRID mode (no mutual exclusion)
    var isGridMode by remember { mutableStateOf(false) }

    LaunchedEffect(allClothesFromDb) {
        if (listOfNotNull(topId, pulloverId, pantsId, jacketId, skirtId, dressId).isEmpty()){
            topId = allClothesFromDb.find { it.type == Type.TShirt && it.selected }?.id
            pulloverId = allClothesFromDb.find { it.type == Type.Pullover && it.selected }?.id
            pantsId = allClothesFromDb.find { it.type == Type.Pants && it.selected }?.id
            jacketId = allClothesFromDb.find { it.type == Type.Jacket && it.selected }?.id
            skirtId = allClothesFromDb.find { it.type == Type.Skirt && it.selected }?.id
            dressId = allClothesFromDb.find { it.type == Type.Dress && it.selected }?.id
            shoesId = allClothesFromDb.find { it.type == Type.Shoes && it.selected }?.id
        }

        if (allClothesFromDb.isNotEmpty() && topId == null && pulloverId == null && dressId == null) {
            val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb)
            topId = outfit.top?.id
            pulloverId = outfit.pullover?.id
            pantsId = outfit.pants?.id
            skirtId = outfit.skirt?.id
            jacketId = outfit.jacket?.id
            dressId = outfit.dress?.id
            shoesId = outfit.shoes?.id
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

            // Fetch weather on launch: use GPS if available, otherwise restore saved city
            LaunchedEffect(Unit) {
                if (application.locationProvider.hasLocationPermission()) {
                    isLocationEnabled = application.locationProvider.isLocationEnabled()
                    if (isLocationEnabled) {
                        application.locationProvider.getCurrentLocation().onSuccess { location ->
                            weatherViewModel.fetchWeather(location.latitude, location.longitude)
                        }.onFailure {
                            weatherViewModel.fetchWeatherForSavedCity()
                        }
                    } else {
                        weatherViewModel.fetchWeatherForSavedCity()
                    }
                } else {
                    weatherViewModel.fetchWeatherForSavedCity()
                }
            }

            FullOutfitScreen(
                allClothes = allClothesFromDb,
                selectedTshirtId = topId,
                selectedPulloverId = pulloverId,
                selectedPantsId = pantsId,
                selectedSkirtId = skirtId,
                selectedDressId = dressId,
                selectedJacketId = jacketId,
                selectedShoesId = shoesId,
                onSlotChanged = { type, id ->
                    when (type) {
                        Type.TShirt -> topId = id
                        Type.Pullover -> pulloverId = id
                        Type.Pants -> {
                            pantsId = id
                            if (id != null && !isGridMode) {
                                skirtId = null
                                dressId = null
                            }
                        }
                        Type.Skirt -> {
                            skirtId = id
                            if (id != null && !isGridMode) {
                                pantsId = null
                                dressId = null
                            }
                        }
                        Type.Dress -> {
                            dressId = id
                            if (id != null && !isGridMode) {
                                topId = null
                                pantsId = null
                                skirtId = null
                            }
                        }
                        Type.Jacket -> jacketId = id
                        Type.Shoes -> shoesId = id
                    }
                },
                weatherState = weatherState,
                permissionState = permissionState,
                isLocationEnabled = isLocationEnabled,
                onWeatherClick = { navController.navigate(Routes.Weather.route) },
                onClick = { clothesId ->
                    navController.navigate(Routes.Details.createRoute(clothesId))
                },
                onConfirm = { wornClothesList ->
                    val updatedClothesList = wornClothesList.map { it.copy(wornSince = System.currentTimeMillis(), selected = true) }
                    clothesViewModel.updateAll(updatedClothesList)
                    clothesViewModel.incrementClothesPreference(updatedClothesList)
                    outfitViewModel.incrementOutfitPreference(
                        topId,
                        dressId,
                        skirtId,
                        pantsId,
                        jacketId,
                        pulloverId,
                        shoesId
                    )
                },
                onMoveToWashingMachine = { dirtyClothesList, cleanClothesList ->
                    topId = null
                    pulloverId = null
                    pantsId = null
                    jacketId = null
                    skirtId = null
                    dressId = null
                    shoesId = null
                    val updatedDirtyClothesList = dirtyClothesList.map { it.copy(selected = false, clean = false) }
                    val updatedCleanClothesList = cleanClothesList.map { it.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(it)) }
                    clothesViewModel.updateAll(updatedDirtyClothesList + updatedCleanClothesList)
                },
                onWashingMachine = { navController.navigate(Routes.WashingMachine.route) },
                onGenerateRandom = {
                    clothesViewModel.updateAll(allClothesFromDb.map { it.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(it)) })
                    val outfit = generateRandomOutfit(allClothesFromDb, allOutfitsFromDb)
                    topId = outfit.top?.id
                    pulloverId = outfit.pullover?.id
                    pantsId = outfit.pants?.id
                    skirtId = outfit.skirt?.id
                    jacketId = outfit.jacket?.id
                    dressId = outfit.dress?.id
                    shoesId = outfit.shoes?.id
                },
                onCamera = { navController.navigate(Routes.Scan.createRoute(-1)) },
                onSave = {
                    val outfitToSave = Outfit(
                        dressId = dressId,
                        topsId = topId,
                        pulloverId = pulloverId,
                        skirtId = skirtId,
                        pantsId = pantsId,
                        jacketId = jacketId,
                        shoesId = shoesId,
                        isSynced = false,
                        isManuelSaved = true
                    )
                    outfitViewModel.insert(outfitToSave)
                },
                onGridModeChanged = { gridMode -> isGridMode = gridMode }
            )
        }

        composable(Routes.ChoseClothes.route) {
            CategoriesScreen(
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
                    val isInOutfit = cloth.id == topId || cloth.id == pantsId ||
                        cloth.id == jacketId || cloth.id == skirtId || cloth.id == dressId
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        ClothInformationScreen(
                            clothesData = cloth,
                            viewModel = clothesViewModel,
                            onMoveToWashingMachine = {
                                val updatedCloth = cloth.copy(clean = false)
                                clothesViewModel.update(updatedCloth)
                                if(topId == cloth.id) topId = null
                                if(pulloverId == cloth.id) pulloverId = null
                                if(pantsId == cloth.id) pantsId = null
                                if(jacketId == cloth.id) jacketId = null
                                if(skirtId == cloth.id) skirtId = null
                                if(dressId == cloth.id) dressId = null
                                if(shoesId == cloth.id) shoesId = null
                            },
                            onNavigateToDetails = { newId ->
                                navController.navigate(Routes.Details.createRoute(newId)) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = { navController.popBackStack() },
                            isInOutfit = isInOutfit,
                            onConfirmOutfit = { confirmedId ->
                                val selectedCloth = getClothById(allClothesFromDb, confirmedId)
                                val prevCloth = allClothesFromDb.find { it.type == selectedCloth?.type && it.selected }
                                if(prevCloth != null && prevCloth.id != selectedCloth?.id) {
                                    clothesViewModel.update(prevCloth.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(prevCloth)))
                                }

                                selectedCloth?.let {
                                    when (it.type) {
                                        Type.TShirt -> topId = it.id
                                        Type.Pullover -> pulloverId = it.id
                                        Type.Pants -> {
                                            pantsId = it.id
                                            skirtId = null
                                            dressId = null
                                        }
                                        Type.Jacket -> jacketId = it.id
                                        Type.Skirt -> {
                                            skirtId = it.id
                                            pantsId = null
                                            dressId = null
                                        }
                                        Type.Dress -> {
                                            dressId = it.id
                                            skirtId = null
                                            pantsId = null
                                            topId = null
                                        }
                                        Type.Shoes -> shoesId = it.id
                                    }
                                }
                                navController.navigate(Routes.Home.route) {
                                    popUpTo(Routes.Home.route) { inclusive = true }
                                }
                            },
                            onDeselectOutfit = {
                                val cleanOfSameType = allClothesFromDb.count { it.type == cloth.type && it.clean }
                                val canDeselect = cleanOfSameType > 1

                                if (!canDeselect) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = message,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } else {
                                    when (cloth.type) {
                                        Type.TShirt -> topId = null
                                        Type.Pullover -> pulloverId = null
                                        Type.Pants -> pantsId = null
                                        Type.Jacket -> jacketId = null
                                        Type.Skirt -> skirtId = null
                                        Type.Dress -> dressId = null
                                        Type.Shoes -> shoesId = null
                                    }
                                    if (cloth.selected) clothesViewModel.update(cloth.copy(selected = false, wornSince = null, daysWorn = calculateDaysWorn(cloth)))
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
                onCropPhoto = {
                    activeUriString?.let { uri ->
                        navController.navigate(Routes.EditPicture.createRoute(uri))
                    }
                },
                onEditImage = {
                    navController.navigate(Routes.Scan.createRoute(-1)) {
                        popUpTo(Routes.AddNewClothes.route) {
                            inclusive = true
                        }
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

                // Read cropped URI returned by EditPictureScreen via SavedStateHandle
                val croppedUriFromEditor by backStackEntry.savedStateHandle
                    .getStateFlow(RouteArgs.CROPPED_URI, "")
                    .collectAsState()

                // Active URI: start with the route arg; override when EditPictureScreen delivers a crop
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
                                        Type.TShirt -> if (topId == it.id) topId = it.id
                                        Type.Pullover -> if (pulloverId == it.id) pulloverId = it.id
                                        Type.Pants -> if (pantsId == it.id) pantsId = it.id
                                        Type.Jacket -> if (jacketId == it.id) jacketId = it.id
                                        Type.Skirt -> if (skirtId == it.id) skirtId = it.id
                                        Type.Dress -> if (dressId == it.id) dressId = it.id
                                        Type.Shoes -> if (shoesId == it.id) shoesId = it.id
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
                    onCropPhoto = {
                        // activeUriString is null when editing an existing item that hasn't had
                        // a new photo taken yet → fall back to the stored image path from DB
                        val uriToEdit = if (!activeUriString.isNullOrEmpty()) {
                            activeUriString
                        } else {
                            allClothesFromDb.find { it.id == clothesId }
                                ?.imagePath?.takeIf { it.isNotEmpty() }
                                ?.let { java.io.File(it).toUri().toString() }
                        }
                        uriToEdit?.let { uri ->
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
                    val outfitPullover = outfit.pulloverId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitPants = outfit.pantsId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitDress = outfit.dressId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitJacket = outfit.jacketId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitSkirt = outfit.skirtId?.let { id -> allClothesFromDb.find { it.id == id } }
                    val outfitShoes = outfit.shoesId?.let { id -> allClothesFromDb.find { it.id == id } }

                    OutfitDetailsScreen(
                        outfit = outfit,
                        outfitTop = outfitTop,
                        outfitPullover = outfitPullover,
                        outfitPants = outfitPants,
                        outfitDress = outfitDress,
                        outfitJacket = outfitJacket,
                        outfitSkirt = outfitSkirt,
                        outfitShoes = outfitShoes,
                        // Button 1: Bearbeiten - Navigiert zu Edit Screen
                        onEdit = {
                            editingOutfitId = outfitId
                            navController.navigate(Routes.EditOutfit.createRoute(outfitId))
                        },
                        // Button 2: Löschen - Löscht Outfit und geht zurück
                        onDelete = {
                            outfitViewModel.delete(outfit)
                            navController.popBackStack(Routes.SavedOutfits.route, inclusive = false)
                        },
                        // Button 3: Tragen/Auswählen - Setzt Outfit auf Home
                        onWear = {
                            topId = outfitTop?.id
                            pulloverId = outfitPullover?.id
                            pantsId = outfitPants?.id
                            dressId = outfitDress?.id
                            jacketId = outfitJacket?.id
                            skirtId = outfitSkirt?.id
                            shoesId = outfitShoes?.id
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.SavedOutfits.route) { inclusive = false }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(
            route = Routes.EditOutfit.route,
            arguments = listOf(navArgument(RouteArgs.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getInt(RouteArgs.ID)
            if (outfitId != null) {

                CategoriesScreen(
                    categoryItems = categoryItems,
                    onClick = { type ->
                        val finalRoute = Routes.SpecificCategory.createRoute(type)
                        navController.navigate(finalRoute)
                    }
                )
            }
        }
    }
}


fun getClothById(clothes: List<Clothes>, id: Int): Clothes? {
    return clothes.find { it.id == id }
}

fun calculateDaysWorn(cloth: Clothes): Int {
    return cloth.daysWorn + if(cloth.wornSince == null) 0 else floor(((System.currentTimeMillis() - (cloth.wornSince)) / (1000 * 60 * 60 * 24)).toDouble()).toInt() + 1
}