package com.guitarlearning.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.guitarlearning.app.data.Scale

/**
 * Draws a horizontal fretboard diagram showing the notes in a scale.
 *
 * Strings run left-to-right (low E at top), frets increase to the right.
 * Root notes are highlighted in primary colour; other notes in secondary.
 */
@Composable
fun FretboardDiagram(
    scale: Scale,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    onSurface: Color = MaterialTheme.colorScheme.onSurface
) {
    val fretsToShow = 5

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val w = size.width
        val h = size.height
        val numStrings = 6

        val leftMargin  = w * 0.08f
        val rightMargin = w * 0.04f
        val topMargin   = h * 0.12f
        val bottomMargin= h * 0.08f

        val gridW = w - leftMargin - rightMargin
        val gridH = h - topMargin - bottomMargin

        val fretSpacing   = gridW / fretsToShow
        val stringSpacing = gridH / (numStrings - 1)
        val dotRadius     = minOf(fretSpacing, stringSpacing) * 0.28f

        // ── Fret lines (vertical) ─────────────────────────────────────────
        for (f in 0..fretsToShow) {
            val x = leftMargin + f * fretSpacing
            drawLine(
                color = onSurface.copy(alpha = 0.25f),
                start = Offset(x, topMargin),
                end   = Offset(x, topMargin + gridH),
                strokeWidth = if (f == 0) 4f else 1.5f
            )
            // Fret number label
            if (f > 0) {
                val fretNum = scale.startFret + f - 1
                drawContext.canvas.nativeCanvas.drawText(
                    fretNum.toString(),
                    x - fretSpacing / 2,
                    topMargin - 8f,
                    android.graphics.Paint().apply {
                        color = onSurface.copy(alpha = 0.6f).toArgb()
                        textSize = dotRadius * 1.6f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // ── String lines (horizontal) ─────────────────────────────────────
        // String 6 (low E) at top, string 1 (high e) at bottom
        for (s in 0 until numStrings) {
            val y = topMargin + s * stringSpacing
            val stringNum = 6 - s  // 6..1

            // String label (E A D G B e)
            val label = listOf("E", "A", "D", "G", "B", "e")[s]
            drawContext.canvas.nativeCanvas.drawText(
                label,
                leftMargin - 16f,
                y + dotRadius * 0.4f,
                android.graphics.Paint().apply {
                    color = onSurface.copy(alpha = 0.7f).toArgb()
                    textSize = dotRadius * 1.5f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            drawLine(
                color = onSurface.copy(alpha = 0.4f),
                start = Offset(leftMargin, y),
                end   = Offset(leftMargin + gridW, y),
                strokeWidth = (s * 0.4f + 0.8f)
            )

            // ── Dots ──────────────────────────────────────────────────────
            scale.notes
                .filter { (noteStr, _) -> noteStr == stringNum }
                .forEach { (_, fret) ->
                    val relFret = fret - scale.startFret + 1
                    if (relFret in 1..fretsToShow) {
                        val cx = leftMargin + (relFret - 0.5f) * fretSpacing
                        val isRoot = scale.rootFrets.contains(stringNum to fret)

                        if (isRoot) {
                            drawCircle(primaryColor, dotRadius, Offset(cx, y))
                            drawCircle(Color.White, dotRadius * 0.45f, Offset(cx, y))
                        } else {
                            drawCircle(secondaryColor.copy(alpha = 0.8f), dotRadius, Offset(cx, y))
                        }
                    }
                }
        }
    }
}
