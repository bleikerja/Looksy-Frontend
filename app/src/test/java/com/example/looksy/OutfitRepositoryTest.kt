package com.example.looksy

import com.example.looksy.data.local.dao.OutfitDao
import com.example.looksy.domain.repository.OutfitRepository
import com.example.looksy.model.Outfit
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class OutfitRepositoryTest {

    private lateinit var outfitDao: OutfitDao
    private lateinit var outfitRepository: OutfitRepository

    @Before
    fun setUp() {
        outfitDao = mockk(relaxed = true)
        outfitRepository = OutfitRepository(outfitDao)
    }

    @Test
    fun `insert() should call dao's insert`() = runTest {
        // Given
        val outfit = Outfit(
            id = 0,
            dressId = 1,
            topsId = 2,
            skirtId = null,
            pantsId = 3,
            jacketId = null
        )

        // When
        outfitRepository.insert(outfit)

        // Then
        coVerify { outfitDao.insert(outfit) }
    }

    @Test
    fun `outfit can be stored with all IDs null`() = runTest {
        // Given - Outfit mit allen IDs null (alle Kleidungsstücktypen können leer sein)
        val outfit = Outfit(
            id = 0,
            dressId = null,
            topsId = null,
            skirtId = null,
            pantsId = null,
            jacketId = null
        )

        // When
        outfitRepository.insert(outfit)

        // Then
        coVerify { outfitDao.insert(outfit) }
    }

    @Test
    fun `outfit can be stored with only some IDs`() = runTest {
        // Given - Outfit mit nur einigen IDs
        val outfit = Outfit(
            id = 0,
            dressId = null,
            topsId = 1,
            skirtId = null,
            pantsId = 2,
            jacketId = null
        )

        // When
        outfitRepository.insert(outfit)

        // Then
        coVerify { outfitDao.insert(outfit) }
    }

    @Test
    fun `getAllOutfits() should return flow from dao`() = runTest {
        // Given
        val outfits = listOf(
            Outfit(id = 1, dressId = 1, topsId = null, skirtId = null, pantsId = null, jacketId = null),
            Outfit(id = 2, dressId = null, topsId = 2, pantsId = 3, skirtId = null, jacketId = null)
        )
        coEvery { outfitDao.getAllOutfits() } returns flowOf(outfits)

        // When
        val result = outfitRepository.allOutfits

        // Then
        assertNotNull(result)
        coVerify { outfitDao.getAllOutfits() }
    }

    @Test
    fun `getOutfitById() should call dao's getOutfitById`() = runTest {
        // Given
        val id = 1
        coEvery { outfitDao.getOutfitById(id) } returns flowOf(
            Outfit(id = 1, dressId = 1, topsId = null, skirtId = null, pantsId = null, jacketId = null)
        )

        // When
        val result = outfitRepository.getOutfitById(id)

        // Then
        assertNotNull(result)
        coVerify { outfitDao.getOutfitById(id) }
    }

    @Test
    fun `update() should call dao's update`() = runTest {
        // Given
        val outfit = Outfit(
            id = 1,
            dressId = 1,
            topsId = 2,
            skirtId = null,
            pantsId = null,
            jacketId = null
        )

        // When
        outfitRepository.update(outfit)

        // Then
        coVerify { outfitDao.update(outfit) }
    }

    @Test
    fun `delete() should call dao's delete`() = runTest {
        // Given
        val outfit = Outfit(
            id = 1,
            dressId = 1,
            topsId = null,
            skirtId = null,
            pantsId = null,
            jacketId = null
        )

        // When
        outfitRepository.delete(outfit)

        // Then
        coVerify { outfitDao.delete(outfit) }
    }

    @Test
    fun `getByIdDirect() should call dao's getByIdDirect`() = runTest {
        // Given
        val id = 1
        val outfit = Outfit(id = 1, dressId = 1, topsId = null, skirtId = null, pantsId = null, jacketId = null)
        coEvery { outfitDao.getByIdDirect(id) } returns outfit

        // When
        val result = outfitRepository.getByIdDirect(id)

        // Then
        assertEquals(outfit, result)
        coVerify { outfitDao.getByIdDirect(id) }
    }
}
