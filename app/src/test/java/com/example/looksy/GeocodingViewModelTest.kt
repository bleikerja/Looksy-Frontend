package com.example.looksy

import com.example.looksy.data.location.Location
import com.example.looksy.data.repository.GeocodingRepository
import com.example.looksy.ui.viewmodel.GeocodingUiState
import com.example.looksy.ui.viewmodel.GeocodingViewModel
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeocodingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: GeocodingRepository
    private lateinit var viewModel: GeocodingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = GeocodingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCityCoordinates sets success when repository returns location`() = runTest {
        val location = Location(47.3769, 8.5417)
        coEvery { repository.getCityCoordinates("Zürich") } returns Result.success(location)

        viewModel.getCityCoordinates("Zürich")
        advanceUntilIdle()

        val state = viewModel.geocodingState.value
        assertTrue(state is GeocodingUiState.Success)
        assertEquals("Zürich", (state as GeocodingUiState.Success).cityName)
        assertEquals(location, state.location)
    }

    @Test
    fun `getCityCoordinates sets error when city name is blank`() = runTest {
        viewModel.getCityCoordinates("   ")

        val state = viewModel.geocodingState.value
        assertTrue(state is GeocodingUiState.Error)
        assertEquals("Bitte gib einen Stadtnamen ein", (state as GeocodingUiState.Error).message)
    }

    @Test
    fun `getCityCoordinates sets error when repository fails`() = runTest {
        coEvery { repository.getCityCoordinates("Basel") } returns Result.failure(Exception("City missing"))

        viewModel.getCityCoordinates("Basel")
        advanceUntilIdle()

        val state = viewModel.geocodingState.value
        assertTrue(state is GeocodingUiState.Error)
        assertEquals("City missing", (state as GeocodingUiState.Error).message)
    }

    @Test
    fun `getCityCoordinates uses fallback message when repository error has no message`() = runTest {
        coEvery { repository.getCityCoordinates("Xyz") } returns Result.failure(Exception())

        viewModel.getCityCoordinates("Xyz")
        advanceUntilIdle()

        val state = viewModel.geocodingState.value
        assertTrue(state is GeocodingUiState.Error)
        assertEquals("Fehler beim Suchen der Stadt", (state as GeocodingUiState.Error).message)
    }

    @Test
    fun `resetState sets idle`() = runTest {
        coEvery { repository.getCityCoordinates("Luzern") } returns Result.failure(Exception("No result"))
        viewModel.getCityCoordinates("Luzern")
        advanceUntilIdle()

        viewModel.resetState()

        assertTrue(viewModel.geocodingState.value is GeocodingUiState.Idle)
    }
}
