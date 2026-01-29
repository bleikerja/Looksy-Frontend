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
                viewModel = clothesViewModel
            )
        }
    }
    @Test
    fun clothesPreferenceCounterWorks(){
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home.route)
        }

        composeTestRule.onNodeWithContentDescription("Outfit anziehen").performClick()

        coVerify (timeout = 2000){
            clothesViewModel.incrementClothesPreference(listOf(testTop, testPants))
            clothesViewModel.updateAll(any())
        }
    }
}


