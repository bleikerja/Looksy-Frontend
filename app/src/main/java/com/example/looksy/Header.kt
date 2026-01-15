package com.example.looksy

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
fun Header(onNavigateBack: () -> Unit,
           onNavigateToRightIcon: (Int) -> Unit,
           clothesData: Clothes,
           headerText: String,
           rightIconContentDescription: String,
           rightIcon: ImageVector
           ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Zurück",
                modifier = Modifier.padding(end = 10.dp)
            )
        }
        Text(
            headerText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onNavigateToRightIcon(clothesData.id) }) {
            Icon(
                imageVector = rightIcon,
                contentDescription = rightIconContentDescription,
                modifier = Modifier.padding(end = 10.dp)
            )
        }
    }
}