package com.example.looksy

import android.icu.util.TimeUnit
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
import com.example.looksy.util.OutfitResult
import com.example.looksy.util.generateRandomOutfit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
//import com.example.looksy.util.OutfitResultimport com.example.looksy.util.generateRandomOutfit

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val clothesViewModel = mockk<ClothesViewModel>(relaxed = true)
    private val outfitViewModel = mockk<OutfitViewModel>(relaxed = true)
    private lateinit var navController: TestNavHostController

    private val testTop = Clothes(
        id = 1,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}",
        isSynced = false,
        wornClothes = 5
    )

    private val testPants = Clothes(
        id = 2,
        type = Type.Pants,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false,
        wornClothes = 2
    )
    private val testSkirt = Clothes(
        id = 3,
        type = Type.Skirt,
        clean = true,
        size = Size._M,
        seasonUsage = Season.inBetween,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false
    )

    private val clothesFlow = MutableStateFlow(listOf(testTop, testPants, testSkirt))
    @Before
    fun setup() {
        mockkStatic("com.example.looksy.util.OutfitGeneratorKt")
        every { generateRandomOutfit(any()) } returns OutfitResult(
            top = testTop,
            pants = testPants,
            skirt = testSkirt,
            jacket = null,
            dress = null
        )
        every { clothesViewModel.allClothes } returns clothesFlow

        every { clothesViewModel.getClothesById(any()) } answers {
            val id = it.invocation.args[0] as Int
            MutableStateFlow(clothesFlow.value.find { it.id == id })
        }

        coEvery { clothesViewModel.getByIdDirect(any()) } answers {
            val id = it.invocation.args[0] as Int
            clothesFlow.value.find { it.id == id }
        }

        coEvery { clothesViewModel.delete(any()) } answers {
            val toDelete = it.invocation.args[0] as Clothes
            val currentList = clothesFlow.value.toMutableList()
            currentList.removeAll { item -> item.id == toDelete.id }
            clothesFlow.value = currentList
            mockk(relaxed = true)
        }

        coEvery { clothesViewModel.updateAll(any()) } answers {
            val updatedList = it.invocation.args[0] as List<Clothes>
            val currentList = clothesFlow.value.toMutableList()

            // Ersetze die alten Kleidungsstücke durch die neuen aus der `updatedList`
            updatedList.forEach { updatedItem ->
                val index = currentList.indexOfFirst { it.id == updatedItem.id }
                if (index != -1) {
                    currentList[index] = updatedItem
                }
            }
            // Aktualisiere den Flow, damit die UI und andere Testteile die Änderung sehen
            clothesFlow.value = currentList
            mockk(relaxed = true)
        }

        every { clothesViewModel.incrementClothesPreference(any()) } answers {
            val itemsToIncrement = it.invocation.args[0] as List<Clothes>
            val currentItems = clothesFlow.value.toMutableList()
            itemsToIncrement.forEach { item ->
                val index = currentItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    currentItems[index] =
                        currentItems[index].copy(wornClothes = currentItems[index].wornClothes + 1)
                }
            }
            clothesFlow.value = currentItems
        }

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            NavGraph(
                navController = navController,
                clothesViewModel = clothesViewModel,
                outfitViewModel = outfitViewModel
            )
        }
    }

    @Test
    fun confirmOutfit_triggersPreferenceIncrementAndSelectedUpdate() {
        // UI-Aktion: Nutze die ContentDescription aus deinem FullOutfitScreen
        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()

        // Verifizierung (coVerify für suspend Funktionen im Repository/ViewModel)
        coVerify(timeout = 2000) {
            outfitViewModel.incrementOutfitPreference(
                1, null, 3, 2,
                selectedJacketId = null
            )
            clothesViewModel.updateAll(
                match { updatedList ->
                    updatedList.size == 3 &&
                    updatedList.containsAll(listOf(testTop, testPants, testSkirt))
                    updatedList.all { it.selected }
                }
            )
        }
    }

    @Test
    fun selectedClothesDisplayCorrectDaysWorn() {
        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Details.createRoute(testTop.id))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1 Tag", substring = true).assertIsDisplayed()

        mockkStatic(System::class)
        every { System.currentTimeMillis() } returns System.currentTimeMillis() + 86400000

        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home.route)
            navController.navigate(Routes.Details.createRoute(testTop.id))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("3 Tage", substring = true).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectedClothesGetSetDirtyAfterChanging (){
        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Neues Outfit").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Welche Kleider sollen als schmutzig markiert werden?"))
        composeTestRule.onNodeWithText("Weiter").performClick()

        composeTestRule.waitForIdle()

        assert(clothesFlow.value.all { !it.clean })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun generatorUpdatesAfterAOutfitpartIsDeleted() {
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home.route)
        }
        composeTestRule.waitUntilAtLeastOneExists(hasClickAction())
        // 1. Home Screen: Klicke auf den Stift-Button des Rocks
        composeTestRule.onAllNodesWithContentDescription("Bearbeiten")[1].performClick()
        // 2. Details Screen
        composeTestRule.waitUntilExactlyOneExists(hasText("Details"), 5000)
        composeTestRule.onNodeWithContentDescription("Bearbeiten").performClick()
        // 3. Edit Screen
        composeTestRule.waitUntilExactlyOneExists(hasText("Bearbeiten"), 5000)
        composeTestRule.onNodeWithContentDescription("Löschen").performClick()
        // 4. Confirmation Dialog
        composeTestRule.onNodeWithText("Löschen").performClick()
        // VERIFIKATION: Prüfe nur die ID
        coVerify { clothesViewModel.delete(match { it.id == testSkirt.id }) }
        composeTestRule.waitForIdle()
        assert(navController.currentDestination?.route == Routes.Home.route)
        assert(clothesFlow.value.size == 2)
        assert(clothesFlow.value.none { it.id == testSkirt.id })
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


        // Wir klicken auf den Pfeil ("See more") in der Sektion
        composeTestRule.onAllNodes(
            hasContentDescription("See more") and hasAnySibling(
                hasText(
                    "Oberteil",
                    substring = true
                )
            )
        ).onFirst().performClick()

        Assert.assertEquals(
            Routes.SpecificCategory.route, navController.currentDestination?.route
        )
    }

    @Test
    fun navGraph_confirmOutfitSelection_returnsToHome() {
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Details.createRoute(1))
        }
        composeTestRule.waitForIdle()
        // Button-Text ist dynamisch: "${Type.Tops} auswählen" -> "Tops auswählen"
        composeTestRule.onNodeWithText("${Type.Tops} auswählen", ignoreCase = true)
            .performClick()

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
            composeTestRule.onAllNodesWithText(expectedErrorMessage, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(expectedErrorMessage, substring = true)
            .assertIsDisplayed()
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
        every { clothesViewModel.allClothes } returns MutableStateFlow(
            listOf(
                testTop, testPants
            )
        )

        // ContentDescription aus deinem FullOutfitScreen
        composeTestRule.onNodeWithContentDescription("Zufälliges Outfit generieren")
            .performClick()

        Assert.assertEquals(
            Routes.Home.route, navController.currentBackStackEntry?.destination?.route
        )
    }
}