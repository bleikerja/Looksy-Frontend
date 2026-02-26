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
            description = "Clear sky",
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
        composeTestRule.onNodeWithText("Clear sky", ignoreCase = true).assertIsDisplayed()
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
}
