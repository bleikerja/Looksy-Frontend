package com.example.looksy

import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.ui.viewmodel.ClothesViewModel
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
            washingNotes = listOf(WashingNotes.Temperature30),
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
            washingNotes = listOf(WashingNotes.Temperature30),
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
            washingNotes = listOf(WashingNotes.Temperature30),
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
    @Test
    fun `discardClothes() should delete from repository and set lastDiscardedClothes`() = testScope.runTest {
        // Given
        val clothes = Clothes(
            id = 1,
            size = Size._M,
            seasonUsage = Season.Summer,
            type = Type.Tops,
            material = Material.Cotton,
            clean = true,
            washingNotes = listOf(WashingNotes.Temperature30),
            imagePath = "",
            isSynced = false
        )
        val clothesList = listOf(clothes) // testCloth muss im @Before oder als Property definiert sein

        viewModel.discardClothes(clothesList)
        advanceUntilIdle()

        coVerify { repository.deleteAll(clothesList) }
        assertEquals(clothesList, viewModel.lastDiscardedClothes.value)
    }

    @Test
    fun `undoLastDiscard() should insert clothes back into repository`() = testScope.runTest {
        // Given
        val clothes = Clothes(
            id = 1,
            size = Size._M,
            seasonUsage = Season.Summer,
            type = Type.Tops,
            material = Material.Cotton,
            clean = true,
            washingNotes = listOf(WashingNotes.Temperature30),
            imagePath = "",
            isSynced = false
        )
        val clothesList = listOf(clothes)

        // Zuerst aussortieren, um einen Zustand zum Rückgängigmachen zu haben
        viewModel.discardClothes(clothesList)
        advanceUntilIdle()

        // Dann rückgängig machen
        viewModel.undoLastDiscard()
        advanceUntilIdle()

        coVerify { repository.insertAll(clothesList) }
        assertNull(viewModel.lastDiscardedClothes.value)
    }
}
