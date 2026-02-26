package com.example.looksy

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import com.example.looksy.ui.components.ConfirmationDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confirmationDialog_displaysContent_andHandlesClicks() {
        var confirmed = false
        var dismissed = false

        composeTestRule.setContent {
            ConfirmationDialog (
                title = "Löschen bestätigen",
                text = "Möchtest du dieses Kleidungsstück wirklich endgültig löschen?",
                dismissText = "Nein",
                onDismiss = { dismissed = true },
                confirmText = "Ja",
                onConfirm = { confirmed = true }
            )
        }

        // Title & Text sichtbar
        composeTestRule
            .onNodeWithText("Löschen bestätigen")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Möchtest du dieses Kleidungsstück wirklich endgültig löschen?")
            .assertIsDisplayed()

        // Buttons sichtbar
        composeTestRule
            .onNodeWithText("Ja")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Nein")
            .assertIsDisplayed()

        // Confirm Button klicken
        composeTestRule
            .onNodeWithText("Ja")
            .performClick()

        assertTrue(confirmed)

        // Dismiss Button klicken
        composeTestRule
            .onNodeWithText("Nein")
            .performClick()

        assertTrue(dismissed)
    }
}
