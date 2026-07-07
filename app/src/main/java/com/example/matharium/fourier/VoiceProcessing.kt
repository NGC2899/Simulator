package com.example.matharium.fourier

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
    
    var permissionGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
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
        GlassCard(colors = colors) {
            Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
                Text(
                    "STFT Voice Processing",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.spacingSmall))
                Text(
                    "This mode processes your voice using Short-Time Fourier Transform (STFT) with 2ms intervals, mimicking modern DSP algorithms.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
        ) {
            Button(
                onClick = {
                    if (!permissionGranted) {
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        isRecording = true
                    }
                },
                modifier = Modifier.weight(1f).height(AppDesign.buttonHeight),
                enabled = !isRecording,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan),
                shape = RoundedCornerShape(AppDesign.radiusMedium)
            ) {
                Icon(painterResource(id = R.drawable.mic_outline), null, tint = colors.textOnAccent)
                Spacer(Modifier.width(8.dp))
                Text(if (isRecording) "Recording..." else "Record")
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
                modifier = Modifier.weight(1f).height(AppDesign.buttonHeight),
                enabled = processedAudio != null && !isPlaying,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentViolet),
                shape = RoundedCornerShape(AppDesign.radiusMedium)
            ) {
                Icon(painterResource(id = R.drawable.caret_forward_outline), null, tint = colors.textOnAccent)
                Spacer(Modifier.width(8.dp))
                Text(if (isPlaying) "Playing..." else "Play Processed")
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
                    val hopSize = windowSize / 2 // 50% overlap

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

                    val recordDurationMs = 3000
                    val totalSamples = sampleRate * recordDurationMs / 1000
                    val rawBuffer = ShortArray(totalSamples)

                    audioRecord.startRecording()
                    var samplesRead = 0
                    while (samplesRead < totalSamples && isRecording) {
                        val read = audioRecord.read(rawBuffer, samplesRead, totalSamples - samplesRead)
                        if (read > 0) samplesRead += read else break
                        delay(10)
                    }
                    audioRecord.stop()
                    audioRecord.release()

                    // Process STFT
                    val outputSamples = FloatArray(totalSamples)
                    val window = FloatArray(windowSize) { i -> 
                        0.5f * (1f - cos(2 * PI.toFloat() * i / (windowSize - 1)))
                    }

                    for (i in 0 until (samplesRead - windowSize) step hopSize) {
                        val segment = FloatArray(windowSize) { j -> 
                            rawBuffer[i + j].toFloat() * window[j]
                        }
                        
                        // FFT
                        val spectrum = FourierLogic.fft(segment)
                        
                        // Filter: Keep high frequency terms (simple high-pass or just pass through for fidelity)
                        // For this educational mode, we can zero out low frequencies to show the effect
                        // spectrum[0] = FourierLogic.Complex(0.0, 0.0) 

                        // IFFT
                        val reconstructed = FourierLogic.ifft(spectrum)
                        
                        // Overlap-Add
                        for (j in 0 until windowSize) {
                            if (i + j < totalSamples) {
                                outputSamples[i + j] += reconstructed[j] * window[j]
                            }
                        }
                    }

                    val finalAudio = ShortArray(totalSamples) { i -> 
                        outputSamples[i].coerceIn(-32768f, 32767f).toInt().toShort()
                    }

                    withContext(Dispatchers.Main) {
                        processedAudio = finalAudio
                        isRecording = false
                    }
                }
            }
        }

        GlassCard(colors = colors) {
            Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
                Text("How it works", fontSize = AppDesign.textHeadline, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(AppDesign.radiusSmall))
                BulletPoint("Signal is sliced into 2ms windows (approx. 96 samples).")
                BulletPoint("FFT converts each slice from time to frequency domain.")
                BulletPoint("Inverse FFT reconstructs the signal back to time domain.")
                BulletPoint("Overlap-Add (OLA) ensures smooth transitions between slices.")
            }
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

private fun playAudio(audioData: ShortArray) {
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

    audioTrack.write(audioData, 0, audioData.size)
    audioTrack.play()
    Thread.sleep((audioData.size.toFloat() / sampleRate * 1000).toLong())
    audioTrack.release()
}
