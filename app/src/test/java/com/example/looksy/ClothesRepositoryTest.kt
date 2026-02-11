package com.example.looksy

import com.example.looksy.data.local.dao.ClothesDao
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.data.repository.ClothesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ClothesRepositoryTest {

    private lateinit var repository: ClothesRepository
    private lateinit var dao: ClothesDao

    private val testCloth = Clothes(
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

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        repository = ClothesRepository(dao)
    }

    @Test
    fun `insertAll() should call dao's insertAll`() = runTest {
        val list = listOf(testCloth)
        repository.insertAll(list)
        coVerify { dao.insertAll(list) }
    }

    @Test
    fun `deleteAll() should call dao's deleteAll`() = runTest {
        val list = listOf(testCloth)
        repository.deleteAll(list)
        coVerify { dao.deleteAll(list) }
    }

    @Test
    fun `updateAll() should call dao's updateAll`() = runTest {
        val list = listOf(testCloth)
        repository.updateAll(list)
        coVerify { dao.updateAll(list) }
    }
}
