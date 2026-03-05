package com.guitarlearning.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

// ── Standard guitar open-string tuning ────────────────────────────────────────
private val STANDARD_TUNING = listOf(
    "E2" to 82.41f,
    "A2" to 110.00f,
    "D3" to 146.83f,
    "G3" to 196.00f,
    "B3" to 246.94f,
    "E4" to 329.63f
)

// All chromatic notes used for frequency→note detection
private val NOTE_FREQUENCIES = buildMap<String, Float> {
    val names = listOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")
    for (octave in 0..8) {
        names.forEachIndexed { i, name ->
            val n = (octave - 4) * 12 + i - 9   // semitones from A4
            val freq = 440f * 2f.pow(n / 12f)
            put("$name$octave", freq)
        }
    }
}

private data class NoteMatch(val name: String, val freq: Float, val cents: Float)

private fun detectNote(frequency: Float): NoteMatch? {
    if (frequency <= 0f) return null
    var best: Pair<String, Float>? = null
    var bestDist = Float.MAX_VALUE
    NOTE_FREQUENCIES.forEach { (name, freq) ->
        val dist = abs(frequency - freq)
        if (dist < bestDist) { bestDist = dist; best = name to freq }
    }
    val (name, target) = best ?: return null
    val cents = 1200f * log2(frequency / target)
    return NoteMatch(name, target, cents)
}

/** Simplified pitch detector using autocorrelation on PCM audio data. */
private fun detectPitch(buffer: ShortArray, sampleRate: Int): Float {
    val size = minOf(buffer.size, 4096)
    val pcm = FloatArray(size) { buffer[it] / 32768f }

    // Autocorrelation
    val minPeriod = (sampleRate / 1200f).toInt()   // max 1200 Hz
    val maxPeriod = (sampleRate / 40f).toInt()      // min 40 Hz

    var bestPeriod = -1
    var bestCorr = 0f

    for (period in minPeriod..minOf(maxPeriod, size / 2)) {
        var corr = 0f
        for (i in 0 until size - period) {
            corr += pcm[i] * pcm[i + period]
        }
        corr /= (size - period)
        if (corr > bestCorr) { bestCorr = corr; bestPeriod = period }
    }

    return if (bestPeriod > 0 && bestCorr > 0.01f) sampleRate.toFloat() / bestPeriod else 0f
}

