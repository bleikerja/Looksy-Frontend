package com.example.looksy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LooksyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    picture: @Composable () ->Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        picture()
    }
}

@Preview
@Composable
fun PreviewLooksyButton() {
        LooksyButton(
            onClick = { },
            modifier = Modifier,
            {
                Icon(Icons.Default.Add, contentDescription = "")
            }
        )
}