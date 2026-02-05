package com.example.looksy

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.looksy.data.model.*
import com.example.looksy.ui.navigation.NavGraph
import com.example.looksy.ui.navigation.Routes
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.viewmodel.OutfitViewModel
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for Weather navigation integration in NavGraph.
 * Tests routing from Home screen to WeatherScreen.
 */
class WeatherNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private val clothesViewModel = mockk<ClothesViewModel>(relaxed = true)
    private val outfitViewModel = mockk<OutfitViewModel>(relaxed = true)
    private val weatherViewModel = mockk<WeatherViewModel>(relaxed = true)

    private val testTop = Clothes(
        id = 1,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
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
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false
    )

    private val clothesFlow = MutableStateFlow(listOf(testTop, testPants))
    private val outfitsFlow = MutableStateFlow(emptyList<Outfit>())
    private val weatherStateFlow = MutableStateFlow<com.example.looksy.ui.viewmodel.WeatherUiState>(
        com.example.looksy.ui.viewmodel.WeatherUiState.Loading
    )

    @Before
    fun setupNavHost() {
        every { clothesViewModel.allClothes } returns clothesFlow
        every { outfitViewModel.allOutfits } returns outfitsFlow
        every { weatherViewModel.weatherState } returns weatherStateFlow

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            NavGraph(
                navController = navController,
                clothesViewModel = clothesViewModel,
                outfitViewModel = outfitViewModel,
                weatherViewModel = weatherViewModel
            )
        }
    }

    @Test
    fun navGraph_weatherRoute_exists() {
        // Given: NavHost is set up

        // When: Navigate to Weather route
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Weather.route)
        }

        // Then: Current route is Weather
        assertEquals(Routes.Weather.route, navController.currentBackStackEntry?.destination?.route)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navGraph_homeScreen_displaysWeatherIcon_andNavigatesToWeather() {
        // Given: Home screen is displayed with weather success state
        val testWeather = Weather(
            temperature = 20.0,
            feelsLike = 19.0,
            description = "Clear sky",
            humidity = 60,
            windSpeed = 2.5,
            pressure = 1012,
            cityName = "Zürich"
        )
        weatherStateFlow.value = com.example.looksy.ui.viewmodel.WeatherUiState.Success(testWeather)

        composeTestRule.waitForIdle()

        // Then: Weather temperature is visible on home screen
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("20°C"),
            timeoutMillis = 3000
        )

        // When: Weather icon is clicked
        composeTestRule.onNodeWithText("20°C").performClick()

        // Then: Navigation to Weather screen occurs
        composeTestRule.runOnUiThread {
            assertEquals(Routes.Weather.route, navController.currentBackStackEntry?.destination?.route)
        }

        // And: Weather screen content is displayed
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Wetter"),
            timeoutMillis = 3000
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navGraph_weatherScreen_displaysBackButton_andNavigatesBack() {
        // Given: Navigate to Weather screen
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Weather.route)
        }

        composeTestRule.waitForIdle()

        // Then: Weather screen header is visible
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Wetter"),
            timeoutMillis = 3000
        )

        // When: Back button is clicked
        composeTestRule.onNodeWithContentDescription("Zurück").performClick()

        // Then: Navigation back to Home occurs
        composeTestRule.runOnUiThread {
            assertEquals(Routes.Home.route, navController.currentBackStackEntry?.destination?.route)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navGraph_weatherScreen_receivesWeatherViewModel() {
        // Given: Weather state with data
        val testWeather = Weather(
            temperature = 15.0,
            feelsLike = 14.0,
            description = "Cloudy",
            humidity = 70,
            windSpeed = 3.0,
            pressure = 1010,
            cityName = "Zürich"
        )
        weatherStateFlow.value = com.example.looksy.ui.viewmodel.WeatherUiState.Success(testWeather)

        // When: Navigate to Weather screen
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Weather.route)
        }

        composeTestRule.waitForIdle()

        // Then: Weather data from ViewModel is displayed
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("15°C"),
            timeoutMillis = 3000
        )
        composeTestRule.onNodeWithText("Zürich").assertIsDisplayed()
    }

    @Test
    fun navGraph_weatherScreen_usesSharedWeatherViewModel() {
        // Given: Weather state on home screen
        val testWeather = Weather(
            temperature = 22.0,
            feelsLike = 21.0,
            description = "Sunny",
            humidity = 55,
            windSpeed = 2.0,
            pressure = 1015,
            cityName = "Zürich"
        )
        weatherStateFlow.value = com.example.looksy.ui.viewmodel.WeatherUiState.Success(testWeather)

        composeTestRule.waitForIdle()

        // When: Navigate to Weather screen
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Weather.route)
        }

        composeTestRule.waitForIdle()

        // Then: Same weather data is displayed (ViewModel is shared via ScreenBlueprint)
        composeTestRule.onNodeWithText("22°C").assertIsDisplayed()
    }

    @Test
    fun navGraph_weatherScreen_handlesDifferentStates() {
        // Given: Weather error state
        weatherStateFlow.value = com.example.looksy.ui.viewmodel.WeatherUiState.Error("Network error")

        // When: Navigate to Weather screen
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Weather.route)
        }

        composeTestRule.waitForIdle()

        // Then: Error state is displayed
        composeTestRule.onNode(
            hasText("Fehler beim Laden", substring = true) or
            hasText("Erneut versuchen")
        ).assertExists()
    }
}
