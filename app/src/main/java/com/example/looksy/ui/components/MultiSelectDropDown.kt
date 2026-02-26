package com.example.looksy.ui.components

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.looksy.R
import com.example.looksy.data.model.WashingNotes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    options: List<WashingNotes>,
    label: String,
    selectedOptions: List<WashingNotes>,
    onOptionSelected: (WashingNotes) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedOptions.joinToString(",\n") { it.displayName },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            singleLine = false
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
        ) {
            options.forEach { option ->
                val conflicts = WashingNotes.getConflicts(option)
                val isDisabled =
                    selectedOptions.any { it in conflicts } && !selectedOptions.contains(option)

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedOptions.contains(option),
                                onCheckedChange = null,
                                enabled = !isDisabled
                            )
                            option.iconRes?.let { iconRes ->
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = option.displayName,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp),
                                    tint = if (isDisabled) Color.Gray else Color.Unspecified
                                )
                            }
                            Text(
                                text = " " + option.displayName,
                                color = if (isDisabled) Color.Gray else Color.Unspecified
                            )
                        }
                    },
                    onClick = {
                        if (!isDisabled) {
                            onOptionSelected(option)
                        }
                    },
                    enabled = !isDisabled,
                    modifier = Modifier.height(60.dp)
                )
            }
        }
    }
}
