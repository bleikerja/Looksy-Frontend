package com.example.looksy

import com.example.looksy.data.local.dao.OutfitDao
import com.example.looksy.data.repository.OutfitRepository
import com.example.looksy.data.model.Outfit
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
    @Test
    fun `incrementOutfitPreference() should increment count and update when outfit exists`() = runTest {
        // Given: Ein existierendes Outfit mit aktuellem Score 5
        val existingOutfit = Outfit(
            id = 100,
            topsId = 1,
            pantsId = 2,
            preference = 5
        )

        // Mocking: Das DAO findet dieses Outfit
        coEvery {
            outfitDao.findMatchingOutfit(1, 2, null, null, null)
        } returns existingOutfit

        // When: Die Funktion wird aufgerufen
        outfitRepository.incrementOutfitPreference(1, 2, null, null, null)

        // Then: Verifiziere, dass update mit Score 6 gerufen wurde
        coVerify {
            outfitDao.update(match { it.id == 100 && it.preference == 6 })
        }
    }

    @Test
    fun `incrementOutfitPreference() should insert new outfit when no match found`() = runTest {
        // Given: Kein passendes Outfit in der Datenbank
        coEvery {
            outfitDao.findMatchingOutfit(any(), any(), any(), any(), any())
        } returns null

        // When: Die Funktion wird aufgerufen
        outfitRepository.incrementOutfitPreference(10, 20, null, null, null)

        // Then: Verifiziere, dass ein neues Outfit mit Score 1 eingefügt wurde
        coVerify {
            outfitDao.insert(match {
                it.topsId == 10 && it.pantsId == 20 && it.preference == 1
            })
        }
    }
}
