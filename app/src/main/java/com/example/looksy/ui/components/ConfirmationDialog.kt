package com.example.looksy.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ConfirmationDialog (
    title: String,
    text: String,
    dismissText: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    isDeletion: Boolean = false
    ) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button( onClick = onConfirm, colors = ButtonDefaults.buttonColors(if (isDeletion) Color.Red else Color.Unspecified)) { Text(confirmText) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(dismissText) }
        }
    )
}