package com.example.looksy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.looksy.data.location.LocationProvider
import com.example.looksy.data.location.Location
import com.example.looksy.data.model.Weather
import com.example.looksy.ui.screens.WeatherScreen
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.ui.viewmodel.GeocodingUiState
import com.example.looksy.ui.viewmodel.GeocodingViewModel
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for WeatherScreen component.
 * Tests UI rendering for different weather states (Loading, Success, Error).
 */
class WeatherScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockWeatherViewModel = mockk<WeatherViewModel>(relaxed = true)
    private val mockLocationProvider = mockk<LocationProvider>(relaxed = true)
    private val mockGeocodingViewModel = mockk<GeocodingViewModel>(relaxed = true)
    private val weatherStateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    private val geocodingStateFlow = MutableStateFlow<GeocodingUiState>(GeocodingUiState.Idle)

    private fun setupPermissionGrantedAndLocationEnabled() {
        every { mockLocationProvider.hasLocationPermission() } returns true
        every { mockLocationProvider.isLocationEnabled() } returns true
        coEvery { mockLocationProvider.getCurrentLocation() } returns Result.success(
            Location(latitude = 47.3769, longitude = 8.5417)
        )
    }

    @Test
    fun weatherScreen_displaysLoadingState() {
        // Given: Weather state is Loading
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Loading

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Loading indicator is visible
        composeTestRule.onNodeWithText("Wetterdaten werden geladen...").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_displaysSuccessState_withWeatherData() {
        // Given: Weather state is Success with test data
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 18.5,
            feelsLike = 17.0,
            description = "Klarer Himmel",
            humidity = 65,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Success(testWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Weather data is displayed
        composeTestRule.onNodeWithText("19°C").assertIsDisplayed() // Temperature rounded
        composeTestRule.onNodeWithText("Zürich").assertIsDisplayed()
        composeTestRule.onNodeWithText("Klarer Himmel", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("65%").assertIsDisplayed() // Humidity
    }

    @Test
    fun weatherScreen_displaysErrorState_withRetryButton() {
        // Given: Weather state is Error
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Error("Network error")

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Error message and retry button are visible
        composeTestRule.onNodeWithText("Wetter nicht verfügbar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Erneut versuchen").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_showsPermissionDialog_whenPermissionNotGranted() {
        // Given: Location permission not granted
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns false
        every { mockLocationProvider.isLocationEnabled() } returns false
        weatherStateFlow.value = WeatherUiState.Loading

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Permission prompt is visible
        composeTestRule.onNodeWithText("Standort erforderlich").assertIsDisplayed()
        composeTestRule.onNodeWithText("Standort erlauben").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_displaysOutfitRecommendations_forColdWeather() {
        // Given: Cold weather (5°C)
        val coldWeather = Weather(
            locationName = "Zürich",
            temperature = 5.0,
            feelsLike = 3.0,
            description = "Cloudy",
            humidity = 70,
            iconUrl = "https://openweathermap.org/img/w/03d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Success(coldWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Cold weather recommendations are shown
        composeTestRule.onNodeWithText("Outfit-Empfehlungen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jacke", substring = true).assertIsDisplayed()
    }

    @Test
    fun weatherScreen_displaysOutfitRecommendations_forWarmWeather() {
        // Given: Warm weather (25°C)
        val warmWeather = Weather(
            locationName = "Zürich",
            temperature = 25.0,
            feelsLike = 26.0,
            description = "Sunny",
            humidity = 50,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Success(warmWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Warm weather recommendations are shown
        composeTestRule.onNodeWithText("Outfit-Empfehlungen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leichte Kleidung empfohlen").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_headerBackButton_callsNavigateBack() {
        // Given: Screen is displayed
        var backCalled = false
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Loading

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = { backCalled = true }
                )
            }
        }

        // When: Back button is clicked
        composeTestRule.onNodeWithContentDescription("Zurück").performClick()

        // Then: Navigation callback is triggered
        assert(backCalled)
    }

    @Test
    fun weatherScreen_retryButton_retriggersWeatherFetch() {
        // Given: Error state
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Error("Network error")

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // When: Retry button is clicked
        composeTestRule.onNodeWithText("Erneut versuchen").performClick()

        // Then: Weather fetch is called (verification happens in real integration test)
        composeTestRule.waitForIdle()
        coVerify(atLeast = 1) { mockWeatherViewModel.fetchWeather(any(), any()) }
    }

    @Test
    fun weatherScreen_swipeDown_refreshesWeather() {
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 18.0,
            feelsLike = 17.0,
            description = "Cloudy",
            humidity = 60,
            iconUrl = "icon"
        )

        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Success(testWeather)

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        clearMocks(mockWeatherViewModel, answers = false, recordedCalls = true)

        // Target the scrollable weather content to avoid touching the Scaffold header
        composeTestRule.onNode(hasScrollAction()).performTouchInput {
            swipe(start = topCenter, end = bottomCenter, durationMillis = 300)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // let refreshWeatherState coroutine complete

        coVerify(atLeast = 1) { mockWeatherViewModel.fetchWeather(any(), any()) }
    }

    @Test
    fun weatherScreen_permissionFlow_showsOnlyAndroidDialogNotCustomSheet() {
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns false
        every { mockLocationProvider.isLocationEnabled() } returns false
        weatherStateFlow.value = WeatherUiState.Loading

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Standort erlauben").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Während der Nutzung der App").assertDoesNotExist()
        composeTestRule.onNodeWithText("Nur dieses Mal").assertDoesNotExist()
    }

    @Test
    fun weatherScreen_showsChangeCityButton_whenManualCityAndNoPermission() {
        // Given: Manual city entered (via geocoding) and no permission
        val testWeather = Weather(
            locationName = "Berlin",
            temperature = 15.0,
            feelsLike = 14.0,
            description = "Cloudy",
            humidity = 70,
            iconUrl = "https://openweathermap.org/img/w/03d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns false
        every { mockLocationProvider.isLocationEnabled() } returns false

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // When: Geocoding succeeds (simulating manual city entry)
        geocodingStateFlow.value = GeocodingUiState.Success(Location(52.52, 13.405), "Berlin")
        composeTestRule.waitForIdle()
        Thread.sleep(100) // Allow LaunchedEffect to process

        // Then: Weather is fetched
        weatherStateFlow.value = WeatherUiState.Success(testWeather)
        composeTestRule.waitForIdle()

        // Then: Change city button is displayed
        composeTestRule.onNodeWithText("Andere Stadt eingeben").assertIsDisplayed()
        composeTestRule.onNodeWithText("Berlin").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_showsChangeCityButton_whenManualCityAndLocationDisabled() {
        // Given: Manual city entered and location disabled (but permission granted)
        val testWeather = Weather(
            locationName = "München",
            temperature = 12.0,
            feelsLike = 10.0,
            description = "Rainy",
            humidity = 80,
            iconUrl = "https://openweathermap.org/img/w/10d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        every { mockLocationProvider.isLocationEnabled() } returns false

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // When: Geocoding succeeds
        geocodingStateFlow.value = GeocodingUiState.Success(Location(48.1351, 11.582), "München")
        composeTestRule.waitForIdle()
        Thread.sleep(100)

        weatherStateFlow.value = WeatherUiState.Success(testWeather)
        composeTestRule.waitForIdle()

        // Then: Change city button is displayed
        composeTestRule.onNodeWithText("Andere Stadt eingeben").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_hidesChangeCityButton_whenGPSAvailable() {
        // Given: Weather fetched via GPS (both permission and location enabled)
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 18.0,
            feelsLike = 17.0,
            description = "Sunny",
            humidity = 60,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        setupPermissionGrantedAndLocationEnabled()
        weatherStateFlow.value = WeatherUiState.Success(testWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Change city button does NOT exist (GPS was used)
        composeTestRule.onNodeWithText("Andere Stadt eingeben").assertDoesNotExist()
        composeTestRule.onNodeWithText("Zürich").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_changeCityButton_showsCityInputCard() {
        // Given: Weather displayed for manual city without permission
        val testWeather = Weather(
            locationName = "Hamburg",
            temperature = 10.0,
            feelsLike = 8.0,
            description = "Windy",
            humidity = 75,
            iconUrl = "https://openweathermap.org/img/w/50d.png"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockGeocodingViewModel.geocodingState } returns geocodingStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns false
        every { mockLocationProvider.isLocationEnabled() } returns false

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    geocodingViewModel = mockGeocodingViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Setup: Simulate manual city entry flow
        geocodingStateFlow.value = GeocodingUiState.Success(Location(53.5511, 9.9937), "Hamburg")
        composeTestRule.waitForIdle()
        Thread.sleep(100)

        weatherStateFlow.value = WeatherUiState.Success(testWeather)
        composeTestRule.waitForIdle()

        // When: Click "Andere Stadt eingeben" button
        composeTestRule.onNodeWithText("Andere Stadt eingeben").performClick()
        composeTestRule.waitForIdle()

        // Then: CityInputCard is shown
        composeTestRule.onNodeWithText("Stadt eingeben").assertIsDisplayed()
        
        // And: Weather data is no longer displayed (showCityInput hides it)
        composeTestRule.onNodeWithText("Hamburg").assertDoesNotExist()
    }
}
