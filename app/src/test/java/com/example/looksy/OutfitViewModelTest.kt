package com.example.looksy

import com.example.looksy.data.repository.OutfitRepository
import com.example.looksy.data.model.Outfit
import com.example.looksy.ui.viewmodel.OutfitViewModel
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class OutfitViewModelTest {

    private lateinit var viewModel: OutfitViewModel
    private lateinit var repository: OutfitRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher.scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = OutfitViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert() should call repository's insert`() = testScope.runTest {
        // Given
        val outfit = Outfit(
            id = 0,
            dressId = 1,
            topsId = 2,
            skirtId = null,
            pantsId = 3,
            jacketId = null,
            isSynced = false
        )

        // When
        viewModel.insert(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(outfit) }
    }

    @Test
    fun `insert() should save outfit with all clothing pieces`() = testScope.runTest {
        // Given
        val outfit = Outfit(
            id = 0,
            dressId = null,
            topsId = 1,
            skirtId = 2,
            pantsId = 3,
            jacketId = 4,
            isSynced = false
        )

        // When
        viewModel.insert(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(outfit) }
    }

    @Test
    fun `insert() should save outfit with only some clothing pieces`() = testScope.runTest {
        // Given
        val outfit = Outfit(
            id = 0,
            dressId = null,
            topsId = 1,
            skirtId = null,
            pantsId = 2,
            jacketId = null,
            isSynced = false
        )

        // When
        viewModel.insert(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(outfit) }
    }

    @Test
    fun `insert() should save outfit with dress only`() = testScope.runTest {
        // Given
        val outfit = Outfit(
            id = 0,
            dressId = 1,
            topsId = null,
            skirtId = null,
            pantsId = null,
            jacketId = null,
            isSynced = false
        )

        // When
        viewModel.insert(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(outfit) }
    }
}
