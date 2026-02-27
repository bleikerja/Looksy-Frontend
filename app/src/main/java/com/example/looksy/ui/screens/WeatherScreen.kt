package com.example.looksy.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.looksy.data.location.LocationInputMode
import com.example.looksy.data.location.LocationProvider
import com.example.looksy.data.location.PermissionState
import com.example.looksy.data.model.Weather
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.viewmodel.GeocodingUiState
import com.example.looksy.ui.viewmodel.GeocodingViewModel
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel,
    geocodingViewModel: GeocodingViewModel,
    locationProvider: LocationProvider,
    onNavigateBack: () -> Unit
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val geocodingState by geocodingViewModel.geocodingState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // State management
    var permissionState by remember { mutableStateOf(PermissionState.NOT_ASKED) }
    var locationInputMode by remember { mutableStateOf(LocationInputMode.GPS) }
    var showCityInput by remember { mutableStateOf(false) }
    var cityName by remember { mutableStateOf("") }
    var isLocationEnabled by remember { mutableStateOf(true) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }
    // Holds the error message to show in a snackbar, decoupled from geocodingState
    // so the LaunchedEffect key change doesn't cancel the running snackbar coroutine.
    var lastGeocodingError by remember { mutableStateOf<String?>(null) }
    // Persists the last city the user searched, so a pull-to-refresh re-fetches
    // weather for that city instead of reverting back to the permission/GPS flow.
    var lastSearchedCity by remember { mutableStateOf("") }

    fun refreshWeatherState() {
        if (isRefreshing) return

        scope.launch {
            isRefreshing = true

            // If the user previously entered a city manually, keep fetching for
            // that city on every refresh — unless location permission and services
            // are both available again, in which case switch back to GPS.
            if (lastSearchedCity.isNotBlank()) {
                val hasPermissionNow = locationProvider.hasLocationPermission()
                val locationOnNow = locationProvider.isLocationEnabled()
                if (hasPermissionNow && locationOnNow) {
                    // Location is back — clear the city override and fall through
                    // to the GPS branch below.
                    lastSearchedCity = ""
                } else {
                    geocodingViewModel.getCityCoordinates(lastSearchedCity)
                    isRefreshing = false
                    return@launch
                }
            }

            val hasPermission = locationProvider.hasLocationPermission()
            isLocationEnabled = locationProvider.isLocationEnabled()

            if (hasPermission) {
                permissionState = PermissionState.GRANTED_WHILE_IN_USE

                if (isLocationEnabled) {
                    locationInputMode = LocationInputMode.GPS
                    showCityInput = false

                    locationProvider.getCurrentLocation().onSuccess { location ->
                        weatherViewModel.fetchWeather(location.latitude, location.longitude)
                    }.onFailure {
                        locationInputMode = LocationInputMode.MANUAL_CITY
                        showCityInput = true
                    }
                } else {
                    locationInputMode = LocationInputMode.MANUAL_CITY
                    showCityInput = false
                }
            } else {
                // shouldShowRequestPermissionRationale returns true when the user
                // denied once in the current install, so we can differentiate
                // "never asked" from "already denied" on a fresh screen load.
                val activity = context as? android.app.Activity
                val wasDeniedBefore = (activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.ACCESS_FINE_LOCATION
                    )) || permissionState == PermissionState.DENIED

                permissionState = if (wasDeniedBefore) PermissionState.DENIED else PermissionState.NOT_ASKED
                locationInputMode = LocationInputMode.MANUAL_CITY
                // Jump straight to city input when permission was already denied,
                // so the user lands on the city card rather than the prompt card.
                showCityInput = wasDeniedBefore
            }

            isRefreshing = false
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            permissionState = PermissionState.GRANTED_WHILE_IN_USE
            // Drop the saved city so the newly granted GPS location is used.
            lastSearchedCity = ""
            refreshWeatherState()

            if (!locationProvider.isLocationEnabled()) {
                showLocationSettingsDialog = true
            }
        } else {
            // Detect permanent denial: rationale is false AND we already knew we
            // were DENIED (set during this composable's lifetime).  In that case the
            // OS won't show a dialog at all, so send the user straight to App Settings.
            val activity = context as? android.app.Activity
            val isPermanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_FINE_LOCATION
                ) && permissionState == PermissionState.DENIED

            permissionState = PermissionState.DENIED
            locationInputMode = LocationInputMode.MANUAL_CITY
            showCityInput = true

            if (isPermanentlyDenied) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

    // Observe geocoding state
    LaunchedEffect(geocodingState) {
        when (geocodingState) {
            is GeocodingUiState.Success -> {
                val success = geocodingState as GeocodingUiState.Success
                // Persist city so pull-to-refresh re-fetches the same city.
                lastSearchedCity = success.cityName
                weatherViewModel.fetchWeather(success.location.latitude, success.location.longitude)
                // Mark location as available so the LocationDisabledCard branch
                // doesn't re-appear after a successful city lookup.
                isLocationEnabled = true
                showCityInput = false
                cityName = ""
                geocodingViewModel.resetState()
            }
            is GeocodingUiState.Error -> {
                val error = geocodingState as GeocodingUiState.Error
                // Copy the message first, then reset state immediately.
                // If we called resetState() after showSnackbar() the key change
                // would cancel the coroutine and the snackbar would never appear.
                lastGeocodingError = error.message
                geocodingViewModel.resetState()
            }
            else -> {}
        }
    }

    // Show geocoding error in a snackbar, keyed separately so it is never
    // cancelled by a state transition in geocodingState.
    LaunchedEffect(lastGeocodingError) {
        lastGeocodingError?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            lastGeocodingError = null
        }
    }

    // Check permissions and location services on launch
    LaunchedEffect(Unit) {
        refreshWeatherState()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshWeatherState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Location Settings Dialog
    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { 
                showLocationSettingsDialog = false
                locationInputMode = LocationInputMode.MANUAL_CITY
                showCityInput = true
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null
                )
            },
            title = { Text("Standort aktivieren") },
            text = {
                Text(
                    "Standortdienste sind deaktiviert. Bitte aktiviere sie in den Einstellungen, " +
                    "um das Wetter für deinen aktuellen Standort zu sehen."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showLocationSettingsDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Zu Einstellungen")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLocationSettingsDialog = false
                    locationInputMode = LocationInputMode.MANUAL_CITY
                    showCityInput = true
                }) {
                    Text("Stadt eingeben")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Header(
                onNavigateBack = onNavigateBack,
                // When the city-input card is on screen, show a LocationOff icon
                // in the header so the user can tap to enable location in one tap.
                onNavigateToRightIcon = { _ ->
                    if (!locationProvider.hasLocationPermission()) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    } else {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                },
                clothesData = null,
                headerText = "Wetter",
                rightIconContentDescription = if (lastSearchedCity.isNotBlank() && !showCityInput) "Standort aktivieren" else null,
                rightIcon = if (lastSearchedCity.isNotBlank() && !showCityInput) Icons.Default.LocationOff else null
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshWeatherState() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show appropriate UI based on state
                when {
                    // Permission not asked yet — use the same card as location-disabled,
                    // wiring the primary button to request permission instead.
                    permissionState == PermissionState.NOT_ASKED -> {
                        LocationAccessCard(
                            onEnableLocation = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            },
                            onEnterCity = { showCityInput = true },
                            enableButtonText = "Standort erlauben"
                        )
                    }

                    // Permission granted but location is off
                    permissionState != PermissionState.NOT_ASKED &&
                    permissionState != PermissionState.DENIED &&
                    !isLocationEnabled &&
                    locationInputMode == LocationInputMode.MANUAL_CITY &&
                    !showCityInput -> {
                        LocationAccessCard(
                            onEnableLocation = {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            },
                            onEnterCity = { showCityInput = true }
                        )
                    }

                    // User chose to enter city manually (or was sent here after
                    // permission denial). showCityInput is the single source of truth;
                    // removing the permissionState == DENIED check here means that
                    // after a successful geocode (showCityInput = false) the weather
                    // card appears even when the permission is still DENIED.
                    showCityInput &&
                    locationInputMode == LocationInputMode.MANUAL_CITY -> {
                        CityInputCard(
                            cityName = cityName,
                            onCityNameChange = { cityName = it },
                            isLoading = geocodingState is GeocodingUiState.Loading,
                            onSubmit = {
                                if (cityName.isNotBlank()) {
                                    geocodingViewModel.getCityCoordinates(cityName)
                                }
                            },
                            // Show the permission button whenever the app does not
                            // yet have permission (covers both NOT_ASKED and DENIED).
                            onRequestPermission = if (permissionState != PermissionState.GRANTED_WHILE_IN_USE &&
                                permissionState != PermissionState.GRANTED_ONCE) {
                                {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                }
                            } else null
                        )
                    }

                    // Normal weather display
                    else -> {
                        when (weatherState) {
                            is WeatherUiState.Loading -> {
                                LoadingWeatherCard()
                            }

                            is WeatherUiState.Success -> {
                                val weather = (weatherState as WeatherUiState.Success).weather

                                // Main Weather Card
                                WeatherCard(weather = weather)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Outfit Recommendations
                                OutfitRecommendationsCard(weather = weather)
                            }

                            is WeatherUiState.Error -> {
                                ErrorWeatherCard(
                                    message = (weatherState as WeatherUiState.Error).message,
                                    onRetry = {
                                        if (permissionState == PermissionState.GRANTED_WHILE_IN_USE ||
                                            permissionState == PermissionState.GRANTED_ONCE) {
                                            isLocationEnabled = locationProvider.isLocationEnabled()
                                            if (isLocationEnabled) {
                                                refreshWeatherState()
                                            } else {
                                                showLocationSettingsDialog = true
                                            }
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                )
                                            )
                                        }
                                    },
                                    onEnterCity = {
                                        locationInputMode = LocationInputMode.MANUAL_CITY
                                        showCityInput = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(weather: Weather) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weather Icon (Emoji)
            Text(
                text = getWeatherEmoji(weather.iconUrl),
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = weather.locationName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Temperature
            Text(
                text = "${weather.temperature.roundToInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Gefühlt wie ${weather.feelsLike.roundToInt()}°C",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                InfoChip(
                    icon = Icons.Default.WaterDrop,
                    label = "Feuchtigkeit",
                    value = "${weather.humidity}%"
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OutfitRecommendationsCard(weather: Weather) {
    val recommendations = getOutfitRecommendations(weather)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Outfit-Empfehlungen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (recommendation.recommended)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Info,
                        contentDescription = null,
                        tint = if (recommendation.recommended)
                            Color(0xFF4CAF50)
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recommendation.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingWeatherCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Wetterdaten werden geladen...")
        }
    }
}

@Composable
private fun ErrorWeatherCard(
    message: String,
    onRetry: () -> Unit,
    onEnterCity: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Wetter nicht verfügbar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEnterCity) {
                    Text("Stadt eingeben")
                }
                Button(onClick = onRetry) {
                    Text("Erneut versuchen")
                }
            }
        }
    }
}

@Composable
private fun LocationAccessCard(
    onEnableLocation: () -> Unit,
    onEnterCity: () -> Unit,
    enableButtonText: String = "Standort aktivieren"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Standort aktivieren",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Deine Standortdienste sind deaktiviert. Aktiviere sie, um das Wetter für deinen aktuellen Standort zu sehen.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEnterCity) {
                    Text("Stadt eingeben")
                }
                Button(onClick = onEnableLocation) {
                    Text(enableButtonText)
                }
            }
        }
    }
}

