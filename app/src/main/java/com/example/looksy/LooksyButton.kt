package com.example.looksy

import androidx.compose.foundation.Image
import androidx.compose.material3.FilledIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LooksyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    picture: Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
    ) { picture }
}

@Preview
@Composable
fun PreviewLooksyButton() {
    LooksyButton(
        onClick = {},
        modifier = Modifier,
        picture =
            Image(
                painter = painterResource(id = R.drawable.unbenanntes),
                contentDescription = ""
            )
    )
}