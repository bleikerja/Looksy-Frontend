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
import io.mockk.coEvery
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
class DiscardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val clothesViewModel = mockk<ClothesViewModel>(relaxed = true)
    private val outfitViewModel = mockk<OutfitViewModel>(relaxed = true)
    private lateinit var navController: TestNavHostController

    private val now = System.currentTimeMillis()
    private val moreThanOneYearAgo = now - (366L * 24 * 60 * 60 * 1000)
    private val recently = now - (10L * 24 * 60 * 60 * 1000)

    private val oldCloth = Clothes(
        id = 10,
        type = Type.Tops,
        clean = true,
        size = Size._M,
        seasonUsage = Season.Summer,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        lastWorn = moreThanOneYearAgo,
        imagePath = ""
    )

    private val newCloth = Clothes(
        id = 11,
        type = Type.Pants,
        clean = true,
        size = Size._M,
        seasonUsage = Season.Summer,
        material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        lastWorn = recently,
        imagePath = ""
    )

    private val clothesFlow = MutableStateFlow(listOf(oldCloth, newCloth))

    @Before
    fun setup() {
        every { clothesViewModel.allClothes } returns clothesFlow
        every { clothesViewModel.lastDiscardedClothes } returns MutableStateFlow(null)

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
    fun discardScreen_showsOnlyOldClothes() {
        // Navigiere zum Kleiderschrank
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.ChoseClothes.route)
        }
        
        // Klicke auf den Aussortieren-Button
        composeTestRule.onNodeWithText("Vorschläge zum Aussortieren").performClick()
        
        // Prüfe, ob wir auf dem Discard-Screen sind
        Assert.assertEquals(Routes.Discard.route, navController.currentDestination?.route)
        
        // Prüfe, ob das alte Kleidungsstück angezeigt wird (anhand der ID/Existenz im UI)
        // Da AsyncImage schwer zu testen ist ohne Resource-ID, prüfen wir die Liste
        // In unserem DiscardScreen wird ein Text angezeigt: "Diese Sachen hast du seit über einem Jahr nicht mehr getragen"
        composeTestRule.onNodeWithText("Diese Sachen hast du seit über einem Jahr nicht mehr getragen:", substring = true).assertIsDisplayed()
        
        // Wir können prüfen, ob ein Item selektierbar ist.
        // Da wir nur 1 altes Kleidungsstück haben, sollte nur 1 Item-Container da sein.
        // Wir klicken auf das Bild (WashingItemContainer Style)
        composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()
        
        // Button "Aussortieren (1)" sollte erscheinen
        composeTestRule.onNodeWithText("Aussortieren (1)", substring = true).assertIsDisplayed()
    }

    @Test
    fun discardConfirm_callsViewModelDiscard() {
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Discard.route)
        }

        // Klicke auf das erste Item (oldCloth)
        composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()
        
        // Klicke auf Bestätigen
        composeTestRule.onNodeWithText("Aussortieren (1)", substring = true).performClick()
        
        coVerify { clothesViewModel.discardClothes(match { it.first().id == oldCloth.id }) }
    }
    
    @Test
    fun undoButton_appearsWhenCanUndoIsTrue() {
        // Mocke canUndo = true
        every { clothesViewModel.lastDiscardedClothes } returns MutableStateFlow(listOf(oldCloth))
        
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Discard.route)
        }
        
        composeTestRule.onNodeWithText("Rückgängig", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Rückgängig", substring = true).performClick()
        
        coVerify { clothesViewModel.undoLastDiscard() }
    }
}
