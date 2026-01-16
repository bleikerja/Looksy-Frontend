package com.example.looksy

import com.example.looksy.domain.repository.ClothesRepository
import com.example.looksy.model.Clothes
import com.example.looksy.model.Material
import com.example.looksy.model.Season
import com.example.looksy.model.Size
import com.example.looksy.model.Type
import com.example.looksy.model.WashingNotes
import com.example.looksy.presentation.viewmodel.ClothesViewModel
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
class ClothesViewModelTest {

    private lateinit var viewModel: ClothesViewModel
    private lateinit var repository: ClothesRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher.scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = ClothesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert() should call repository's insert`() = testScope.runTest {
        // Given
        val clothes = Clothes(
            id = 1,
            size = Size._M,
            seasonUsage = Season.Summer,
            type = Type.Tops,
            material = Material.Cotton,
            clean = true,
            washingNotes = WashingNotes.Temperature30,
            imagePath = "",
            isSynced = false
        )

        // When
        viewModel.insert(clothes)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(clothes) }
    }

    @Test
    fun `update() should call repository's update`() = testScope.runTest {
        // Given
        val clothes = Clothes(
            id = 1,
            size = Size._M,
            seasonUsage = Season.Summer,
            type = Type.Tops,
            material = Material.Cotton,
            clean = true,
            washingNotes = WashingNotes.Temperature30,
            imagePath = "",
            isSynced = false
        )

        // When
        viewModel.update(clothes)
        advanceUntilIdle()

        // Then
        coVerify { repository.update(clothes) }
    }

    @Test
    fun `delete() should call repository's delete`() = testScope.runTest {
        // Given
        val clothes = Clothes(
            id = 1,
            size = Size._M,
            seasonUsage = Season.Summer,
            type = Type.Tops,
            material = Material.Cotton,
            clean = true,
            washingNotes = WashingNotes.Temperature30,
            imagePath = "",
            isSynced = false
        )

        // When
        viewModel.delete(clothes)
        advanceUntilIdle()

        // Then
        coVerify { repository.delete(clothes) }
    }

    @Test
    fun `getClothesById() should call repository's getClothesById`() {
        // Given
        val id = 1

        // When
        viewModel.getClothesById(id)

        // Then
        coVerify { repository.getClothesById(id) }
    }
}
