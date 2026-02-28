package com.example.looksy

import com.example.looksy.data.model.Weather
import com.example.looksy.data.repository.WeatherRepository
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for WeatherViewModel
 * 
 * Tests the ViewModel's ability to:
 * - Fetch weather data successfully
 * - Handle API errors gracefully
 * - Manage loading states correctly
 */
@ExperimentalCoroutinesApi
class WeatherViewModelTest {

    private lateinit var viewModel: WeatherViewModel
    private lateinit var repository: WeatherRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = WeatherViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchWeather() should update state to Loading then Success`() = runTest {
        // Given
        val testWeather = Weather(
            locationName = "Berlin",
            temperature = 15.5,
            feelsLike = 14.0,
            description = "klarer Himmel",
            humidity = 60,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )
        coEvery { repository.getWeather(any(), any()) } returns testWeather

        // When
        viewModel.fetchWeather(52.52, 13.405)
        advanceUntilIdle()

        // Then
        val state = viewModel.weatherState.value
        assertTrue("Expected Success state but got ${state::class.simpleName}", state is WeatherUiState.Success)
        assertEquals(testWeather, (state as WeatherUiState.Success).weather)
        assertEquals("Berlin", state.weather.locationName)
        assertEquals(15.5, state.weather.temperature, 0.01)
    }

    @Test
    fun `fetchWeather() should update state to Error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getWeather(any(), any()) } throws Exception(errorMessage)

        // When
        viewModel.fetchWeather(52.52, 13.405)
        advanceUntilIdle()

        // Then
        val state = viewModel.weatherState.value
        assertTrue("Expected Error state but got ${state::class.simpleName}", state is WeatherUiState.Error)
        assertEquals(errorMessage, (state as WeatherUiState.Error).message)
    }

    @Test
    fun `fetchWeather() should set Loading state initially`() = runTest {
        // Given
        val testWeather = Weather("Berlin", 15.5, 14.0, "klarer Himmel", 60, "https://openweathermap.org/img/w/01d.png")
        coEvery { repository.getWeather(any(), any()) } returns testWeather

        // When
        viewModel.fetchWeather(52.52, 13.405)
        // Don't advance - check immediate state

        // Then
        assertTrue(
            "Expected Loading state but got ${viewModel.weatherState.value::class.simpleName}",
            viewModel.weatherState.value is WeatherUiState.Loading
        )
    }
}
