package com.example.looksy

import com.example.looksy.data.preferences.UserPreferencesRepository
import com.example.looksy.data.remote.api.WeatherApiService
import com.example.looksy.data.remote.dto.Main
import com.example.looksy.data.remote.dto.WeatherInfo
import com.example.looksy.data.remote.dto.WeatherResponse
import com.example.looksy.data.repository.WeatherRepository
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherFeatureIntegrationTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var apiService: WeatherApiService
    private lateinit var repository: WeatherRepository
    private lateinit var prefs: UserPreferencesRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        apiService = mockk()
        repository = WeatherRepository(apiService, "integration_key")
        prefs = mockk(relaxed = true)
        every { prefs.lastSearchedCity } returns flowOf("")
        every { prefs.lastSearchedLat } returns flowOf(null)
        every { prefs.lastSearchedLon } returns flowOf(null)
        viewModel = WeatherViewModel(repository, prefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel to repository to api flow returns success state`() = runTest {
        coEvery { apiService.getWeatherByLocation(52.52, 13.405, "integration_key", any(), any()) } returns
            WeatherResponse(
                name = "Berlin",
                main = Main(
                    temp = 15.5,
                    feels_like = 14.0,
                    temp_min = 13.0,
                    temp_max = 17.0,
                    humidity = 60
                ),
                weather = listOf(WeatherInfo(800, "Clear", "klarer Himmel", "01d"))
            )

        viewModel.fetchWeather(52.52, 13.405)
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals("Berlin", (state as WeatherUiState.Success).weather.locationName)
        assertEquals(15.5, state.weather.temperature, 0.01)
        assertEquals("klarer Himmel", state.weather.description)
    }

    @Test
    fun `viewModel to repository to api flow returns error state on exception`() = runTest {
        coEvery { apiService.getWeatherByLocation(any(), any(), any(), any()) } throws RuntimeException("API Error")

        viewModel.fetchWeather(52.52, 13.405)
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error)
        assertEquals("API Error", (state as WeatherUiState.Error).message)
    }
}
