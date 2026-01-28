package com.example.looksy

import com.example.looksy.data.remote.api.WeatherApiService
import com.example.looksy.data.remote.dto.Main
import com.example.looksy.data.remote.dto.WeatherInfo
import com.example.looksy.data.remote.dto.WeatherResponse
import com.example.looksy.data.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for WeatherRepository
 * 
 * Tests the Repository's ability to:
 * - Transform API responses into domain models
 * - Handle successful API calls
 * - Handle API failures
 * - Handle edge cases (empty weather list)
 */
class WeatherRepositoryTest {

    private lateinit var repository: WeatherRepository
    private lateinit var apiService: WeatherApiService
    private val testApiKey = "test_api_key_12345"

    @Before
    fun setup() {
        apiService = mockk()
        repository = WeatherRepository(apiService, testApiKey)
    }

    @Test
    fun `getWeather() should return Success when API call succeeds`() = runTest {
        // Given
        val mockResponse = WeatherResponse(
            name = "Berlin",
            main = Main(
                temp = 15.5,
                feels_like = 14.0,
                temp_min = 13.0,
                temp_max = 17.0,
                humidity = 60
            ),
            weather = listOf(
                WeatherInfo(
                    id = 800,
                    main = "Clear",
                    description = "clear sky",
                    icon = "01d"
                )
            )
        )
        coEvery {
            apiService.getWeatherByLocation(52.52, 13.405, testApiKey)
        } returns mockResponse

        // When
        val result = repository.getWeather(52.52, 13.405).first()

        // Then
        assertTrue(result.isSuccess, "Expected successful result")
        val weather = result.getOrNull()
        assertNotNull(weather, "Weather should not be null")
        assertEquals("Berlin", weather.locationName)
        assertEquals(15.5, weather.temperature)
        assertEquals(14.0, weather.feelsLike)
        assertEquals("clear sky", weather.description)
        assertEquals(60, weather.humidity)
        assertEquals("https://openweathermap.org/img/w/01d.png", weather.iconUrl)
    }

    @Test
    fun `getWeather() should return Failure when API call fails`() = runTest {
        // Given
        val exception = Exception("Network timeout")
        coEvery {
            apiService.getWeatherByLocation(any(), any(), any())
        } throws exception

        // When
        val result = repository.getWeather(52.52, 13.405).first()

        // Then
        assertTrue(result.isFailure, "Expected failed result")
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getWeather() should handle empty weather list`() = runTest {
        // Given
        val mockResponse = WeatherResponse(
            name = "Berlin",
            main = Main(15.5, 14.0, 13.0, 17.0, 60),
            weather = emptyList() // Edge case - no weather info
        )
        coEvery {
            apiService.getWeatherByLocation(any(), any(), any())
        } returns mockResponse

        // When
        val result = repository.getWeather(52.52, 13.405).first()

        // Then
        assertTrue(result.isSuccess, "Expected successful result even with empty weather list")
        val weather = result.getOrNull()
        assertNotNull(weather)
        assertEquals("", weather.description, "Description should be empty string")
        assertEquals("https://openweathermap.org/img/w/null.png", weather.iconUrl, "Icon URL should contain 'null'")
    }
}
