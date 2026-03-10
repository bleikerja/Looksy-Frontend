package com.example.looksy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.looksy.data.model.ClothesColor
import com.example.looksy.data.model.WashingNotes

// ─────────────────────────────────────────────────────────
//  Base accordion scaffold
// ─────────────────────────────────────────────────────────

/**
 * A borderless row that expands downward to reveal [content] when tapped.
 * Shows [label] on the left and [displayValue] + animated chevron on the right.
 */
@Composable
fun AccordionRow(
    label: String,
    displayValue: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        label = "chevronRotation",
    )
    val contentAlpha = if (enabled) 1f else 0.38f

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onToggle)
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha * 0.7f),
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                )
            }
        }
        HorizontalDivider()
        AnimatedVisibility(visible = isOpen) {
            Box(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
                content()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  Pill chip
// ─────────────────────────────────────────────────────────

/**
 * A rounded FilterChip pill.
 * [leadingContent] is an optional slot rendered before the label (used for color bubbles).
 */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                leadingContent?.invoke()
                Text(text)
            }
        },
    )
}

// ─────────────────────────────────────────────────────────
//  Single-select accordion
// ─────────────────────────────────────────────────────────

/**
 * Accordion that expands inline to reveal selectable pill chips.
 * Collects one value; closes after selection.
 * [openKey] / [onOpenKeyChange] enforce single-open-at-a-time across siblings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SingleSelectAccordion(
    fieldKey: String,
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    openKey: String?,
    onOpenKeyChange: (String?) -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val isOpen = openKey == fieldKey
    val displayValue = selectedOption?.let { optionLabel(it) } ?: "—"

    Column(modifier = modifier) {
        AccordionRow(
            label = label,
            displayValue = displayValue,
            isOpen = isOpen,
            onToggle = { onOpenKeyChange(if (isOpen) null else fieldKey) },
            enabled = enabled,
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { option ->
                    PillChip(
                        text = optionLabel(option),
                        selected = selectedOption == option,
                        onClick = {
                            onOptionSelected(option)
                            onOpenKeyChange(null)
                        },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  Optional single-select accordion (allows clearing to null)
// ─────────────────────────────────────────────────────────

/**
 * Like [SingleSelectAccordion] but prepends a "—" pill that clears the selection to null.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> OptionalSingleSelectAccordion(
    fieldKey: String,
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    openKey: String?,
    onOpenKeyChange: (String?) -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
) {
    val isOpen = openKey == fieldKey
    val displayValue = selectedOption?.let { optionLabel(it) } ?: "—"

    Column(modifier = modifier) {
        AccordionRow(
            label = label,
            displayValue = displayValue,
            isOpen = isOpen,
            onToggle = { onOpenKeyChange(if (isOpen) null else fieldKey) },
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PillChip(
                    text = "—",
                    selected = selectedOption == null,
                    onClick = {
                        onOptionSelected(null)
                        onOpenKeyChange(null)
                    },
                )
                options.forEach { option ->
                    PillChip(
                        text = optionLabel(option),
                        selected = selectedOption == option,
                        onClick = {
                            onOptionSelected(option)
                            onOpenKeyChange(null)
                        },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  Color selector accordion
// ─────────────────────────────────────────────────────────

/** Small colored circle rendered inside each ClothesColor chip. */
@Composable
private fun ColorBubble(clothesColor: ClothesColor) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(clothesColor.colorValue),
    )
}

/**
 * Accordion variant for [ClothesColor] that renders a colored bubble inside each chip.
 * Includes a leading "—" pill to clear the selection.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSelectAccordion(
    fieldKey: String,
    label: String,
    selectedOption: ClothesColor?,
    onOptionSelected: (ClothesColor?) -> Unit,
    openKey: String?,
    onOpenKeyChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOpen = openKey == fieldKey
    val displayValue = selectedOption?.displayName ?: "—"

    Column(modifier = modifier) {
        AccordionRow(
            label = label,
            displayValue = displayValue,
            isOpen = isOpen,
            onToggle = { onOpenKeyChange(if (isOpen) null else fieldKey) },
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PillChip(
                    text = "—",
                    selected = selectedOption == null,
                    onClick = {
                        onOptionSelected(null)
                        onOpenKeyChange(null)
                    },
                )
                ClothesColor.entries.forEach { color ->
                    PillChip(
                        text = color.displayName,
                        selected = selectedOption == color,
                        onClick = {
                            onOptionSelected(color)
                            onOpenKeyChange(null)
                        },
                        leadingContent = { ColorBubble(color) },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  WashingNotes multi-select accordion
// ─────────────────────────────────────────────────────────

/**
 * Multi-select accordion for [WashingNotes].
 * Conflicting options are automatically disabled based on [WashingNotes.getConflicts].
 * The accordion does not close on selection so the user can pick multiple values.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WashingNotesAccordion(
    fieldKey: String,
    label: String,
    selectedOptions: List<WashingNotes>,
    onOptionSelected: (WashingNotes) -> Unit,
    openKey: String?,
    onOpenKeyChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOpen = openKey == fieldKey
    val summaryText = when {
        selectedOptions.isEmpty() -> "—"
        selectedOptions.size == 1 -> selectedOptions[0].displayName
        else -> "${selectedOptions.size} gewählt"
    }

    Column(modifier = modifier) {
        AccordionRow(
            label = label,
            displayValue = summaryText,
            isOpen = isOpen,
            onToggle = { onOpenKeyChange(if (isOpen) null else fieldKey) },
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                WashingNotes.entries.forEach { option ->
                    val conflicts = WashingNotes.getConflicts(option)
                    val isDisabled =
                        selectedOptions.any { it in conflicts } && !selectedOptions.contains(option)
                    PillChip(
                        text = option.displayName,
                        selected = selectedOptions.contains(option),
                        enabled = !isDisabled,
                        onClick = { onOptionSelected(option) },
                        leadingContent = if (option.iconRes != null) {
                            {
                                Image(
                                    painter = painterResource(id = option.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}
