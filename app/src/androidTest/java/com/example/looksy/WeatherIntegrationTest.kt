/*
package com.example.looksy

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.looksy.data.remote.api.WeatherApiService
import com.example.looksy.data.remote.dto.Main
import com.example.looksy.data.remote.dto.WeatherInfo
import com.example.looksy.data.remote.dto.WeatherResponse
import com.example.looksy.data.repository.WeatherRepository
import com.example.looksy.ui.viewmodel.WeatherUiState
import com.example.looksy.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Instrumented integration test for Weather feature
 *
 * Tests the full flow from ViewModel -> Repository -> API (mocked)
 * This runs on an Android device/emulator to test real coroutine behavior
 * and Android-specific components, but with a mocked API service.
 */
@RunWith(AndroidJUnit4::class)
class WeatherIntegrationTest {

    private lateinit var context: Context
    private lateinit var mockApiService: WeatherApiService
    private lateinit var repository: WeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        mockApiService = mockk()
        repository = WeatherRepository(mockApiService, "test_api_key")
        viewModel = WeatherViewModel(repository)
    }

    @Test
    fun fullWeatherFlow_withMockedAPI_returnsWeatherData() = runTest {
        // Given - Mock API response
        val mockResponse = WeatherResponse(
            name = "Berlin",
            main = Main(15.5, 14.0, 13.0, 17.0, 60),
            weather = listOf(
                WeatherInfo(800, "Clear", "clear sky", "01d")
            )
        )
        coEvery {
            mockApiService.getWeatherByLocation(any(), any(), any())
        } returns mockResponse

        // When - Fetch weather through ViewModel
        viewModel.fetchWeather(52.52, 13.405)
        delay(200) // Allow time for Flow collection and state update

        // Then - Verify final state
        val state = viewModel.weatherState.value
        assertTrue(
            state is WeatherUiState.Success,
            "Expected Success state but got ${state::class.simpleName}"
        )
//        assertEquals("Berlin", (state as WeatherUiState.Success).weather.locationName)
        assertEquals(15.5, state.weather.temperature)
        assertEquals("clear sky", state.weather.description)
    }

    @Test
    fun fullWeatherFlow_withAPIError_returnsErrorState() = runTest {
        // Given - Mock API failure
        coEvery {
            mockApiService.getWeatherByLocation(any(), any(), any())
        } throws Exception("API Error")

        // When - Fetch weather through ViewModel
        viewModel.fetchWeather(52.52, 13.405)
        delay(200) // Allow time for Flow collection and state update

        // Then - Verify error state
        val state = viewModel.weatherState.value
        assertTrue(
            state is WeatherUiState.Error,
            "Expected Error state but got ${state::class.simpleName}"
        )
        assertEquals("API Error", (state as WeatherUiState.Error).message)
    }
}
*/
