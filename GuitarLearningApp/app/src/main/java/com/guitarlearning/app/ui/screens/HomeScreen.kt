package com.guitarlearning.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarlearning.app.data.LessonLevel
import com.guitarlearning.app.data.LessonRepository

@Composable
fun HomeScreen(
    completedLessonIds: Set<Int>,
    onNavigateToLessons: () -> Unit,
    onNavigateToChords: () -> Unit,
    onNavigateToScales: () -> Unit,
    onNavigateToTuner: () -> Unit
) {
    val total     = LessonRepository.lessons.size
    val completed = completedLessonIds.size
    val progress  = if (total > 0) completed.toFloat() / total else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Hero header ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Guitar Learning",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your personal guitar teacher",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(16.dp))
                // Progress bar
                Text(
                    "$completed / $total lessons completed",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.35f)
                )
            }
        }

        // ── Quick tip card ─────────────────────────────────────────────────
        DailyTipCard()

        // ── Main navigation grid ───────────────────────────────────────────
        Text(
            "Explore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NavCard(
                label = "Lessons",
                icon = Icons.Default.MenuBook,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = onNavigateToLessons,
                modifier = Modifier.weight(1f)
            )
            NavCard(
                label = "Chords",
                icon = Icons.Default.LibraryMusic,
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onNavigateToChords,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NavCard(
                label = "Scales",
                icon = Icons.Default.Piano,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = onNavigateToScales,
                modifier = Modifier.weight(1f)
            )
            NavCard(
                label = "Tuner",
                icon = Icons.Default.GraphicEq,
                color = MaterialTheme.colorScheme.errorContainer,
                onClick = onNavigateToTuner,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Level summary ──────────────────────────────────────────────────
        Text(
            "Lessons by level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LessonLevel.entries.forEach { level ->
            val levelLessons  = LessonRepository.lessons.filter { it.level == level }
            val levelComplete = levelLessons.count { it.id in completedLessonIds }
            LevelProgressRow(
                level = level.label,
                color = Color(level.color),
                completed = levelComplete,
                total = levelLessons.size
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DailyTipCard() {
    val tips = listOf(
        "Practice for 15 minutes every day — consistency beats long weekend sessions.",
        "Always warm up your fingers before playing to avoid injury.",
        "Use a metronome. Start slow. Speed comes with accuracy, not the other way around.",
        "Record yourself playing. You'll hear things you don't notice while playing.",
        "Learn songs you love — motivation is the best teacher.",
        "Chord transitions are more important than knowing lots of chords."
    )
    val tipIndex = (System.currentTimeMillis() / 86_400_000L % tips.size).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Tip of the day",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    tips[tipIndex],
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun NavCard(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LevelProgressRow(
    label: String,
    color: Color,
    completed: Int,
    total: Int
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Text(
                    "$completed/$total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color
            )
        }
    }
}
