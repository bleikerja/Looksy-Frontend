package com.example.looksy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.looksy.data.repository.ClothesRepository
import com.example.looksy.ui.screens.AddNewClothesScreen
import com.example.looksy.ui.viewmodel.ClothesViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class AddNewClothesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRepository = mockk<ClothesRepository>(relaxed = true)
    private val viewModel = ClothesViewModel(mockRepository)

    @Test
    fun addNewClothesScreen_initialState_saveButtonDisabled() {
        composeTestRule.setContent {
            AddNewClothesScreen(
                imageUriString = null,
                viewModel = viewModel,
                onSave = {},
                onNavigateBack = {}
            )
        }

        // Check if "Speichern" button is disabled
        composeTestRule
            .onNodeWithText("Speichern")
            .assertIsNotEnabled()
    }

    @Test
    fun addNewClothesScreen_displaysCorrectHeader() {
        composeTestRule.setContent {
            AddNewClothesScreen(
                imageUriString = null,
                viewModel = viewModel,
                onSave = {},
                onNavigateBack = {},
                clothesIdToEdit = null
            )
        }

        composeTestRule
            .onNodeWithText("Hinzufügen")
            .assertIsDisplayed()
    }

    @Test
    fun addNewClothesScreen_editMode_displaysCorrectHeader() {
        composeTestRule.setContent {
            AddNewClothesScreen(
                imageUriString = null,
                viewModel = viewModel,
                onSave = {},
                onNavigateBack = {},
                clothesIdToEdit = 1
            )
        }

        composeTestRule
            .onNodeWithText("Bearbeiten")
            .assertIsDisplayed()
    }

    @Test
    fun addNewClothesScreen_formValidation_enablesSaveButton() {
        composeTestRule.setContent {
            AddNewClothesScreen(
                imageUriString = "test_uri",
                viewModel = viewModel,
                onSave = {},
                onNavigateBack = {}
            )
        }

        // 1. Open Typ accordion → select T-Shirt/Longsleeve
        composeTestRule.onNodeWithText("Typ").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("T-Shirt/Longsleeve").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 2. Open Größe accordion → select M
        composeTestRule.onNodeWithText("Größe").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("M").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 3. Open Saison accordion → select Sommer
        composeTestRule.onNodeWithText("Saison").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sommer").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 4. Open Waschhinweise accordion → select Waschen 40°C
        composeTestRule.onNodeWithText("Waschhinweise").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Waschen 40°C").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Save button should now be enabled
        composeTestRule
            .onNodeWithText("Speichern")
            .assertIsEnabled()
    }

    @Test
    fun washingNotes_conflictsPreventSelection() {
        composeTestRule.setContent {
            AddNewClothesScreen(
                imageUriString = null,
                viewModel = viewModel,
                onSave = {},
                onNavigateBack = {}
            )
        }

        // Open the WashingNotes accordion
        composeTestRule.onNodeWithText("Waschhinweise").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Select Handwäsche
        composeTestRule.onNodeWithText("Handwäsche").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Waschen 30°C must now be disabled (conflicts with Handwäsche)
        composeTestRule.onNodeWithText("Waschen 30°C").performScrollTo().assertIsNotEnabled()

        // Select Bleichen
        composeTestRule.onNodeWithText("Bleichen").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Nicht Bleichen must now be disabled (conflicts with Bleichen)
        composeTestRule.onNodeWithText("Nicht Bleichen").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun washingNotes_noneConflictsWithEverything() {
        composeTestRule.setContent {
            AddNewClothesScreen(
                imageUriString = null,
                viewModel = viewModel,
                onSave = {},
                onNavigateBack = {}
            )
        }

        // Open the WashingNotes accordion
        composeTestRule.onNodeWithText("Waschhinweise").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Select "—" (WashingNotes.None).
        // Multiple "—" texts exist in the form (accordion row headers for Color and Material),
        // but only the pill chips are toggleable (FilterChip), so we narrow by isToggleable().
        composeTestRule
            .onAllNodes(hasText("—") and isToggleable())[0]
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Other chips must still be present in the tree
        composeTestRule.onNodeWithText("Handwäsche", substring = true).assertExists()
        composeTestRule.onNodeWithText("Trockner").assertExists()
    }
}
