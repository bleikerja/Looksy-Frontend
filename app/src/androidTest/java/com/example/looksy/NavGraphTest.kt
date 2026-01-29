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
import com.example.looksy.ui.navigation.Routes
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.viewmodel.OutfitViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val clothesViewModel = mockk<ClothesViewModel>(relaxed = true)
    private val outfitViewModel = mockk<OutfitViewModel>(relaxed = true)
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
        every { clothesViewModel.getClothesById(any()) } returns MutableStateFlow(testTop)

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
            // WICHTIG: Behebt java.lang.ClassCastException
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            NavGraph(
                navController = navController,
                clothesViewModel = clothesViewModel,
                outfitViewModel = outfitViewModel
            )
        }
    }

    @Test
    fun confirmOutfit_triggersPreferenceIncrementAndCleanStatusUpdate() {
        // UI-Aktion: Nutze die ContentDescription aus deinem FullOutfitScreen
        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()

        // Verifizierung (coVerify für suspend Funktionen im Repository/ViewModel)
        coVerify(timeout = 2000) {
            outfitViewModel.incrementOutfitPreference(1, null, null, 2, null)
            clothesViewModel.updateAll(any())
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

    @Test
    fun navGraph_categoriesToSpecificCategory_passesCorrectType() {
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.ChoseClothes.route)
        }

        composeTestRule.onNodeWithText("Oberteil", substring = true).assertIsDisplayed()

        // Wir klicken auf den Pfeil ("See more") in der Sektion
        composeTestRule.onAllNodes(
            hasContentDescription("See more") and
                    hasAnySibling(hasText("Oberteil", substring = true))
        ).onFirst().performClick()

        Assert.assertEquals(Routes.SpecificCategory.route, navController.currentDestination?.route)
    }

    @Test
    fun navGraph_confirmOutfitSelection_returnsToHome() {
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Details.createRoute(1))
        }
        composeTestRule.waitForIdle()
        // Button-Text ist dynamisch: "${Type.Tops} auswählen" -> "Tops auswählen"
        composeTestRule.onNodeWithText("${Type.Tops} auswählen", ignoreCase = true).performClick()

        Assert.assertEquals(Routes.Home.route, navController.currentDestination?.route)
    }

    @Test
    fun navGraph_deselectLastTop_showsErrorAndStaysOnDetails() {
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Details.createRoute(1))
        }
        composeTestRule.onNodeWithText("Aus Outfit entfernen").performClick()

        // Route sollte gleich bleiben
        Assert.assertTrue(navController.currentDestination?.route?.contains("details") == true)

        val context = composeTestRule.activity
        val expectedErrorMessage = context.getString(R.string.error_cannot_deselect_last_item)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText(expectedErrorMessage, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(expectedErrorMessage, substring = true).assertIsDisplayed()
    }

    @Test
    fun navGraph_navigateToEdit_passesIdCorrectly() {
        val testId = 1
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Details.createRoute(testId))
        }

        composeTestRule.onNodeWithContentDescription("Bearbeiten").performClick()

        Assert.assertEquals(Routes.EditClothes.route, navController.currentDestination?.route)
        val argId = navController.currentBackStackEntry?.arguments?.getInt("id")
        Assert.assertEquals(testId, argId)
    }

    @Test
    fun navGraph_home_clickRandomize_triggersNoNavigationButLogic() {
        val testTop = Clothes(
            id = 1,
            type = Type.Tops,
            clean = true,
            size = Size._M,
            seasonUsage = Season.inBetween,
            material = Material.Cotton,
            washingNotes = WashingNotes.Dryer,
            imagePath = "",
            isSynced = false
        )
        val testPants = Clothes(
            id = 2,
            type = Type.Pants,
            clean = true,
            size = Size._M,
            seasonUsage = Season.inBetween,
            material = Material.Cotton,
            washingNotes = WashingNotes.Dryer,
            imagePath = "",
            isSynced = false
        )
        every { clothesViewModel.allClothes } returns MutableStateFlow(listOf(testTop, testPants))

        // ContentDescription aus deinem FullOutfitScreen
        composeTestRule.onNodeWithContentDescription("Zufälliges Outfit generieren").performClick()

        Assert.assertEquals(
            Routes.Home.route,
            navController.currentBackStackEntry?.destination?.route
        )
    }
    private fun assertEquals(expected: String?, actual: String?) {
        Assert.assertEquals(expected, actual)
    }
}