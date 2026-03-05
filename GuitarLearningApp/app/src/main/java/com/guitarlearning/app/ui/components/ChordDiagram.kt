package com.guitarlearning.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guitarlearning.app.data.Chord

/**
 * Renders a standard 6-string guitar chord diagram using Canvas.
 *
 * @param chord   The chord to render.
 * @param size    Width (and approximate height) of the diagram.
 * @param primaryColor  Dot fill colour (defaults to MaterialTheme primary).
 */
@Composable
fun ChordDiagram(
    chord: Chord,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onSurface: Color = MaterialTheme.colorScheme.onSurface,
    surfaceVariant: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Canvas(modifier = modifier.size(size, size * 1.25f)) {
        val w = this.size.width
        val h = this.size.height

        val numStrings = 6
        val numFrets   = 5

        // Layout margins
        val topMargin    = h * 0.18f   // room for X / O symbols
        val bottomMargin = h * 0.05f
        val leftMargin   = w * 0.12f
        val rightMargin  = w * 0.04f

        val gridW = w - leftMargin - rightMargin
        val gridH = h - topMargin - bottomMargin

        val stringSpacing = gridW / (numStrings - 1)
        val fretSpacing   = gridH / numFrets

        val dotRadius = stringSpacing * 0.32f
        val nutThickness = if (chord.startFret == 1) fretSpacing * 0.22f else 0f

        // ── Draw nut / position marker ─────────────────────────────────────
        if (chord.startFret == 1) {
            // Thick nut bar at the top
            drawRect(
                color = onSurface,
                topLeft = Offset(leftMargin, topMargin),
                size = Size(gridW, nutThickness)
            )
        } else {
            // Position text (fret number)
            drawContext.canvas.nativeCanvas.drawText(
                "${chord.startFret}fr",
                leftMargin - dotRadius * 2.5f,
                topMargin + fretSpacing * 0.7f,
                android.graphics.Paint().apply {
                    color = onSurface.toArgb()
                    textSize = dotRadius * 1.8f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // ── Draw fret lines ────────────────────────────────────────────────
        for (f in 0..numFrets) {
            val y = topMargin + nutThickness + f * fretSpacing
            drawLine(
                color = onSurface.copy(alpha = 0.3f),
                start = Offset(leftMargin, y),
                end   = Offset(leftMargin + gridW, y),
                strokeWidth = if (f == 0 && chord.startFret != 1) 1.5f else 1f
            )
        }

        // ── Draw string lines ──────────────────────────────────────────────
        for (s in 0 until numStrings) {
            val x = leftMargin + s * stringSpacing
            drawLine(
                color = onSurface.copy(alpha = 0.5f),
                start = Offset(x, topMargin + nutThickness),
                end   = Offset(x, topMargin + nutThickness + gridH),
                strokeWidth = (s * 0.4f + 0.8f)   // strings get thicker toward low E
            )
        }

        // ── Draw barre ────────────────────────────────────────────────────
        chord.barre?.let { barre ->
            val barreRelFret = barre.fret - chord.startFret
            val cy = topMargin + nutThickness + barreRelFret * fretSpacing - fretSpacing / 2
            val xFrom = leftMargin + (numStrings - barre.toString) * stringSpacing
            val xTo   = leftMargin + (numStrings - barre.fromString) * stringSpacing
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(xFrom - dotRadius, cy - dotRadius),
                size = Size(xTo - xFrom + dotRadius * 2, dotRadius * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(dotRadius)
            )
        }

        // ── Draw finger dots ───────────────────────────────────────────────
        // chord.frets: index 0 = string 6 (low E), index 5 = string 1 (high e)
        chord.frets.forEachIndexed { idx, fret ->
            // idx 0 → drawn on left side of diagram (low E = left when viewed from player)
            val stringPos = idx   // 0=string6 on the left of diagram
            val x = leftMargin + stringPos * stringSpacing

            when {
                fret == -1 -> {
                    // Muted — draw X above grid
                    drawMuteSymbol(x, topMargin - dotRadius * 1.4f, dotRadius * 0.7f, onSurface)
                }
                fret == 0 -> {
                    // Open — draw O above grid
                    drawOpenSymbol(x, topMargin - dotRadius * 1.4f, dotRadius * 0.7f, onSurface)
                }
                else -> {
                    // Check if this note is covered by a barre (skip separate dot)
                    val barreCoversThis = chord.barre?.let { b ->
                        fret == b.fret &&
                            (numStrings - idx) >= b.fromString &&
                            (numStrings - idx) <= b.toString
                    } ?: false

                    if (!barreCoversThis) {
                        val relFret = fret - chord.startFret + 1
                        val cy = topMargin + nutThickness + (relFret - 0.5f) * fretSpacing
                        drawCircle(primaryColor, dotRadius, Offset(x, cy))

                        // Finger number inside dot
                        val finger = chord.fingers.getOrElse(idx) { 0 }
                        if (finger > 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                finger.toString(),
                                x,
                                cy + dotRadius * 0.38f,
                                android.graphics.Paint().apply {
                                    color = Color.White.toArgb()
                                    textSize = dotRadius * 1.3f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMuteSymbol(cx: Float, cy: Float, r: Float, color: Color) {
    val s = r * 1.4f
    drawLine(color, Offset(cx - s, cy - s), Offset(cx + s, cy + s), strokeWidth = r * 0.7f)
    drawLine(color, Offset(cx + s, cy - s), Offset(cx - s, cy + s), strokeWidth = r * 0.7f)
}

private fun DrawScope.drawOpenSymbol(cx: Float, cy: Float, r: Float, color: Color) {
    drawCircle(Color.Transparent, r, Offset(cx, cy))
    drawCircle(color, r, Offset(cx, cy), style = Stroke(width = r * 0.55f))
}