@Composable
private fun CityInputCard(
    cityName: String,
    onCityNameChange: (String) -> Unit,
    isLoading: Boolean = false,
    onSubmit: () -> Unit,
    onRequestPermission: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌍",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Stadt eingeben",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gib den Namen deiner Stadt ein, um das Wetter zu sehen.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = cityName,
                onValueChange = onCityNameChange,
                label = { Text("Stadt") },
                placeholder = { Text("z.B. Berlin, München") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSubmit,
                enabled = cityName.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Suche läuft..." else "Wetter suchen")
            }
            
            if (onRequestPermission != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRequestPermission) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Standortzugriff erlauben")
                }
            }
        }
    }
}

// Helper functions
// Maps OWM icon codes to emojis (language-independent)
// Icon codes: https://openweathermap.org/weather-conditions
private fun getWeatherEmoji(iconUrl: String): String {
    val code = iconUrl.substringAfterLast("/").removeSuffix(".png").take(2)
    return when (code) {
        "01" -> "☀️"
        "02" -> "🌤️"
        "03" -> "🌥️"
        "04" -> "☁️"
        "09" -> "🌦️"
        "10" -> "🌧️"
        "11" -> "⛈️"
        "13" -> "❄️"
        "50" -> "🌫️"
        else -> "🌤️"
    }
}

