package com.example.looksy

import com.example.looksy.Repository.ClothesRepository
import com.example.looksy.ViewModels.ClothesViewModel
import com.example.looksy.dataClassClones.Clothes
import com.example.looksy.dataClassClones.Type
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class ClothesViewModelTest {

    private lateinit var viewModel: ClothesViewModel
    private lateinit var repository: ClothesRepository
    private val testDispatcher = StandardTestDispatcher()

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
    fun `insert() should call repository's insert`() = runTest {
        // Given
        val clothes = Clothes(id = 1, name = "Test Shirt", imagePath = "", type = Type.SHIRT, lastWorn = Date())

        // When
        viewModel.insert(clothes)

        // Then
        coVerify { repository.insert(clothes) }
    }

    @Test
    fun `update() should call repository's update`() = runTest {
        // Given
        val clothes = Clothes(id = 1, name = "Updated Shirt", imagePath = "", type = Type.SHIRT, lastWorn = Date())

        // When
        viewModel.update(clothes)

        // Then
        coVerify { repository.update(clothes) }
    }

    @Test
    fun `delete() should call repository's delete`() = runTest {
        // Given
        val clothes = Clothes(id = 1, name = "Test Shirt", imagePath = "", type = Type.SHIRT, lastWorn = Date())

        // When
        viewModel.delete(clothes)

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
