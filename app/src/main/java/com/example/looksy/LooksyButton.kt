package com.example.looksy

import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
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
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = TODO(),
        colors = TODO(),
        interactionSource = TODO()
    ) { picture }
}

@Preview
@Composable
fun PreviewLooksyButton() {
    LooksyButton()
}