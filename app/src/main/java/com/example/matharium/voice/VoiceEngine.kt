package com.example.matharium.voice

import com.example.matharium.fourier.engine.FourierLogic
import kotlin.math.*

/**
 * Pure Mathematical Engine for Voice Processing.
 * No Android dependencies.
 */
object VoiceEngine {

    const val SAMPLE_RATE = 48000
    const val WINDOW_SIZE = 2048
    const val HOP_SIZE = WINDOW_SIZE / 4 // 75% overlap

    /**
     * Performs Short-Time Fourier Transform (STFT) filtering.
     * Keeps only the specified number of low-frequency terms.
     */
    fun performSTFTFilter(audio: ShortArray, nTerms: Int): Pair<ShortArray, FloatArray> {
        val totalSamples = audio.size
        val outputSamples = FloatArray(totalSamples)
        val windowNormalization = FloatArray(totalSamples)
        
        val window = FloatArray(WINDOW_SIZE) { i ->
            // Hann Window
            0.5f * (1f - cos(2 * PI.toFloat() * i / (WINDOW_SIZE - 1)))
        }

        var bestMagnitudes: FloatArray? = null
        var maxEnergy = 0f

        for (i in 0 until (totalSamples - WINDOW_SIZE) step HOP_SIZE) {
            val segment = FloatArray(WINDOW_SIZE) { j ->
                audio[i + j].toFloat() * window[j]
            }

            // FFT
            val spectrum = FourierLogic.fft(segment)

            // Capture magnitudes for visualization
            val currentMagnitudes = FloatArray(WINDOW_SIZE / 2) { k ->
                val c = spectrum[k]
                sqrt(c.re * c.re + c.im * c.im).toFloat()
            }
            
            val energy = currentMagnitudes.sum()
            if (energy > maxEnergy) {
                maxEnergy = energy
                bestMagnitudes = currentMagnitudes
            }

            // Filter: Keep only the specified number of low-frequency terms
            val kLimit = nTerms.coerceIn(1, WINDOW_SIZE / 2)
            for (k in 0 until WINDOW_SIZE) {
                if (k > kLimit && k < WINDOW_SIZE - kLimit) {
                    spectrum[k] = FourierLogic.Complex(0.0, 0.0)
                }
            }

            // IFFT
            val reconstructed = FourierLogic.ifft(spectrum)

            // Overlap-Add with Normalization
            for (j in 0 until WINDOW_SIZE) {
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

        return finalAudio to (bestMagnitudes ?: FloatArray(WINDOW_SIZE / 2))
    }
}