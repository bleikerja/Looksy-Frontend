package com.example.looksy

import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.ui.navigation.NavGraph
import com.example.looksy.ui.navigation.Routes
import com.example.looksy.ui.viewmodel.ClothesViewModel
import com.example.looksy.ui.viewmodel.OutfitViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.looksy.R
import io.mockk.clearMocks
import org.junit.After

class NavGraphIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController
    private val clothesViewModel = mockk<ClothesViewModel>(relaxed = true)
    private val outfitViewModel = mockk<OutfitViewModel>(relaxed = true)

    // Testdaten mit gültigen Bildpfaden, damit die Komponenten gerendert werden und klickbar sind
    private val testTop = Clothes(
        id = 1, type = Type.Tops, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.shirt_category}", 
        isSynced = false
    )

    private val testPants = Clothes(
        id = 2, type = Type.Pants, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = listOf(WashingNotes.Temperature30),
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}", 
        isSynced = false
    )

    private lateinit var clothesFlow:MutableStateFlow<List<Clothes>>
    private lateinit var lastDiscardedState: MutableState<List<Clothes>?>

    @Before
    fun setupNavHost() {
        clothesFlow = MutableStateFlow(listOf(testTop, testPants))
        lastDiscardedState = mutableStateOf(null)
        // Mocks konfigurieren
        every { clothesViewModel.allClothes } returns clothesFlow
        every { clothesViewModel.getClothesById(any()) } returns MutableStateFlow(testTop)
        every { clothesViewModel.lastDiscardedClothes } returns lastDiscardedState

        composeTestRule.runOnUiThread {
            navController = TestNavHostController(composeTestRule.activity).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
        }

        composeTestRule.setContent {
            val context = LocalContext.current
            val controller = androidx.compose.runtime.remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            navController = controller

            NavGraph(
                navController = navController,
                clothesViewModel = clothesViewModel,
                outfitViewModel = outfitViewModel
            )
        }
    }

    @After
    fun tearDown() {
        // Mocks nach jedem Test zurücksetzen, um Seiteneffekte zu vermeiden
        clearMocks(clothesViewModel, outfitViewModel)
    }

    @Test
    fun navGraph_startDestination_isHome() {
        assertEquals(Routes.Home.route, navController.currentDestination?.route)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navGraph_navigateToDetails_works() {
        // Erzwinge den Zustand "Outfit ausgewählt"
        val selectedTop = testTop.copy(selected = true)
        val selectedPants = testPants.copy(selected = true)
        clothesFlow.value = listOf(selectedTop, selectedPants)
        composeTestRule.waitForIdle()

        // Warten, bis das Outfit gerendert wurde
        composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Kleidungsstück"), timeoutMillis = 10000)

        // Klick auf den LooksyButton (IconButton) im OutfitPart
        // Wir schließen den "Zur Waschmaschine" Button im Header aus
        composeTestRule.onAllNodes(
            hasClickAction() and 
            hasAnySibling(hasContentDescription("Kleidungsstück")) and 
            !hasContentDescription("Zur Waschmaschine")
        )
            .onFirst()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            navController.currentDestination?.route == Routes.Details.route
        }
        
        // Assertions gegen das Routen-Template
        assertEquals(Routes.Details.route, navController.currentDestination?.route)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navGraph_onConfirm_works() {
        // Warten, bis der Button sichtbar ist (Outfit vorhanden)
        composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Outfit anziehen"), 5000)

        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()

        composeTestRule.waitForIdle()

        // Verifizieren, dass die ViewModel-Methoden aufgerufen wurden
        coVerify(exactly = 1) {
            outfitViewModel.incrementOutfitPreference(any(), any(), any(), any(), any())
            clothesViewModel.updateAll(any())
        }
    }

    @Test
    fun navGraph_onGenerateRandom_works() {
        composeTestRule.onNodeWithContentDescription("Zufälliges Outfit generieren").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Routes.Home.route, navController.currentDestination?.route)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navGraph_navigationToScan_works() {
        // Empty State erzwingen
        clothesFlow.value = emptyList()


        composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Zur Kamera"), 5000)
        composeTestRule.onNodeWithContentDescription("Zur Kamera").performClick()

        composeTestRule.waitForIdle()
        assertEquals(Routes.Scan.route, navController.currentDestination?.route)
    }

    @Test
    fun navGraph_washingMachine_confirmsAndSetsLastWorn() {
        // Falls testTop in der Setup-Methode clean=true ist, hier für diesen Test überschreiben:
        val dirtyCloth = testTop.copy(clean = false)
        clothesFlow.value = listOf(dirtyCloth)

        // Navigiere zur Waschmaschine
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.WashingMachine.route)
        }

        // Wähle das Kleidungsstück in der Liste aus
        composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()

        // Klicke auf den Bestätigungsbutton ("Gewaschen (1)")
        composeTestRule.onNodeWithText("Gewaschen (1)", substring = true).performClick()

        // Verifiziere, dass das ViewModel mit clean = true UND einem Zeitstempel in lastWorn aktualisiert wurde
        coVerify {
            clothesViewModel.update(match { it.clean && it.lastWorn != null })
        }
    }

    @Test
    fun navGraph_navigateToDiscard_works() {
        // Navigiere zum Kleiderschrank (ChoseClothes)
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.ChoseClothes.route)
        }

        // Klicke auf den Button für die Aussortier-Vorschläge
        composeTestRule.onNodeWithContentDescription("Vorschläge zum Aussortieren").performClick()

        // Überprüfe, ob die Route jetzt auf dem Discard-Screen ist
        assertEquals(Routes.Discard.route, navController.currentDestination?.route)
    }
}
