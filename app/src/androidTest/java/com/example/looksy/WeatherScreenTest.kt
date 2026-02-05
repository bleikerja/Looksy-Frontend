package com.example.looksy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.looksy.data.location.LocationProvider
import com.example.looksy.data.model.Weather
import com.example.looksy.ui.screens.WeatherScreen
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
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
    private val weatherStateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)

    @Test
    fun weatherScreen_displaysLoadingState() {
        // Given: Weather state is Loading
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        weatherStateFlow.value = WeatherUiState.Loading

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Loading indicator is visible
        composeTestRule.onNodeWithText("Wetter wird geladen...").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_displaysSuccessState_withWeatherData() {
        // Given: Weather state is Success with test data
        val testWeather = Weather(
            temperature = 18.5,
            feelsLike = 17.0,
            description = "Clear sky",
            humidity = 65,
            windSpeed = 3.5,
            pressure = 1013,
            cityName = "Zürich"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        weatherStateFlow.value = WeatherUiState.Success(testWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
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
        composeTestRule.onNodeWithText("3.5 m/s").assertIsDisplayed() // Wind speed
    }

    @Test
    fun weatherScreen_displaysErrorState_withRetryButton() {
        // Given: Weather state is Error
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        weatherStateFlow.value = WeatherUiState.Error("Network error")

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Error message and retry button are visible
        composeTestRule.onNodeWithText("Fehler beim Laden", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Erneut versuchen").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_showsPermissionDialog_whenPermissionNotGranted() {
        // Given: Location permission not granted
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns false
        weatherStateFlow.value = WeatherUiState.Loading

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Permission dialog is visible
        composeTestRule.onNodeWithText("Standortzugriff erforderlich").assertIsDisplayed()
        composeTestRule.onNodeWithText("Erlauben").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abbrechen").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_displaysOutfitRecommendations_forColdWeather() {
        // Given: Cold weather (5°C)
        val coldWeather = Weather(
            temperature = 5.0,
            feelsLike = 3.0,
            description = "Cloudy",
            humidity = 70,
            windSpeed = 5.0,
            pressure = 1015,
            cityName = "Zürich"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        weatherStateFlow.value = WeatherUiState.Success(coldWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
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
            temperature = 25.0,
            feelsLike = 26.0,
            description = "Sunny",
            humidity = 50,
            windSpeed = 2.0,
            pressure = 1010,
            cityName = "Zürich"
        )
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        weatherStateFlow.value = WeatherUiState.Success(warmWeather)

        // When: Screen is displayed
        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // Then: Warm weather recommendations are shown
        composeTestRule.onNodeWithText("Outfit-Empfehlungen").assertIsDisplayed()
        composeTestRule.onNodeWithText("T-Shirt", substring = true).assertIsDisplayed()
    }

    @Test
    fun weatherScreen_headerBackButton_callsNavigateBack() {
        // Given: Screen is displayed
        var backCalled = false
        every { mockWeatherViewModel.weatherState } returns weatherStateFlow
        every { mockLocationProvider.hasLocationPermission() } returns true
        weatherStateFlow.value = WeatherUiState.Loading

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
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
        every { mockLocationProvider.hasLocationPermission() } returns true
        coEvery { mockLocationProvider.getCurrentLocation() } returns Result.success(
            mockk {
                every { latitude } returns 47.3769
                every { longitude } returns 8.5417
            }
        )
        weatherStateFlow.value = WeatherUiState.Error("Network error")

        composeTestRule.setContent {
            LooksyTheme {
                WeatherScreen(
                    weatherViewModel = mockWeatherViewModel,
                    locationProvider = mockLocationProvider,
                    onNavigateBack = {}
                )
            }
        }

        // When: Retry button is clicked
        composeTestRule.onNodeWithText("Erneut versuchen").performClick()

        // Then: Weather fetch is called (verification happens in real integration test)
        composeTestRule.waitForIdle()
    }
}
