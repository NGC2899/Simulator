package com.example.matharium.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.matharium.R
import com.example.matharium.app.*

/**
 * UI Layer for Voice Processing.
 * Responsible only for rendering and user input.
 */
@Composable
fun VoiceProcessing() {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val state = rememberVoiceState()

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> permissionGranted = isGranted }

    // Sync processing when nTerms change
    LaunchedEffect(state.nTerms) {
        state.processAudio()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {
        LabeledSlider(
            label = "Fourier Terms",
            valueDisplay = state.nTerms.toInt().toString(),
            value = state.nTerms,
            range = 1f..1000f,
            colors = colors,
            onValueChange = { state.nTerms = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
        ) {
            // Record Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight)
                    .clip(RoundedCornerShape(AppDesign.radiusMedium))
                    .background(colors.cardSurface.copy(AppDesign.opacityLow))
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            listOf(colors.accentCyan, colors.accentViolet)
                        ),
                        RoundedCornerShape(AppDesign.radiusMedium)
                    )
                    .clickable(enabled = !state.isRecording) {
                        if (!permissionGranted) {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            state.startRecording()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.mic_outline),
                        null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(AppDesign.iconSmall)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.isRecording) "Recording..." else "Record",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Play Button
            Button(
                onClick = { state.playProcessed() },
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight),
                enabled = state.processedAudio != null && !state.isPlaying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentCyan,
                    disabledContainerColor = colors.accentCyan.copy(alpha = 0.3f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(AppDesign.radiusMedium)
            ) {
                Icon(
                    if (state.isPlaying) painterResource(id = R.drawable.pause_outline) else painterResource(id = R.drawable.caret_forward_outline),
                    null,
                    tint = colors.textOnAccent,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (state.isPlaying) "Playing..." else "Play", color = colors.textOnAccent)
            }

            // Clear Button
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .height(AppDesign.buttonHeight)
                    .clip(RoundedCornerShape(AppDesign.radiusMedium))
                    .background(colors.accentHell.copy(alpha = if (state.rawAudio != null) 0.15f else 0.05f))
                    .border(
                        2.dp,
                        if (state.rawAudio != null) colors.accentHell.copy(0.6f) else colors.accentHell.copy(0.3f),
                        RoundedCornerShape(AppDesign.radiusMedium)
                    )
                    .clickable(enabled = state.rawAudio != null) {
                        state.clear()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(id = R.drawable.trash_outline),
                    null,
                    tint = if (state.rawAudio != null) colors.accentHell else colors.accentHell.copy(0.3f),
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
            }
        }

        SpectrumVisualizer(
            magnitudes = state.magnitudes ?: FloatArray(128),
            nTerms = state.nTerms.toInt(),
            colors = colors
        )

        GlassCard(colors = colors) {
            Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
                Text(
                    "STFT Voice Processing",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.spacingSmall))
                Text(
                    "This mode processes your voice using Short-Time Fourier Transform (STFT) with 42ms windows, balancing time and frequency resolution.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(AppDesign.spacingSmall))
                Text(
                    "How it works",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.radiusSmall))
                BulletPoint("Signal is sliced into 42ms windows (2048 samples).")
                BulletPoint("FFT converts each slice from time to frequency domain.")
                BulletPoint("Inverse FFT reconstructs the signal back to time domain.")
                BulletPoint("Overlap-Add (OLA) ensures smooth transitions between slices.")
            }
        }
    }
}

@Composable
private fun SpectrumVisualizer(
    magnitudes: FloatArray,
    nTerms: Int,
    colors: AppColors
) {
    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
            Text(
                "Frequency Spectrum (STFT Window)",
                fontSize = AppDesign.textHeadline,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(AppDesign.spacingSmall))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(colors.cardSurface.copy(alpha = 0.1f), RoundedCornerShape(AppDesign.radiusSmall))
                    .padding(AppDesign.spacingSmall)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barCount = magnitudes.size
                    val canvasWidth = this.size.width
                    val canvasHeight = this.size.height
                    val barWidth = canvasWidth / barCount
                    val maxMag = magnitudes.maxOrNull()?.coerceAtLeast(1f) ?: 1f

                    for (i in magnitudes.indices) {
                        val mag = magnitudes[i]
                        val x = i * barWidth
                        val normalizedHeight = (mag / maxMag) * canvasHeight
                        val isKept = i <= nTerms
                        val color = if (isKept) colors.accentCyan else colors.textSecondary.copy(alpha = 0.3f)

                        drawRect(
                            color = color,
                            topLeft = Offset(x + 1f, canvasHeight - normalizedHeight),
                            size = androidx.compose.ui.geometry.Size((barWidth - 2f).coerceAtLeast(1f), normalizedHeight)
                        )
                    }
                }
            }

            Spacer(Modifier.height(AppDesign.spacingSmall))
            Text(
                "Blue bars represent the Fourier terms kept in the reconstruction. Greyed out bars are filtered by your settings.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    val colors = LocalAppColors.current
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", color = colors.textSecondary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(text, color = colors.textSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}