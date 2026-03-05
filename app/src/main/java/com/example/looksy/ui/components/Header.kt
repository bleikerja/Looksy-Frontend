package com.example.looksy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.looksy.data.model.Clothes

@Composable
fun Header(
    onNavigateBack: () -> Unit,
    onNavigateToRightIcon: (Int?) -> Unit,
    clothesData: Clothes?,
    headerText: String,
    modifier: Modifier = Modifier,
    headerTextStart: Boolean = false,
    rightIconContentDescription: String? = null,
    rightIcon: ImageVector? = null,
    rightIconSize: Float = 1F,
    isFirstHeader: Boolean = false,
    leftIcon: (@Composable (modifier: Modifier) -> Unit)? = null,
    // ── Second icon slot (shown to the left of rightIcon) ──────────────
    // Intended for the demo-mode toggle on the Home screen.
    secondRightIcon: ImageVector? = null,
    secondRightIconContentDescription: String? = null,
    onSecondRightIconClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (leftIcon != null){
            leftIcon(modifier.align(Alignment.CenterStart))
        } else if (!isFirstHeader) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Zurück",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = headerText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = if(headerTextStart) Modifier.align(Alignment.CenterStart) else Modifier.align(Alignment.Center)
        )

        // Right-side icon(s): demo toggle + optional primary action icon
        val hasRightIcon = rightIcon != null && rightIconContentDescription != null
        val hasSecondIcon = secondRightIcon != null && secondRightIconContentDescription != null
        if (hasSecondIcon || hasRightIcon) {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                if (hasSecondIcon) {
                    IconButton(onClick = onSecondRightIconClick) {
                        Icon(
                            imageVector = secondRightIcon!!,
                            contentDescription = secondRightIconContentDescription
                        )
                    }
                }
                if (hasRightIcon) {
                    IconButton(onClick = { onNavigateToRightIcon(clothesData?.id) }) {
                        Icon(
                            imageVector = rightIcon!!,
                            contentDescription = rightIconContentDescription,
                            modifier = Modifier.fillMaxSize(rightIconSize)
                        )
                    }
                }
            }
        }
    }
}