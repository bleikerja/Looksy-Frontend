package com.example.looksy

import com.example.looksy.data.remote.api.GeocodingApiService
import com.example.looksy.data.remote.api.GeocodingResponse
import com.example.looksy.data.repository.GeocodingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeocodingRepositoryTest {

    private lateinit var repository: GeocodingRepository
    private lateinit var apiService: GeocodingApiService
    private val testApiKey = "test_api_key"

    @Before
    fun setup() {
        apiService = mockk()
        repository = GeocodingRepository(apiService, testApiKey)
    }

    @Test
    fun `getCityCoordinates returns location when city exists`() = runTest {
        coEvery { apiService.getCityCoordinates("Berlin", 1, testApiKey) } returns listOf(
            GeocodingResponse(
                name = "Berlin",
                lat = 52.52,
                lon = 13.405,
                country = "DE"
            )
        )

        val result = repository.getCityCoordinates("Berlin")

        assertTrue(result.isSuccess)
        val location = result.getOrNull()
        assertNotNull(location)
        assertEquals(52.52, location!!.latitude, 0.0001)
        assertEquals(13.405, location.longitude, 0.0001)
    }

    @Test
    fun `getCityCoordinates returns failure when city does not exist`() = runTest {
        coEvery { apiService.getCityCoordinates("Unknown", 1, testApiKey) } returns emptyList()

        val result = repository.getCityCoordinates("Unknown")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Stadt nicht gefunden") == true)
    }

    @Test
    fun `getCityCoordinates returns failure when api throws`() = runTest {
        coEvery { apiService.getCityCoordinates(any(), any(), any()) } throws RuntimeException("Network error")

        val result = repository.getCityCoordinates("Zurich")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
