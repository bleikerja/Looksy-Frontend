package com.example.looksy.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.looksy.data.location.LocationProvider
import com.example.looksy.data.model.Weather
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel,
    locationProvider: LocationProvider,
    onNavigateBack: () -> Unit
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions.values.any { it }
        if (locationPermissionGranted) {
            scope.launch {
                locationProvider.getCurrentLocation().onSuccess { location ->
                    weatherViewModel.fetchWeather(location.latitude, location.longitude)
                }
            }
        }
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        if (locationProvider.hasLocationPermission()) {
            locationPermissionGranted = true
            locationProvider.getCurrentLocation().onSuccess { location ->
                weatherViewModel.fetchWeather(location.latitude, location.longitude)
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Permission Dialog
    if (showPermissionDialog) {
        LocationPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        )
    }

    Scaffold(
        topBar = {
            Header(
                onNavigateBack = onNavigateBack,
                onNavigateToRightIcon = {},
                clothesData = null,
                headerText = "Wetter",
                rightIconContentDescription = null,
                rightIcon = null
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                            if (locationPermissionGranted) {
                                scope.launch {
                                    locationProvider.getCurrentLocation().onSuccess { location ->
                                        weatherViewModel.fetchWeather(location.latitude, location.longitude)
                                    }
                                }
                            } else {
                                showPermissionDialog = true
                            }
                        }
                    )
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
                text = getWeatherEmoji(weather.description),
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
    onRetry: () -> Unit
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
            Button(onClick = onRetry) {
                Text("Erneut versuchen")
            }
        }
    }
}

@Composable
private fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null
            )
        },
        title = {
            Text("Standortzugriff erforderlich")
        },
        text = {
            Text(
                "Um das Wetter an deinem Standort anzuzeigen, " +
                        "benötigt Looksy Zugriff auf deinen Standort. " +
                        "Diese Information wird nur für die Wetter-API verwendet."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Erlauben")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ablehnen")
            }
        }
    )
}

// Helper functions
private fun getWeatherEmoji(description: String): String {
    return when {
        description.contains("clear", ignoreCase = true) -> "☀️"
        description.contains("cloud", ignoreCase = true) -> "☁️"
        description.contains("rain", ignoreCase = true) -> "🌧️"
        description.contains("drizzle", ignoreCase = true) -> "🌦️"
        description.contains("thunder", ignoreCase = true) -> "⛈️"
        description.contains("snow", ignoreCase = true) -> "❄️"
        description.contains("mist", ignoreCase = true) ||
                description.contains("fog", ignoreCase = true) -> "🌫️"
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

    if (weather.description.contains("rain", ignoreCase = true)) {
        recommendations.add(OutfitRecommendation("⚠️ Regenschirm empfohlen", true))
    }

    return recommendations
}
