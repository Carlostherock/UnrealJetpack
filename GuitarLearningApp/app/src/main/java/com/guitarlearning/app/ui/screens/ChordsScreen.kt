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
import com.guitarlearning.app.data.Chord
import com.guitarlearning.app.data.ChordCategory
import com.guitarlearning.app.data.ChordRepository
import com.guitarlearning.app.ui.components.ChordDiagram

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordsScreen() {
    var selectedCategory by remember { mutableStateOf(ChordCategory.OPEN) }
    var expandedChordName by remember { mutableStateOf<String?>(null) }

    val chords = ChordRepository.byCategory(selectedCategory)

    Column(Modifier.fillMaxSize()) {
        // Category filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ChordRepository.categories) { cat ->
                FilterChip(
                    selected  = cat == selectedCategory,
                    onClick   = {
                        selectedCategory  = cat
                        expandedChordName = null
                    },
                    label     = { Text(cat.label) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chords, key = { it.name }) { chord ->
                ChordCard(
                    chord     = chord,
                    expanded  = expandedChordName == chord.name,
                    onToggle  = {
                        expandedChordName = if (expandedChordName == chord.name) null else chord.name
                    }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ChordCard(
    chord: Chord,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(if (expanded) 4.dp else 1.dp)
    ) {
        Column {
            // Header row — always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        chord.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        chord.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider()

                    // Diagram + string labels side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ChordDiagram(
                                chord = chord,
                                size  = 160.dp
                            )
                            Spacer(Modifier.height(4.dp))
                            // String labels below diagram
                            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                listOf("E","A","D","G","B","e").forEach { s ->
                                    Text(
                                        s,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(27.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Fingering text
                    StringFingeringRow(chord)

                    // Tip
                    if (chord.tip.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(Modifier.padding(12.dp)) {
                                Text(
                                    "\uD83D\uDCA1 Tip: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    chord.tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StringFingeringRow(chord: Chord) {
    val stringNames = listOf("E","A","D","G","B","e")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Fingering", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chord.frets.forEachIndexed { idx, fret ->
                val finger = chord.fingers.getOrElse(idx) { 0 }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringNames[idx],
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        when (fret) { -1 -> "✕"; 0 -> "○"; else -> fret.toString() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (finger) { 0 -> "–"; else -> finger.toString() },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
