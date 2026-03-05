package com.guitarlearning.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarlearning.app.data.Scale
import com.guitarlearning.app.data.ScaleRepository
import com.guitarlearning.app.data.ScaleType
import com.guitarlearning.app.ui.components.FretboardDiagram

@Composable
fun ScalesScreen() {
    var selectedType by remember { mutableStateOf<ScaleType?>(null) }
    var expandedScaleName by remember { mutableStateOf<String?>(null) }

    val scales = ScaleRepository.scales.let { all ->
        if (selectedType != null) all.filter { it.type == selectedType } else all
    }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick  = { selectedType = null; expandedScaleName = null },
                    label    = { Text("All") }
                )
            }
            items(ScaleType.entries) { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick  = { selectedType = type; expandedScaleName = null },
                    label    = { Text(type.label) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(scales, key = { it.name }) { scale ->
                ScaleCard(
                    scale    = scale,
                    expanded = expandedScaleName == scale.name,
                    onToggle = {
                        expandedScaleName = if (expandedScaleName == scale.name) null else scale.name
                    }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ScaleCard(scale: Scale, expanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(if (expanded) 4.dp else 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                scale.rootNote,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            scale.type.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        scale.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()

                    Text(
                        scale.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        "Fretboard (starting fret ${scale.startFret})",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    FretboardDiagram(scale = scale)

                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendItem(
                            color = MaterialTheme.colorScheme.primary,
                            label = "Root note"
                        )
                        LegendItem(
                            color = MaterialTheme.colorScheme.secondary,
                            label = "Scale note"
                        )
                    }

                    // Notes list
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Notes by string",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            val stringLabels = mapOf(6 to "Low E", 5 to "A", 4 to "D", 3 to "G", 2 to "B", 1 to "High e")
                            for (str in 6 downTo 1) {
                                val frets = scale.notes
                                    .filter { it.first == str }
                                    .map { it.second }
                                    .sorted()
                                if (frets.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            stringLabels[str] ?: "String $str",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Frets: ${frets.joinToString(", ")}",
                                            style      = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape    = RoundedCornerShape(6.dp),
            color    = color
        ) {}
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
