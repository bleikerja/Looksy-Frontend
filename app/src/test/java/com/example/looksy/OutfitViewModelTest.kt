package com.example.looksy

import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Outfit
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.data.repository.OutfitRepository
import com.example.looksy.ui.viewmodel.ClothesViewModel
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
            id = 1,
            dressId = 1,
            topsId = 3,
            pantsId = 4,
            jacketId = 5,
            skirtId = 6,
        )

        // When
        viewModel.insert(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(outfit) }
    }

    @Test
    fun `update() should call repository's update`() = testScope.runTest {
        // Given
        val outfit = Outfit(
            id = 1,
            dressId = 1,
            topsId = 3,
            pantsId = 4,
            jacketId = 5,
            skirtId = 6,
        )

        // When
        viewModel.update(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.update(outfit) }
    }

    @Test
    fun `delete() should call repository's delete`() = testScope.runTest {
        // Given
        val outfit = Outfit(
            id = 1,
            dressId = 1,
            topsId = 3,
            pantsId = 4,
            jacketId = 5,
            skirtId = 6,
        )

        // When
        viewModel.delete(outfit)
        advanceUntilIdle()

        // Then
        coVerify { repository.delete(outfit) }
    }

    @Test
    fun `getOutfitById() should call repository's getOutfitById`() {
        // Given
        val id = 1

        // When
        viewModel.getOutfitById(id)

        // Then
        coVerify { repository.getOutfitById(id) }
    }

    @Test
    fun `incrementOutfitPreference() should call repository`() = testScope.runTest {
        val topId = 10
        val dressId = null
        val skirtId = 20
        val pantsId = null
        val jacketId = 30
        val pulloverId: Int? = null
        val shoesId: Int? = null
        viewModel.incrementOutfitPreference(
            selectedTopId = topId,
            selectedDressId = dressId,
            selectedSkirtId = skirtId,
            selectedPantsId = pantsId,
            selectedJacketId = jacketId,
            selectedPulloverId = pulloverId,
            selectedShoesId = shoesId
        )
        advanceUntilIdle()
        coVerify {
            repository.incrementOutfitPreference(
                selectedTopId = topId,
                selectedDressId = dressId,
                selectedSkirtId = skirtId,
                selectedPantsId = pantsId,
                selectedJacketId = jacketId,
                selectedPulloverId = pulloverId,
                selectedShoesId = shoesId
            )
        }
    }

    @Test
    fun `incrementOutfitPreference() should forward pulloverId`() = testScope.runTest {
        viewModel.incrementOutfitPreference(
            selectedTopId = null,
            selectedDressId = null,
            selectedSkirtId = null,
            selectedPantsId = 2,
            selectedJacketId = null,
            selectedPulloverId = 7,
            selectedShoesId = null
        )
        advanceUntilIdle()
        coVerify {
            repository.incrementOutfitPreference(
                selectedTopId = null,
                selectedDressId = null,
                selectedSkirtId = null,
                selectedPantsId = 2,
                selectedJacketId = null,
                selectedPulloverId = 7,
                selectedShoesId = null
            )
        }
    }
    @Test
    fun `incrementOutfitPreference() should handle all null IDs`() = testScope.runTest {

        // When
        viewModel.incrementOutfitPreference(
            selectedTopId = null,
            selectedDressId = null,
            selectedSkirtId = null,
            selectedPantsId = null,
            selectedJacketId = null,
            selectedPulloverId = null,
            selectedShoesId = null
        )
        advanceUntilIdle()

        // Then
        coVerify {
            repository.incrementOutfitPreference(
                selectedTopId = null,
                selectedDressId = null,
                selectedSkirtId = null,
                selectedPantsId = null,
                selectedJacketId = null,
                selectedPulloverId = null,
                selectedShoesId = null
            )
        }
    }
}
