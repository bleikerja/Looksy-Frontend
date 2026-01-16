package com.example.looksy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.looksy.model.Clothes

@Composable
fun Header(
    onNavigateBack: () -> Unit,
    onNavigateToRightIcon: (Int?) -> Unit,
    clothesData: Clothes?,
    headerText: String,
    rightIconContentDescription: String?,
    rightIcon: ImageVector?,
    isFirstHeader: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isFirstHeader) {
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
            modifier = Modifier.align(Alignment.Center)
        )

        if (rightIcon != null && rightIconContentDescription != null) {
            IconButton(
                onClick = { onNavigateToRightIcon(clothesData?.id) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = rightIconContentDescription,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}