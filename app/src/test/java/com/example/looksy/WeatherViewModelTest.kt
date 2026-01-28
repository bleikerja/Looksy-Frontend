package com.example.looksy

import com.example.looksy.data.model.Weather
import com.example.looksy.data.repository.WeatherRepository
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            description = "clear sky",
            humidity = 60,
            iconUrl = "https://openweathermap.org/img/w/01d.png"
        )
        coEvery { repository.getWeather(any(), any()) } returns flowOf(Result.success(testWeather))

        // When
        viewModel.fetchWeather(52.52, 13.405)
        advanceUntilIdle()

        // Then
        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Success, "Expected Success state but got ${state::class.simpleName}")
        assertEquals(testWeather, (state as WeatherUiState.Success).weather)
        assertEquals("Berlin", state.weather.locationName)
        assertEquals(15.5, state.weather.temperature)
    }

    @Test
    fun `fetchWeather() should update state to Error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getWeather(any(), any()) } returns
                flowOf(Result.failure(Exception(errorMessage)))

        // When
        viewModel.fetchWeather(52.52, 13.405)
        advanceUntilIdle()

        // Then
        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error, "Expected Error state but got ${state::class.simpleName}")
        assertEquals(errorMessage, (state as WeatherUiState.Error).message)
    }

    @Test
    fun `fetchWeather() should set Loading state initially`() = runTest {
        // Given
        val testWeather = Weather("Berlin", 15.5, 14.0, "clear", 60, "icon.png")
        coEvery { repository.getWeather(any(), any()) } returns flowOf(Result.success(testWeather))

        // When
        viewModel.fetchWeather(52.52, 13.405)
        // Don't advance - check immediate state

        // Then
        assertTrue(
            viewModel.weatherState.value is WeatherUiState.Loading,
            "Expected Loading state but got ${viewModel.weatherState.value::class.simpleName}"
        )
    }
}
