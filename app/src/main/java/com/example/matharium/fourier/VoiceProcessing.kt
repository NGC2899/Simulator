package com.example.matharium.fourier

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

@Composable
fun VoiceProcessing() {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var processedAudio by remember { mutableStateOf<ShortArray?>(null) }
    var rawAudio by remember { mutableStateOf<ShortArray?>(null) }
    var nTerms by remember { mutableFloatStateOf(64f) }
    var magnitudes by remember { mutableStateOf<FloatArray?>(null) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {
        LabeledSlider(
            label = "Fourier Terms",
            valueDisplay = nTerms.toInt().toString(),
            value = nTerms,
            range = 1f..1000f,
            colors = colors,
            onValueChange = { nTerms = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
        ) {
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
                    .clickable(enabled = !isRecording) {
                        if (!permissionGranted) {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            isRecording = true
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
                        if (isRecording) "Recording..." else "Record",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    processedAudio?.let { audio ->
                        isPlaying = true
                        coroutineScope.launch(Dispatchers.Default) {
                            playAudio(audio)
                            withContext(Dispatchers.Main) { isPlaying = false }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight),
                enabled = processedAudio != null && !isPlaying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentCyan,
                    disabledContainerColor = colors.accentCyan.copy(alpha = 0.3f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(AppDesign.radiusMedium)
            ) {
                Icon(
                    if (isPlaying) painterResource(id = R.drawable.pause_outline) else painterResource(id = R.drawable.caret_forward_outline),
                    null,
                    tint = colors.textOnAccent,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isPlaying) "Playing..." else "Play", color = colors.textOnAccent)
            }

            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .height(AppDesign.buttonHeight)
                    .clip(RoundedCornerShape(AppDesign.radiusMedium))
                    .background(colors.accentHell.copy(alpha = if (rawAudio != null) 0.15f else 0.05f))
                    .border(
                        2.dp,
                        if (rawAudio != null) colors.accentHell.copy(0.6f) else colors.accentHell.copy(0.3f),
                        RoundedCornerShape(AppDesign.radiusMedium)
                    )
                    .clickable(enabled = rawAudio != null) {
                        rawAudio = null
                        processedAudio = null
                        magnitudes = null
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(id = R.drawable.trash_outline),
                    null,
                    tint = if (rawAudio != null) colors.accentHell else colors.accentHell.copy(0.3f),
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
            }
        }

        if (isRecording) {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Default) {
                    val sampleRate = 48000
                    val windowSizeMs = 2 // 2ms interval
                    val windowSize = (sampleRate * windowSizeMs / 1000f).toInt().let {
                        // Round up to power of 2 for FFT
                        var p = 1
                        while (p < it) p = p shl 1
                        p
                    }

                    val minBufferSize = AudioRecord.getMinBufferSize(
                        sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                    )
                    val audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufferSize.coerceAtLeast(windowSize * 2)
                    )

                    try {
                        val recordDurationMs = 3000
                        val totalSamples = sampleRate * recordDurationMs / 1000
                        val rawBuffer = ShortArray(totalSamples)

                        audioRecord.startRecording()
                        var samplesRead = 0
                        while (samplesRead < totalSamples && isRecording) {
                            val read =
                                audioRecord.read(rawBuffer, samplesRead, totalSamples - samplesRead)
                            if (read > 0) samplesRead += read else break
                            delay(10)
                        }
                        
                        withContext(Dispatchers.Main) {
                            rawAudio = rawBuffer
                            isRecording = false
                        }
                    } finally {
                        try {
                            audioRecord.stop()
                        } catch (e: Exception) {
                            Log.e("VoiceProcessing", "Error stopping AudioRecord", e)
                        }
                        audioRecord.release()
                    }
                }
            }
        }

        // Processing Logic (STFT + Filter + OLA)
        LaunchedEffect(rawAudio, nTerms) {
            val audio = rawAudio ?: return@LaunchedEffect
            withContext(Dispatchers.Default) {
                val windowSize = 2048 // 42ms - optimal balance between time and frequency resolution for voice
                val hopSize = windowSize / 4 // 75% overlap for high quality reconstruction
                val totalSamples = audio.size

                val outputSamples = FloatArray(totalSamples)
                val windowNormalization = FloatArray(totalSamples)
                
                val window = FloatArray(windowSize) { i ->
                    // Hann Window
                    0.5f * (1f - cos(2 * PI.toFloat() * i / (windowSize - 1)))
                }

                var bestMagnitudes: FloatArray? = null
                var maxEnergy = 0f

                for (i in 0 until (totalSamples - windowSize) step hopSize) {
                    val segment = FloatArray(windowSize) { j ->
                        audio[i + j].toFloat() * window[j]
                    }

                    // FFT
                    val spectrum = FourierLogic.fft(segment)

                    // Capture magnitudes for visualization
                    val currentMagnitudes = FloatArray(windowSize / 2) { k ->
                        val c = spectrum[k]
                        sqrt(c.re * c.re + c.im * c.im).toFloat()
                    }
                    val energy = currentMagnitudes.sum()
                    if (energy > maxEnergy) {
                        maxEnergy = energy
                        bestMagnitudes = currentMagnitudes
                    }

                    // Filter: Keep only the specified number of low-frequency terms
                    val kLimit = nTerms.toInt()
                    for (k in 0 until windowSize) {
                        if (k > kLimit && k < windowSize - kLimit) {
                            spectrum[k] = FourierLogic.Complex(0.0, 0.0)
                        }
                    }

                    // IFFT
                    val reconstructed = FourierLogic.ifft(spectrum)

                    // Overlap-Add with Normalization
                    for (j in 0 until windowSize) {
                        if (i + j < totalSamples) {
                            outputSamples[i + j] += reconstructed[j] * window[j]
                            windowNormalization[i + j] += window[j] * window[j]
                        }
                    }
                }

                val finalAudio = ShortArray(totalSamples) { i ->
                    val norm = windowNormalization[i].coerceAtLeast(1e-6f)
                    (outputSamples[i] / norm).coerceIn(-32768f, 32767f).toInt().toShort()
                }

                withContext(Dispatchers.Main) {
                    processedAudio = finalAudio
                    magnitudes = bestMagnitudes
                }
            }
        }

        SpectrumVisualizer(
            magnitudes = magnitudes ?: FloatArray(128),
            nTerms = nTerms.toInt(),
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
        Text("•", color = colors.accentCyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(text, color = colors.textSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

private suspend fun playAudio(audioData: ShortArray) {
    val sampleRate = 48000
    val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(audioData.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

    try {
        audioTrack.write(audioData, 0, audioData.size)
        audioTrack.play()
        delay((audioData.size.toFloat() / sampleRate * 1000).toLong())
    } finally {
        audioTrack.stop()
        audioTrack.release()
    }
}
