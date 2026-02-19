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

        // Fill in Size
        composeTestRule.onNodeWithText("Größe").performClick()
        composeTestRule.onNodeWithText("M").performClick()

        // Fill in Season
        composeTestRule.onNodeWithText("Saison").performClick()
        composeTestRule.onNodeWithText("Sommer").performClick()

        // Fill in Type
        composeTestRule.onNodeWithText("Typ").performClick()
        composeTestRule.onNodeWithText("Oberteil").performClick()

        // Fill in Material
        composeTestRule.onNodeWithText("Material").performClick()
        composeTestRule.onNodeWithText("Baumwolle").performClick()

        // Fill in WashingNotes
        composeTestRule.onNodeWithText("Waschhinweise").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(" Waschen 40°C", substring = true).performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        // 2. MANUELLER SCROLL: Um tiefer liegende Elemente wie " Kein Trockner" sicher zu erreichen,
        // scrollen wir den Container im Popup manuell per Swipe nach oben.
        composeTestRule.onNode(hasScrollAction() and hasAnyAncestor(isPopup()))
            .performTouchInput {
                swipeUp(durationMillis = 1000)
            }
        composeTestRule.waitForIdle()

        // 3. Ziel-Element auswählen. Exakter Text verhindert Verwechslungen.
        composeTestRule.onNode(hasText(" Bleichen") and hasAnyAncestor(isPopup()))
            .performScrollTo() // Falls der Swipe noch nicht ganz gereicht hat
            .performClick()
        composeTestRule.waitForIdle()

        // Dropdown schließen, damit der Speichern-Button nicht überlagert wird
        composeTestRule.onNodeWithText("Waschhinweise").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitForIdle()
        // Close dropdown if necessary or just click outside, but MultiSelectDropdown might stay open
        // Check if "Speichern" button is enabled
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

        // Open WashingNotes dropdown
        composeTestRule.onNodeWithText("Waschhinweise").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        // 1. Test Handwäsche vs Temperature washing
        // Select Handwäsche - use leading space and substring to be robust
        composeTestRule.onNodeWithText(" Handwäsche", substring = true).performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        // Check if "Waschen 30°C" is disabled
        composeTestRule.onNodeWithText(" Waschen 30°C", substring = true).performScrollTo()
            .assertIsNotEnabled()
        // 2. MANUELLER SCROLL: Um tiefer liegende Elemente wie " Kein Trockner" sicher zu erreichen,
        // scrollen wir den Container im Popup manuell per Swipe nach oben.
        composeTestRule.onNode(hasScrollAction() and hasAnyAncestor(isPopup()))
            .performTouchInput {
                swipeUp(durationMillis = 1000)
            }
        composeTestRule.waitForIdle()

        // 3. Ziel-Element auswählen. Exakter Text verhindert Verwechslungen.
        composeTestRule.onNode(hasText(" Bleichen") and hasAnyAncestor(isPopup()))
            .performScrollTo() // Falls der Swipe noch nicht ganz gereicht hat
            .performClick()
        composeTestRule.waitForIdle()

        // Check if "Kein Trockner" is disabled
        composeTestRule.onNodeWithText(" Nicht Bleichen", substring = true).performScrollTo()
            .assertIsNotEnabled()
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

        // Open WashingNotes dropdown
        composeTestRule.onNodeWithText("Waschhinweise").performScrollTo().performClick()

        // Select "-" (None)
        // Since it's exactly "-", we use substring=false or careful matching
        composeTestRule.onNodeWithText(" -", useUnmergedTree = true).performClick()

        // Check if other options are disabled
        composeTestRule.onNodeWithText("Handwäsche", substring = true).assertIsNotEnabled()
        composeTestRule.onNodeWithText(" Trockner").assertIsNotEnabled()
    }
}
