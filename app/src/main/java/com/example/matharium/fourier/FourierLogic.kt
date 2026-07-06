package com.example.matharium.fourier

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.*

object FourierLogic {

    fun performDFT(drawingPoints: List<Float>, samplesCount: Int): List<Pair<Float, Float>> {
        val coeffs = mutableListOf<Pair<Float, Float>>()
        // Calculate Harmonics up to 50
        for (n in 1..50) {
            var re = 0f
            var im = 0f
            val angleFactor = 2 * PI.toFloat() * n / samplesCount
            for (i in 0 until samplesCount) {
                val angle = angleFactor * i
                re += drawingPoints[i] * cos(angle)
                im += drawingPoints[i] * sin(angle)
            }
            re /= (samplesCount / 2f)
            im /= (samplesCount / 2f)

            val amp = sqrt(re * re + im * im)
            val phase = atan2(re, im)
            coeffs.add(amp to phase)
        }
        return coeffs
    }

    fun synthesizeAudio(
        voiceCoefficients: List<Pair<Float, Float>>,
        nTerms: Int,
        sampleRate: Int,
        durationSeconds: Double
    ): ShortArray {
        val numSamples = (sampleRate * durationSeconds).toInt()
        val audioData = ShortArray(numSamples)
        val activeTerms = nTerms.coerceAtMost(voiceCoefficients.size)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            var sampleValue = 0f
            for (nIdx in 0 until activeTerms) {
                val (amp, phase) = voiceCoefficients[nIdx]
                val freq = (nIdx + 1) * 100f
                val angle = 2 * PI.toFloat() * freq * t + phase
                sampleValue += (amp / 150f) * sin(angle)
            }
            audioData[i] = (sampleValue.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return audioData
    }

    fun processVoiceDFT(audioBuffer: ShortArray, samplesRead: Int): List<Pair<Float, Float>> {
        var maxAmp = 0f
        for (i in 0 until samplesRead) {
            val a = abs(audioBuffer[i].toFloat())
            if (a > maxAmp) maxAmp = a
        }
        val normFactor = if (maxAmp > 0) 32767f / maxAmp else 1f

        val coeffs = mutableListOf<Pair<Float, Float>>()
        val dftSize = 8192.coerceAtMost(samplesRead)
        val startOffset = ((samplesRead - dftSize) / 2).coerceAtLeast(0)
        
        val maxCoeffs = 5000
        for (n in 1..maxCoeffs) {
            var re = 0f
            var im = 0f
            for (i in 0 until dftSize) {
                val sampleIdx = startOffset + i
                val window = 0.5f * (1f - cos(2 * PI.toFloat() * i / (dftSize - 1)))
                val valNormalized = (audioBuffer[sampleIdx] * normFactor) / 32768f
                val sample = valNormalized * window
                
                val angle = 2 * PI.toFloat() * n * i / dftSize
                re += sample * cos(angle)
                im += sample * sin(angle)
            }
            re /= (dftSize / 2f)
            im /= (dftSize / 2f)
            val amp = sqrt(re * re + im * im) * 150f
            val phase = atan2(re, im)
            coeffs.add(amp to phase)
        }
        return coeffs
    }
}
