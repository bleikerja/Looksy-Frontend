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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
        isSynced = false
    )

    private val testPants = Clothes(
        id = 2, type = Type.Pants, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false
    )
    private val testSkirt = Clothes(
        id = 3, type = Type.Skirt, clean = true, size = Size._M,
        seasonUsage = Season.inBetween, material = Material.Cotton,
        washingNotes = WashingNotes.Temperature30,
        imagePath = "android.resource://com.example.looksy/${R.drawable.jeans}",
        isSynced = false
    )

    private val clothesFlow = MutableStateFlow(listOf(testTop, testPants, testSkirt))

    @Before
    fun setup() {
        every { clothesViewModel.allClothes } returns clothesFlow

        // WICHTIG: Gib das richtige Kleidungsstück basierend auf der ID zurück
        every { clothesViewModel.getClothesById(any()) } answers {
            val id = it.invocation.args[0] as Int
            MutableStateFlow(clothesFlow.value.find { it.id == id })
        }

        // Mock für getByIdDirect (wird im Edit-Screen vor dem Löschen aufgerufen)
        io.mockk.coEvery { clothesViewModel.getByIdDirect(any()) } answers {
            val id = it.invocation.args[0] as Int
            clothesFlow.value.find { it.id == id }
        }
        io.mockk.coEvery { clothesViewModel.delete(any()) } answers {
            val toDelete = it.invocation.args[0] as Clothes
            val currentList = clothesFlow.value.toMutableList()
            currentList.removeAll { item -> item.id == toDelete.id }
            clothesFlow.value = currentList
            mockk<kotlinx.coroutines.Job>(relaxed = true)
        }

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            NavGraph(
                navController = navController,
                viewModel = clothesViewModel,
            )
        }
    }
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun GeneratorUpdatesAfterAOutfitpartIsDeleted (){
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home.route)
        }
        // 1. Home Screen: Klicke auf den Stift-Button des Rocks
        composeTestRule.onAllNodesWithContentDescription("")[1].performClick()
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
}