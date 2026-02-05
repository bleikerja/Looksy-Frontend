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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Instrumented integration tests for Weather feature.
 * Tests full flow: ViewModel → Repository → API (mocked).
 * Runs on device/emulator to test real coroutine behavior.
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
    fun weatherFlow_withSuccessfulAPI_returnsWeatherData() = runTest {
        // Given: Mock API returns Berlin weather data
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

        // When: Fetch weather through ViewModel
        viewModel.fetchWeather(52.52, 13.405)
        delay(200)

        // Then: State is Success with correct weather data
        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals(15.5, (state as WeatherUiState.Success).weather.temperature)
        assertEquals("clear sky", state.weather.description)
        assertEquals("Berlin", state.weather.locationName)
    }

    @Test
    fun weatherFlow_withAPIError_returnsErrorState() = runTest {
        // Given: Mock API throws exception
        coEvery {
            mockApiService.getWeatherByLocation(any(), any(), any())
        } throws Exception("API Error")

        // When: Fetch weather through ViewModel
        viewModel.fetchWeather(52.52, 13.405)
        delay(200)

        // Then: State is Error with correct message
        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error)
        assertEquals("API Error", (state as WeatherUiState.Error).message)
    }
}
