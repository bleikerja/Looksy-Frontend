package com.example.looksy

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.looksy.model.*
import org.junit.Rule
import org.junit.Test

val testClothes = Clothes(
    id = 42, type = Type.Tops,
    size = Size._38,
    seasonUsage = Season.inBetween,
    material = Material.Wool,
    clean = true,
    washingNotes = WashingNotes.None,
    imagePath = "",
    isSynced = false
)

class HeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun header_displaysTitleCorrectly() {
        // Setup
        composeTestRule.setContent {
            Header(
                onNavigateBack = {},
                onNavigateToRightIcon = {},
                clothesData = null,
                headerText = "Mein Kleiderschrank",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = true
            )
        }

        // Überprüfung: Text vorhanden?
        composeTestRule.onNodeWithText("Mein Kleiderschrank").assertIsDisplayed()
    }

    @Test
    fun header_hidesBackButton_whenIsFirstHeaderIsTrue() {
        composeTestRule.setContent {
            Header(
                onNavigateBack = {},
                onNavigateToRightIcon = {},
                clothesData = null,
                headerText = "Home",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = true
            )
        }

        // Überprüfung: "Zurück" Icon darf nicht existieren
        composeTestRule.onNodeWithContentDescription("Zurück").assertDoesNotExist()
    }

    @Test
    fun header_callsNavigateBack_whenClicked() {
        var backClicked = false

        composeTestRule.setContent {
            Header(
                onNavigateBack = { backClicked = true },
                onNavigateToRightIcon = {},
                clothesData = null,
                headerText = "Test",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = false
            )
        }

        // Klick ausführen
        composeTestRule.onNodeWithContentDescription("Zurück").performClick()

        // Überprüfung: Wurde die Variable auf true gesetzt?
        assert(backClicked)
    }

    @Test
    fun header_showsRightIcon_andCallsNavigationWithId() {

        var clickedId: Int? = -1

        composeTestRule.setContent {
            Header(
                onNavigateBack = {},
                onNavigateToRightIcon = { id -> clickedId = id },
                clothesData = testClothes,
                headerText = "Details",
                rightIconContentDescription = "Einstellungen",
                rightIcon = Icons.Default.Settings,
                isFirstHeader = false
            )
        }

        // Rechts-Icon klicken
        composeTestRule.onNodeWithContentDescription("Einstellungen").performClick()

        // Überprüfung: Wurde die korrekte ID (42) übergeben?
        assert(clickedId == 42)
    }
    @Test
    fun header_showsRightIcon_whenClothesDataIsNull() {
        var capturedId: Int? = 999 // Startwert ungleich null
        val iconDesc = "Settings"

        composeTestRule.setContent {
            Header(
                onNavigateBack = {},
                onNavigateToRightIcon = { id -> capturedId = id },
                clothesData = null, // Hier ist es null!
                headerText = "Test",
                rightIconContentDescription = iconDesc,
                rightIcon = Icons.Default.Settings,
                isFirstHeader = true
            )
        }

        // 1. Prüfen: Icon ist trotzdem sichtbar
        composeTestRule.onNodeWithContentDescription(iconDesc).assertIsDisplayed()

        // 2. Aktion: Icon klicken
        composeTestRule.onNodeWithContentDescription(iconDesc).performClick()

        // 3. Prüfen: Wurde null an die Funktion übergeben?
        assert(capturedId == null)
    }
}
