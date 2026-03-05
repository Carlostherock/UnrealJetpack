package com.guitarlearning.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Guitar-inspired warm colour palette
val GuitarBrown   = Color(0xFF5D4037)
val GuitarAmber   = Color(0xFFFF8F00)
val GuitarCream   = Color(0xFFFFF8E1)
val GuitarGreen   = Color(0xFF2E7D32)
val StringGold    = Color(0xFFFFD54F)
val FretboardDark = Color(0xFF1A1209)
val NeckWood      = Color(0xFF6D4C41)

private val DarkColors = darkColorScheme(
    primary          = StringGold,
    onPrimary        = FretboardDark,
    primaryContainer = GuitarBrown,
    onPrimaryContainer = GuitarCream,
    secondary        = GuitarAmber,
    onSecondary      = FretboardDark,
    background       = Color(0xFF1C1B1F),
    onBackground     = Color(0xFFE6E1E5),
    surface          = Color(0xFF2A2530),
    onSurface        = Color(0xFFE6E1E5),
    surfaceVariant   = Color(0xFF3B3540),
    onSurfaceVariant = Color(0xFFCDC7D1),
    tertiary         = GuitarGreen,
    onTertiary       = Color.White,
    error            = Color(0xFFCF6679),
)

private val LightColors = lightColorScheme(
    primary          = GuitarBrown,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFFFDCC4),
    onPrimaryContainer = Color(0xFF2C1500),
    secondary        = GuitarAmber,
    onSecondary      = Color.White,
    background       = GuitarCream,
    onBackground     = Color(0xFF1C1B1F),
    surface          = Color(0xFFFFFBFE),
    onSurface        = Color(0xFF1C1B1F),
    surfaceVariant   = Color(0xFFF4DDD0),
    onSurfaceVariant = Color(0xFF52443C),
    tertiary         = GuitarGreen,
    onTertiary       = Color.White,
    error            = Color(0xFFB3261E),
)

@Composable
fun GuitarLearningTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
