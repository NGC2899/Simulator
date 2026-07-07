package com.example.matharium.fourier

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

    /**
     * Performs a Forward Fourier Transform on a single segment.
     */
    fun fft(samples: FloatArray): Array<Complex> {
        val n = samples.size
        val result = Array(n) { i -> Complex(samples[i].toDouble(), 0.0) }
        
        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val temp = result[i]
                result[i] = result[j]
                result[j] = temp
            }
            var m = n shr 1
            while (m >= 1 && j >= m) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        // Iterative FFT
        var len = 2
        while (len <= n) {
            val ang = 2 * PI / len
            val wlen = Complex(cos(ang), -sin(ang))
            for (i in 0 until n step len) {
                var w = Complex(1.0, 0.0)
                for (k in 0 until len / 2) {
                    val u = result[i + k]
                    val v = result[i + k + len / 2] * w
                    result[i + k] = u + v
                    result[i + k + len / 2] = u - v
                    w *= wlen
                }
            }
            len = len shl 1
        }
        return result
    }

    /**
     * Performs an Inverse Fourier Transform on a single segment.
     */
    fun ifft(coeffs: Array<Complex>): FloatArray {
        val n = coeffs.size
        val reversed = Array(n) { i -> coeffs[i].conj() }
        
        // Bit-reversal
        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val temp = reversed[i]
                reversed[i] = reversed[j]
                reversed[j] = temp
            }
            var m = n shr 1
            while (m >= 1 && j >= m) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        var len = 2
        while (len <= n) {
            val ang = 2 * PI / len
            val wlen = Complex(cos(ang), sin(ang)) // Conjugate angle for IFFT
            for (i in 0 until n step len) {
                var w = Complex(1.0, 0.0)
                for (k in 0 until len / 2) {
                    val u = reversed[i + k]
                    val v = reversed[i + k + len / 2] * w
                    reversed[i + k] = u + v
                    reversed[i + k + len / 2] = u - v
                    w *= wlen
                }
            }
            len = len shl 1
        }
        
        return FloatArray(n) { (reversed[it].re / n).toFloat() }
    }

    data class Complex(val re: Double, val im: Double) {
        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
        fun conj() = Complex(re, -im)
    }
}
