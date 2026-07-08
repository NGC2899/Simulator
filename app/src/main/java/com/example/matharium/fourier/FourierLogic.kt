package com.example.matharium.fourier

import android.util.Log
import androidx.compose.ui.geometry.Offset
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

    fun performComplexDFT(points: List<Offset>): List<ComplexCoeff> {
        val n = points.size
        val coeffs = mutableListOf<ComplexCoeff>()
        // We calculate both positive and negative frequencies for 2D drawing
        // to handle non-symmetric shapes.
        // Let's take up to 50 terms total (e.g., -25 to 25)
        val limit = 25
        for (k in -limit..limit) {
            var re = 0.0
            var im = 0.0
            for (i in 0 until n) {
                val angle = 2 * PI * k * i / n
                val cosA = cos(angle)
                val sinA = sin(angle)
                // Complex multiplication: (px + i py) * (cosA - i sinA)
                // = (px * cosA + py * sinA) + i (py * cosA - px * sinA)
                re += points[i].x * cosA + points[i].y * sinA
                im += points[i].y * cosA - points[i].x * sinA
            }
            re /= n
            im /= n
            
            val amp = sqrt(re * re + im * im).toFloat()
            val phase = atan2(im, re).toFloat()
            coeffs.add(ComplexCoeff(k, amp, phase))
        }
        // Sort by amplitude for better visualization of epicycles
        return coeffs.sortedByDescending { it.amp }
    }

    data class ComplexCoeff(val freq: Int, val amp: Float, val phase: Float)

    /**
     * Extracts points from SVG path data.
     * Only supports 'd' attribute content from <path> elements.
     * Minimal implementation for demo purposes.
     */
    fun extractPointsFromSVG(svgContent: String): List<Offset> {
        val points = mutableListOf<Offset>()
        try {
            // Very basic validation: Check if there are elements other than <svg> and <path>
            // This is a naive check to satisfy the requirement of "no other elements but paths"
            val tagPattern = "<([a-zA-Z0-9]+)".toRegex()
            val matches = tagPattern.findAll(svgContent)
            for (match in matches) {
                val tagName = match.groupValues[1].lowercase()
                if (tagName != "svg" && tagName != "path" && tagName != "g" && tagName != "defs" && tagName != "style") {
                    throw IllegalArgumentException("SVG contains unsupported element: $tagName. Only paths are allowed.")
                }
            }

            // Extract path data 'd' attributes
            val dPattern = "d=\"([^\"]+)\"".toRegex()
            val dMatches = dPattern.findAll(svgContent)
            
            for (dMatch in dMatches) {
                val d = dMatch.groupValues[1]
                
                // Better SVG path data parsing
                // Split into commands and coordinates, handling both space and comma separators, and negative signs
                val commandRegex = "([a-df-z])|([-+]?\\d*\\.?\\d+)".toRegex(RegexOption.IGNORE_CASE)
                val tokens = commandRegex.findAll(d).map { it.value }.toList()
                
                var currentX = 0f
                var currentY = 0f
                var startX = 0f
                var startY = 0f
                var i = 0
                while (i < tokens.size) {
                    val token = tokens[i]
                    if (token[0].isLetter()) {
                        val command = token[0]
                        i++
                        
                        when (command.lowercaseChar()) {
                            'm' -> { // MoveTo
                                if (i + 1 < tokens.size) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    val y = tokens[i+1].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) {
                                        currentX += x; currentY += y
                                    } else {
                                        currentX = x; currentY = y
                                    }
                                    startX = currentX; startY = currentY
                                    points.add(Offset(currentX, currentY))
                                    i += 2
                                    
                                    // Implicit lineTo for subsequent coordinate pairs
                                    while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                        val nextX = tokens[i].toFloatOrNull() ?: 0f
                                        val nextY = tokens[i+1].toFloatOrNull() ?: 0f
                                        currentX = if (command.isLowerCase()) currentX + nextX else nextX
                                        currentY = if (command.isLowerCase()) currentY + nextY else nextY
                                        points.add(Offset(currentX, currentY))
                                        i += 2
                                    }
                                }
                            }
                            'l' -> { // LineTo
                                while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    val y = tokens[i+1].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) {
                                        currentX += x; currentY += y
                                    } else {
                                        currentX = x; currentY = y
                                    }
                                    points.add(Offset(currentX, currentY))
                                    i += 2
                                }
                            }
                            'h' -> { // Horizontal line
                                while (i < tokens.size && !tokens[i][0].isLetter()) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) currentX += x else currentX = x
                                    points.add(Offset(currentX, currentY))
                                    i++
                                }
                            }
                            'v' -> { // Vertical line
                                while (i < tokens.size && !tokens[i][0].isLetter()) {
                                    val y = tokens[i].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) currentY += y else currentY = y
                                    points.add(Offset(currentX, currentY))
                                    i++
                                }
                            }
                            'c' -> { // Cubic Bézier
                                while (i + 5 < tokens.size && !tokens[i][0].isLetter()) {
                                    // Sample at intervals for smoother curves
                                    val x1 = if (command.isLowerCase()) currentX + (tokens[i].toFloatOrNull() ?: 0f) else tokens[i].toFloatOrNull() ?: 0f
                                    val y1 = if (command.isLowerCase()) currentY + (tokens[i+1].toFloatOrNull() ?: 0f) else tokens[i+1].toFloatOrNull() ?: 0f
                                    val x2 = if (command.isLowerCase()) currentX + (tokens[i+2].toFloatOrNull() ?: 0f) else tokens[i+2].toFloatOrNull() ?: 0f
                                    val y2 = if (command.isLowerCase()) currentY + (tokens[i+3].toFloatOrNull() ?: 0f) else tokens[i+3].toFloatOrNull() ?: 0f
                                    val ex = if (command.isLowerCase()) currentX + (tokens[i+4].toFloatOrNull() ?: 0f) else tokens[i+4].toFloatOrNull() ?: 0f
                                    val ey = if (command.isLowerCase()) currentY + (tokens[i+5].toFloatOrNull() ?: 0f) else tokens[i+5].toFloatOrNull() ?: 0f
                                    
                                    // Simple linear sampling
                                    val steps = 5
                                    for (step in 1..steps) {
                                        val t = step / steps.toFloat()
                                        val u = 1 - t
                                        val px = u*u*u*currentX + 3*u*u*t*x1 + 3*u*t*t*x2 + t*t*t*ex
                                        val py = u*u*u*currentY + 3*u*u*t*y1 + 3*u*t*t*y2 + t*t*t*ey
                                        points.add(Offset(px, py))
                                    }
                                    
                                    currentX = ex; currentY = ey
                                    i += 6
                                }
                            }
                            'q' -> { // Quadratic Bézier
                                while (i + 3 < tokens.size && !tokens[i][0].isLetter()) {
                                    val x1 = if (command.isLowerCase()) currentX + (tokens[i].toFloatOrNull() ?: 0f) else tokens[i].toFloatOrNull() ?: 0f
                                    val y1 = if (command.isLowerCase()) currentY + (tokens[i+1].toFloatOrNull() ?: 0f) else tokens[i+1].toFloatOrNull() ?: 0f
                                    val ex = if (command.isLowerCase()) currentX + (tokens[i+2].toFloatOrNull() ?: 0f) else tokens[i+2].toFloatOrNull() ?: 0f
                                    val ey = if (command.isLowerCase()) currentY + (tokens[i+3].toFloatOrNull() ?: 0f) else tokens[i+3].toFloatOrNull() ?: 0f
                                    
                                    val steps = 5
                                    for (step in 1..steps) {
                                        val t = step / steps.toFloat()
                                        val u = 1 - t
                                        val px = u*u*currentX + 2*u*t*x1 + t*t*ex
                                        val py = u*u*currentY + 2*u*t*y1 + t*t*ey
                                        points.add(Offset(px, py))
                                    }
                                    currentX = ex; currentY = ey
                                    i += 4
                                }
                            }
                            'z' -> { // Close path
                                currentX = startX; currentY = startY
                                points.add(Offset(currentX, currentY))
                            }
                            else -> {
                                // Skip unknown command coordinates
                                while (i < tokens.size && !tokens[i][0].isLetter()) i++
                            }
                        }
                    } else i++
                }
            }
            
            // Center the points
            if (points.isNotEmpty()) {
                val avgX = points.map { it.x }.average().toFloat()
                val avgY = points.map { it.y }.average().toFloat()
                return points.map { Offset(it.x - avgX, it.y - avgY) }
            }
        } catch (e: Exception) {
            Log.e("FourierLogic", "Error parsing SVG", e)
        }
        return points
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
