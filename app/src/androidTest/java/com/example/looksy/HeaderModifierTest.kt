package com.example.looksy

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.looksy.ui.components.Header
import com.example.looksy.ui.theme.LooksyTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for Header component modifier parameter.
 * Tests that the new modifier parameter works correctly.
 */
class HeaderModifierTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun header_acceptsCustomModifier() {
        // Given: Header with custom modifier
        composeTestRule.setContent {
            LooksyTheme {
                Header(
                    onNavigateBack = {},
                    onNavigateToRightIcon = {},
                    clothesData = null,
                    headerText = "Test Header",
                    rightIconContentDescription = null,
                    rightIcon = null,
                    isFirstHeader = true,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // Then: Header is displayed with modifier applied
        composeTestRule.onNodeWithText("Test Header").assertIsDisplayed()
    }

    @Test
    fun header_worksWithoutModifier_usesDefault() {
        // Given: Header without explicit modifier (uses default)
        composeTestRule.setContent {
            LooksyTheme {
                Header(
                    onNavigateBack = {},
                    onNavigateToRightIcon = {},
                    clothesData = null,
                    headerText = "Default Modifier",
                    rightIconContentDescription = null,
                    rightIcon = null,
                    isFirstHeader = true
                )
            }
        }

        // Then: Header is displayed correctly
        composeTestRule.onNodeWithText("Default Modifier").assertIsDisplayed()
    }

    @Test
    fun header_modifierParameter_doesNotBreakExistingBehavior() {
        // Given: Header with modifier and back button
        var backClicked = false

        composeTestRule.setContent {
            LooksyTheme {
                Header(
                    onNavigateBack = { backClicked = true },
                    onNavigateToRightIcon = {},
                    clothesData = null,
                    headerText = "Test Header",
                    rightIconContentDescription = null,
                    rightIcon = null,
                    isFirstHeader = false,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // When: Back button is clicked
        composeTestRule.onNodeWithContentDescription("Zurück").performClick()

        // Then: Navigation callback still works
        assert(backClicked)
    }
}