data class OutfitRecommendation(
    val text: String,
    val recommended: Boolean
)

private fun getOutfitRecommendations(weather: Weather): List<OutfitRecommendation> {
    val temp = weather.temperature
    val recommendations = mutableListOf<OutfitRecommendation>()

    when {
        temp < 5 -> {
            recommendations.add(OutfitRecommendation("Warme Jacke empfohlen", true))
            recommendations.add(OutfitRecommendation("Langarm-Shirt", true))
            recommendations.add(OutfitRecommendation("Lange Hose", true))
        }
        temp < 15 -> {
            recommendations.add(OutfitRecommendation("Leichte Jacke", true))
            recommendations.add(OutfitRecommendation("Langarm oder Kurzarm möglich", false))
        }
        temp < 25 -> {
            recommendations.add(OutfitRecommendation("Kurzarm-Shirt", true))
            recommendations.add(OutfitRecommendation("Jacke optional", false))
        }
        else -> {
            recommendations.add(OutfitRecommendation("Leichte Kleidung empfohlen", true))
            recommendations.add(OutfitRecommendation("Jacke nicht nötig", false))
        }
    }

    val iconCode = weather.iconUrl.substringAfterLast("/").removeSuffix(".png").take(2)
    if (iconCode == "09" || iconCode == "10" || iconCode == "11") {
        recommendations.add(OutfitRecommendation("⚠️ Regenschirm empfohlen", true))
    }

    return recommendations
}
