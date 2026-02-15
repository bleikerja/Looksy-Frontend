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
        composeTestRule.onNodeWithText(" Trockner", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(" Waschen 40°C", substring = true, useUnmergedTree = true).performClick()
        // Close dropdown if necessary or just click outside, but MultiSelectDropdown might stay open
        // Try to click the header to close or just proceed if the button is visible
        
        // Check if "Speichern" button is enabled
        composeTestRule
            .onNodeWithText("Speichern")
            .assertIsEnabled()
    }
}
