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
        isSynced = false
    )

    private val testPants = Clothes(
        id = 2, type = Type.Pants, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false
    )

    private val clothesFlow = MutableStateFlow(listOf(testTop, testPants))

    @Before
    fun setup() {
        every { clothesViewModel.allClothes } returns clothesFlow
        every { clothesViewModel.getClothesById(any()) } returns MutableStateFlow(testTop)

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