@Composable
fun TunerScreen() {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var isListening    by remember { mutableStateOf(false) }
    var detectedNote   by remember { mutableStateOf<NoteMatch?>(null) }
    var selectedString by remember { mutableStateOf<Int?>(null) }   // index into STANDARD_TUNING

    // Needle animation
    val needleAngle by animateFloatAsState(
        targetValue = detectedNote?.cents?.coerceIn(-50f, 50f) ?: 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "needle"
    )

    // In-tune tolerance (cents)
    val inTuneCents = 5f
    val inTune = detectedNote != null && abs(needleAngle) <= inTuneCents

    // Audio recording coroutine
    LaunchedEffect(isListening) {
        if (!isListening) return@LaunchedEffect
        launch(Dispatchers.IO) {
            val sampleRate = 44100
            val bufferSize = android.media.AudioRecord.getMinBufferSize(
                sampleRate,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(8192)

            val recorder = android.media.AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                sampleRate,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            recorder.startRecording()
            val buffer = ShortArray(bufferSize)
            try {
                while (isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val freq = detectPitch(buffer.copyOf(read), sampleRate)
                        detectedNote = detectNote(freq)
                    }
                    delay(80)
                }
            } finally {
                recorder.stop()
                recorder.release()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasPermission) {
            PermissionPrompt(onRequest = { permLauncher.launch(Manifest.permission.RECORD_AUDIO) })
            return@Column
        }

        // ── Tuner dial ────────────────────────────────────────────────────
        TunerDial(
            needleAngle = needleAngle,
            inTune      = inTune,
            modifier    = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        // ── Detected note display ─────────────────────────────────────────
        NoteDisplay(
            note   = detectedNote,
            inTune = inTune
        )

        // ── Listen toggle ─────────────────────────────────────────────────
        Button(
            onClick  = { isListening = !isListening },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isListening) "Stop Tuner" else "Start Tuner",
                style = MaterialTheme.typography.titleMedium
            )
        }

        HorizontalDivider()

        // ── Standard tuning reference ─────────────────────────────────────
        Text(
            "Standard Tuning Reference",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            STANDARD_TUNING.forEachIndexed { idx, (name, freq) ->
                StringButton(
                    name     = name,
                    freq     = freq,
                    selected = selectedString == idx,
                    onClick  = { selectedString = if (selectedString == idx) null else idx }
                )
            }
        }

        if (selectedString != null) {
            val (name, freq) = STANDARD_TUNING[selectedString!!]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {
                    Text(
                        "String ${selectedString!! + 1} — $name",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${"%.2f".format(freq)} Hz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        when (selectedString!!) {
                            0 -> "Thickest string — press and listen"
                            5 -> "Thinnest string — listen carefully"
                            else -> "Match your string to this pitch"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TunerDial(needleAngle: Float, inTune: Boolean, modifier: Modifier = Modifier) {
    val primaryColor      = MaterialTheme.colorScheme.primary
    val errorColor        = MaterialTheme.colorScheme.error
    val onSurface         = MaterialTheme.colorScheme.onSurface
    val surfaceVariant    = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val w  = size.width
        val h  = size.height
        val cx = w / 2
        val cy = h * 0.85f
        val r  = minOf(w, h) * 0.78f

        // Arc background
        drawArc(
            color      = surfaceVariant,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter  = false,
            topLeft    = Offset(cx - r, cy - r),
            size       = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style      = Stroke(width = 18f, cap = StrokeCap.Round)
        )

        // In-tune zone (green arc in the centre)
        val tuneBand = 180f * (inTuneCents / 50f)
        val inTuneColor = Color(0xFF4CAF50)
        drawArc(
            color      = inTuneColor.copy(alpha = 0.3f),
            startAngle = 270f - tuneBand,
            sweepAngle = tuneBand * 2,
            useCenter  = false,
            topLeft    = Offset(cx - r, cy - r),
            size       = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style      = Stroke(width = 18f, cap = StrokeCap.Round)
        )

        // Tick marks every 10 cents
        for (i in -5..5) {
            val angleDeg = 270f + i * (180f / 10f)
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            val outerR = r + 10f
            val innerR = if (i == 0) r - 30f else r - 18f
            drawLine(
                color       = onSurface.copy(alpha = if (i == 0) 0.6f else 0.25f),
                start       = Offset(cx + cos(angleRad) * innerR, cy + sin(angleRad) * innerR),
                end         = Offset(cx + cos(angleRad) * outerR, cy + sin(angleRad) * outerR),
                strokeWidth = if (i == 0) 4f else 2f
            )
        }

        // Needle
        val needleNorm  = needleAngle / 50f          // -1..1
        val needleDeg   = 270f + needleNorm * 90f    // 180°..360°
        val needleRad   = Math.toRadians(needleDeg.toDouble()).toFloat()
        val needleLen   = r * 0.75f
        val needleColor = when {
            inTune            -> inTuneColor
            abs(needleNorm) > 0.5f -> errorColor
            else              -> primaryColor
        }
        drawLine(
            color       = needleColor,
            start       = Offset(cx, cy),
            end         = Offset(cx + cos(needleRad) * needleLen, cy + sin(needleRad) * needleLen),
            strokeWidth = 6f,
            cap         = StrokeCap.Round
        )
        // Pivot circle
        drawCircle(needleColor, 10f, Offset(cx, cy))

        // Labels: flat / in tune / sharp
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize  = 28f
        }
        paint.color = onSurface.copy(alpha = 0.5f).toArgb()
        drawContext.canvas.nativeCanvas.drawText("♭ Flat",  cx - r * 0.7f, cy - r * 0.25f, paint)
        drawContext.canvas.nativeCanvas.drawText("Sharp ♯", cx + r * 0.7f, cy - r * 0.25f, paint)
    }
}

@Composable
private fun NoteDisplay(note: NoteMatch?, inTune: Boolean) {
    val inTuneColor = Color(0xFF4CAF50)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (inTune) inTuneColor.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (note != null) {
                Text(
                    note.name,
                    fontSize   = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (inTune) inTuneColor else MaterialTheme.colorScheme.primary
                )
                Text(
                    "${"%.1f".format(note.freq)} Hz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    when {
                        inTune               -> "In tune!"
                        note.cents > 0       -> "+${"%.0f".format(note.cents)}¢ sharp — tune down"
                        else                 -> "${"%.0f".format(note.cents)}¢ flat — tune up"
                    },
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color      = if (inTune) inTuneColor else MaterialTheme.colorScheme.onSurface
                )
            } else {
                Icon(
                    Icons.Default.MicOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Play a note...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StringButton(name: String, freq: Float, selected: Boolean, onClick: () -> Unit) {
    val (noteName, octave) = name.partition { it.isLetter() || it == '#' }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape    = CircleShape,
            color    = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            onClick  = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    noteName,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = if (selected) MaterialTheme.colorScheme.onPrimary
                                 else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            octave,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Inline value for use in TunerDial
private const val inTuneCents = 5f

@Composable
private fun PermissionPrompt(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint     = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Microphone access needed",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "The tuner listens through your microphone to detect the pitch of your guitar strings. No audio is stored.",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
            Text("Grant Permission")
        }
    }
}
