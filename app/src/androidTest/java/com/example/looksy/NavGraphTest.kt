package com.example.looksy

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.looksy.data.model.*
import com.example.looksy.ui.navigation.NavGraph
import com.example.looksy.ui.viewmodel.ClothesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val clothesViewModel = mockk<ClothesViewModel>(relaxed = true)

    private lateinit var navController: TestNavHostController

    private val testTop = Clothes(
        id = 1, type = Type.Tops, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false,
        wornClothes = 5 // Startwert für den Test
    )

    private val testPants = Clothes(
        id = 2, type = Type.Pants, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false,
        wornClothes = 2 // Startwert für den Test
    )

    private val clothesFlow = MutableStateFlow(listOf(testTop, testPants))

    @Before
    fun setup() {
        every { clothesViewModel.allClothes } returns clothesFlow
        
        // Mock für incrementClothesPreference: Aktualisiert den State im Flow
        every { clothesViewModel.incrementClothesPreference(any()) } answers {
            val itemsToIncrement = it.invocation.args[0] as List<Clothes>
            val currentItems = clothesFlow.value.toMutableList()
            itemsToIncrement.forEach { item ->
                val index = currentItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    val current = currentItems[index]
                    currentItems[index] = current.copy(wornClothes = current.wornClothes + 1)
                }
            }
            clothesFlow.value = currentItems
        }

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            NavGraph(
                navController = navController,
                viewModel = clothesViewModel
            )
        }
    }

    @Test
    fun clothesPreferenceIncrementsOnConfirm() {
        // 1. Ursprüngliche Werte festhalten
        val initialTopWornCount = testTop.wornClothes
        val initialPantsWornCount = testPants.wornClothes

        // 2. Home Screen: Klick auf "Outfit anziehen"
        // Dies triggert im NavGraph: viewModel.incrementClothesPreference(wornClothesList)
        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()
        composeTestRule.waitForIdle()
        // 3. Verifikation: incrementClothesPreference wurde mit den richtigen IDs aufgerufen
        verify {
            clothesViewModel.incrementClothesPreference(match { list ->
                list.any { it.id == testTop.id } && list.any { it.id == testPants.id }
            })
        }
        
        // 4. Verifikation: Die Werte im Flow haben sich tatsächlich erhöht
        val updatedTop = clothesFlow.value.find { it.id == testTop.id }
        val updatedPants = clothesFlow.value.find { it.id == testPants.id }
        
        assert(updatedTop?.wornClothes == initialTopWornCount + 1)
        assert(updatedPants?.wornClothes == initialPantsWornCount + 1)
    }
}
