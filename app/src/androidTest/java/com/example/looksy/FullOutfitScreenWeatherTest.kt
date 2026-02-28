package com.example.looksy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.looksy.R
import com.example.looksy.data.location.PermissionState
import com.example.looksy.data.model.*
import com.example.looksy.ui.screens.FullOutfitScreen
import com.example.looksy.ui.theme.LooksyTheme
import com.example.looksy.ui.viewmodel.WeatherUiState
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for WeatherIconRow integration in FullOutfitScreen.
 * Tests weather display and navigation in the home screen.
 */
class FullOutfitScreenWeatherTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTop = Clothes(
        id = 1,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false
    )

    private val testPants = Clothes(
        id = 2,
        type = Type.Pants,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false
    )

    @Test
    fun fullOutfitScreen_displaysWeatherLoadingState() {
        // Given: Weather state is Loading
        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Loading,
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Loading spinner is visible
        composeTestRule.onNodeWithTag("weather_loading").assertExists()
    }

    @Test
    fun fullOutfitScreen_displaysWeatherSuccessState_withTemperature() {
        // Given: Weather state is Success with 18°C
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 18.5,
            feelsLike = 17.0,
            description = "Klarer Himmel",
            humidity = 65,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(testWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Temperature is displayed
        composeTestRule.onNodeWithText("19°C").assertIsDisplayed()
        // Weather emoji should be visible (☀️ for klarer Himmel, mapped via icon code 01)
        composeTestRule.onNodeWithText("☀️").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_displaysWeatherErrorState() {
        // Given: Weather state is Error
        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Error("Network error"),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Error message is visible
        composeTestRule.onNodeWithText("Wetter nicht verfügbar").assertIsDisplayed()
        // Error icon (CloudOff) should be visible
        composeTestRule.onNodeWithContentDescription("Weather unavailable").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_weatherIconRow_isClickable() {
        // Given: Weather state with data
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 22.0,
            feelsLike = 21.0,
            description = "Mäßig bewölkt",
            humidity = 60,
            iconUrl = "https://openweathermap.org/img/w/02d.png"
        )
        var weatherClicked = false

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(testWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = { weatherClicked = true }
                )
            }
        }

        // When: Weather icon row is clicked
        composeTestRule.onNodeWithText("22°C").performClick()

        // Then: Navigation callback is triggered
        assert(weatherClicked)
    }

    @Test
    fun fullOutfitScreen_weatherEmoji_matchesWeatherCondition_sunny() {
        // Given: Clear weather
        val sunnyWeather = Weather(
            locationName = "Zürich",
            temperature = 25.0,
            feelsLike = 24.0,
            description = "Klarer Himmel",
            humidity = 50,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(sunnyWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Sunny emoji is displayed
        composeTestRule.onNodeWithText("☀️").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_weatherEmoji_matchesWeatherCondition_rainy() {
        // Given: Rainy weather
        val rainyWeather = Weather(
            locationName = "Zürich",
            temperature = 15.0,
            feelsLike = 13.0,
            description = "Leichter Regen",
            humidity = 85,
            iconUrl = "https://openweathermap.org/img/w/10d.png"
        )

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(rainyWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Rain emoji is displayed
        composeTestRule.onNodeWithText("🌧️").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_weatherEmoji_matchesWeatherCondition_cloudy() {
        // Given: Cloudy weather
        val cloudyWeather = Weather(
            locationName = "Zürich",
            temperature = 12.0,
            feelsLike = 11.0,
            description = "Bedeckt",
            humidity = 75,
            iconUrl = "https://openweathermap.org/img/w/04d.png"
        )

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(cloudyWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Cloud emoji is displayed
        composeTestRule.onNodeWithText("☁️").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_weatherIconRow_displaysChevronIcon() {
        // Given: Any weather state
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 20.0,
            feelsLike = 19.0,
            description = "Mäßig bewölkt",
            humidity = 60,
            iconUrl = "https://openweathermap.org/img/w/02d.png"
        )

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(testWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Chevron right icon is visible (indicates clickability)
        composeTestRule.onNodeWithContentDescription("Details anzeigen").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_weatherAndHeader_displayedTogether() {
        // Given: Weather success state
        val testWeather = Weather(
            locationName = "Zürich",
            temperature = 16.0,
            feelsLike = 15.0,
            description = "Bewölkt",
            humidity = 70,
            iconUrl = "https://openweathermap.org/img/w/03d.png"
        )

        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Success(testWeather),
                    permissionState = PermissionState.GRANTED_WHILE_IN_USE,
                    onWeatherClick = {}
                )
            }
        }

        // Then: Both weather and header are visible
        composeTestRule.onNodeWithText("16°C").assertIsDisplayed()
        composeTestRule.onNodeWithText("Heutiges Outfit").assertIsDisplayed()
        // Washing machine icon should also be visible
        composeTestRule.onNodeWithContentDescription("Zur Waschmaschine").assertIsDisplayed()
    }

    @Test
    fun fullOutfitScreen_rendersSingleHeader_only() {
        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = testTop,
                    pants = testPants,
                    weatherState = WeatherUiState.Loading,
                    onWeatherClick = {}
                )
            }
        }

        composeTestRule.onAllNodesWithText("Heutiges Outfit").assertCountEquals(1)
    }

    @Test
    fun fullOutfitScreen_emptyState_stillShowsWeatherRowTopLeft() {
        composeTestRule.setContent {
            LooksyTheme {
                FullOutfitScreen(
                    top = null,
                    pants = null,
                    skirt = null,
                    dress = null,
                    jacket = null,
                    weatherState = WeatherUiState.Loading,
                    permissionState = PermissionState.NOT_ASKED,
                    isLocationEnabled = false,
                    onWeatherClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Standortzugriff erforderlich").assertIsDisplayed()
    }
}
