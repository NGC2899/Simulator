package com.example.matharium.fourier

import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlin.math.*

object FourierLogic {

    fun performDFT(drawingPoints: List<Float>, samplesCount: Int): List<Pair<Float, Float>> {
        // Fast path for 1D signals using FFT
        // Find next power of 2
        var n = 1
        while (n < samplesCount) n = n shl 1
        
        val padded = FloatArray(n)
        for (i in 0 until samplesCount) {
            padded[i] = drawingPoints[i]
        }
        
        val fftResult = fft(padded)
        val coeffs = mutableListOf<Pair<Float, Float>>()
        
        // n=0 (DC offset)
        coeffs.add((fftResult[0].re / samplesCount).toFloat() to 0f)
        
        // Calculate Harmonics up to 250
        for (k in 1..250) {
            if (k >= n / 2) {
                coeffs.add(0f to 0f)
                continue
            }
            // In DFT, the amplitude of harmonic k is 2/N * |X[k]|
            val re = fftResult[k].re / (samplesCount / 2.0)
            val im = fftResult[k].im / (samplesCount / 2.0)
            
            val amp = sqrt(re * re + im * im).toFloat()
            val phase = atan2(im, re).toFloat()
            coeffs.add(amp to phase)
        }
        return coeffs
    }

    fun performComplexDFT(points: List<Offset>): List<ComplexCoeff> {
        val n = points.size
        val coeffs = mutableListOf<ComplexCoeff>()
        // We calculate both positive and negative frequencies for 2D drawing
        // to handle non-symmetric shapes.
        // Let's take up to 250 terms total (e.g., -125 to 125)
        val limit = 125
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

    data class SymmetryResult(val evenPercent: Float, val oddPercent: Float)

    fun detectSymmetry(samples: List<Float>): SymmetryResult {
        if (samples.isEmpty()) return SymmetryResult(0f, 0f)
        val n = samples.size
        
        // Remove DC offset for better symmetry detection
        val dc = samples.average().toFloat()
        val normalized = samples.map { it - dc }
        
        var evenSum = 0f
        var oddSum = 0f
        var totalPower = 0f
        
        for (i in 0 until n) {
            val f1 = normalized[i]
            // f(-x) corresponds to f(n-i) in periodic discrete samples
            val f2 = normalized[(n - i) % n]
            
            val evenPart = (f1 + f2) / 2f
            val oddPart = (f1 - f2) / 2f
            
            evenSum += evenPart * evenPart
            oddSum += oddPart * oddPart
            totalPower += f1 * f1
        }
        
        if (totalPower < 1e-6f) return SymmetryResult(100f, 0f) // Constant function is perfectly even
        
        return SymmetryResult(
            (evenSum / totalPower * 100f).coerceIn(0f, 100f),
            (oddSum / totalPower * 100f).coerceIn(0f, 100f)
        )
    }

    /**
     * Extracts points from SVG path data.
     * Only supports 'd' attribute content from <path> elements.
     * Minimal implementation for demo purposes.
     */
    fun extractPointsFromSVG(svgContent: String): List<Offset> {
        val rawPoints = mutableListOf<Offset>()
        try {
            val tagPattern = "<([a-zA-Z0-9]+)".toRegex()
            val matches = tagPattern.findAll(svgContent)
            for (match in matches) {
                val tagName = match.groupValues[1].lowercase()
                if (tagName != "svg" && tagName != "path" && tagName != "g" && tagName != "defs" && tagName != "style") {
                    throw IllegalArgumentException("SVG contains unsupported element: $tagName. Only paths are allowed.")
                }
            }

            val dPattern = "d=\"([^\"]+)\"".toRegex()
            val dMatches = dPattern.findAll(svgContent)
            
            for (dMatch in dMatches) {
                val d = dMatch.groupValues[1]
                val tokens = mutableListOf<String>()
                val tokenRegex = "([a-df-z])|(-?\\d*\\.?\\d+(?:e[-+]?\\d+)?)"
                val matcher = java.util.regex.Pattern.compile(tokenRegex, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(d)
                while (matcher.find()) {
                    tokens.add(matcher.group())
                }
                
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
                            'm' -> {
                                if (i + 1 < tokens.size) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    val y = tokens[i+1].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) {
                                        currentX += x; currentY += y
                                    } else {
                                        currentX = x; currentY = y
                                    }
                                    startX = currentX; startY = currentY
                                    rawPoints.add(Offset(currentX, currentY))
                                    i += 2
                                    while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                        val nextX = tokens[i].toFloatOrNull() ?: 0f
                                        val nextY = tokens[i+1].toFloatOrNull() ?: 0f
                                        currentX = if (command.isLowerCase()) currentX + nextX else nextX
                                        currentY = if (command.isLowerCase()) currentY + nextY else nextY
                                        rawPoints.add(Offset(currentX, currentY))
                                        i += 2
                                    }
                                }
                            }
                            'l' -> {
                                while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    val y = tokens[i+1].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) {
                                        currentX += x; currentY += y
                                    } else {
                                        currentX = x; currentY = y
                                    }
                                    rawPoints.add(Offset(currentX, currentY))
                                    i += 2
                                }
                            }
                            'h' -> {
                                while (i < tokens.size && !tokens[i][0].isLetter()) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) currentX += x else currentX = x
                                    rawPoints.add(Offset(currentX, currentY))
                                    i++
                                }
                            }
                            'v' -> {
                                while (i < tokens.size && !tokens[i][0].isLetter()) {
                                    val y = tokens[i].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) currentY += y else currentY = y
                                    rawPoints.add(Offset(currentX, currentY))
                                    i++
                                }
                            }
                            'c' -> {
                                while (i + 5 < tokens.size && !tokens[i][0].isLetter()) {
                                    val x1 = if (command.isLowerCase()) currentX + (tokens[i].toFloatOrNull() ?: 0f) else tokens[i].toFloatOrNull() ?: 0f
                                    val y1 = if (command.isLowerCase()) currentY + (tokens[i+1].toFloatOrNull() ?: 0f) else tokens[i+1].toFloatOrNull() ?: 0f
                                    val x2 = if (command.isLowerCase()) currentX + (tokens[i+2].toFloatOrNull() ?: 0f) else tokens[i+2].toFloatOrNull() ?: 0f
                                    val y2 = if (command.isLowerCase()) currentY + (tokens[i+3].toFloatOrNull() ?: 0f) else tokens[i+3].toFloatOrNull() ?: 0f
                                    val ex = if (command.isLowerCase()) currentX + (tokens[i+4].toFloatOrNull() ?: 0f) else tokens[i+4].toFloatOrNull() ?: 0f
                                    val ey = if (command.isLowerCase()) currentY + (tokens[i+5].toFloatOrNull() ?: 0f) else tokens[i+5].toFloatOrNull() ?: 0f
                                    
                                    val curveSteps = 20
                                    for (step in 1..curveSteps) {
                                        val t = step / curveSteps.toFloat()
                                        val u = 1 - t
                                        val px = u*u*u*currentX + 3*u*u*t*x1 + 3*u*t*t*x2 + t*t*t*ex
                                        val py = u*u*u*currentY + 3*u*u*t*y1 + 3*u*t*t*y2 + t*t*t*ey
                                        rawPoints.add(Offset(px, py))
                                    }
                                    currentX = ex; currentY = ey
                                    i += 6
                                }
                            }
                            'q' -> {
                                while (i + 3 < tokens.size && !tokens[i][0].isLetter()) {
                                    val x1 = if (command.isLowerCase()) currentX + (tokens[i].toFloatOrNull() ?: 0f) else tokens[i].toFloatOrNull() ?: 0f
                                    val y1 = if (command.isLowerCase()) currentY + (tokens[i+1].toFloatOrNull() ?: 0f) else tokens[i+1].toFloatOrNull() ?: 0f
                                    val ex = if (command.isLowerCase()) currentX + (tokens[i+2].toFloatOrNull() ?: 0f) else tokens[i+2].toFloatOrNull() ?: 0f
                                    val ey = if (command.isLowerCase()) currentY + (tokens[i+3].toFloatOrNull() ?: 0f) else tokens[i+3].toFloatOrNull() ?: 0f
                                    
                                    val curveSteps = 15
                                    for (step in 1..curveSteps) {
                                        val t = step / curveSteps.toFloat()
                                        val u = 1 - t
                                        val px = u*u*currentX + 2*u*t*x1 + t*t*ex
                                        val py = u*u*currentY + 2*u*t*y1 + t*t*ey
                                        rawPoints.add(Offset(px, py))
                                    }
                                    currentX = ex; currentY = ey
                                    i += 4
                                }
                            }
                            'z' -> {
                                currentX = startX; currentY = startY
                                rawPoints.add(Offset(currentX, currentY))
                            }
                            else -> {
                                while (i < tokens.size && !tokens[i][0].isLetter()) i++
                            }
                        }
                    } else i++
                }
            }
            
            if (rawPoints.isEmpty()) return emptyList()

            // 1. Resample points uniformly along the path
            val resampledPoints = mutableListOf<Offset>()
            val targetSampleCount = 1000
            
            var totalPathLength = 0f
            val segments = mutableListOf<Pair<Offset, Offset>>()
            for (i in 0 until rawPoints.size - 1) {
                val p1 = rawPoints[i]
                val p2 = rawPoints[i+1]
                val dist = sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))
                if (dist > 0.1f) {
                    totalPathLength += dist
                    segments.add(p1 to p2)
                }
            }
            
            if (segments.isEmpty()) return emptyList()
            
            val stepSize = totalPathLength / targetSampleCount
            var currentDist = 0f
            var currentSegmentIdx = 0
            
            resampledPoints.add(segments[0].first)
            for (i in 1 until targetSampleCount) {
                val targetDist = i * stepSize
                while (currentDist + sqrt((segments[currentSegmentIdx].second.x - segments[currentSegmentIdx].first.x).pow(2) + (segments[currentSegmentIdx].second.y - segments[currentSegmentIdx].first.y).pow(2)) < targetDist) {
                    val d = sqrt((segments[currentSegmentIdx].second.x - segments[currentSegmentIdx].first.x).pow(2) + (segments[currentSegmentIdx].second.y - segments[currentSegmentIdx].first.y).pow(2))
                    currentDist += d
                    currentSegmentIdx++
                    if (currentSegmentIdx >= segments.size) break
                }
                if (currentSegmentIdx >= segments.size) break
                
                val seg = segments[currentSegmentIdx]
                val segLen = sqrt((seg.second.x - seg.first.x).pow(2) + (seg.second.y - seg.first.y).pow(2))
                val t = (targetDist - currentDist) / segLen
                resampledPoints.add(Offset(
                    seg.first.x + (seg.second.x - seg.first.x) * t,
                    seg.first.y + (seg.second.y - seg.first.y) * t
                ))
            }

            // 2. Center and Scale
            val minX = resampledPoints.minOf { it.x }
            val maxX = resampledPoints.maxOf { it.x }
            val minY = resampledPoints.minOf { it.y }
            val maxY = resampledPoints.maxOf { it.y }
            
            val width = maxX - minX
            val height = maxY - minY
            val maxDim = max(width, height).coerceAtLeast(1f)
            val scale = 2.5f / maxDim
            val avgX = (minX + maxX) / 2f
            val avgY = (minY + maxY) / 2f
            
            return resampledPoints.map { 
                val x = (it.x - avgX) * scale
                val y = (it.y - avgY) * scale
                if (x.isNaN() || y.isNaN()) Offset.Zero else Offset(x, y)
            }
            
        } catch (e: Exception) {
            Log.e("FourierLogic", "Error parsing SVG", e)
        }
        return emptyList()
    }

    data class SimulationPoint(val time: Float, val value: Float)

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
            val ang = 2.0 * PI / len
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
        val result = Array(n) { i -> Complex(coeffs[i].re, coeffs[i].im) }
        
        // Bit-reversal
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

        var len = 2
        while (len <= n) {
            val ang = 2.0 * PI / len
            val wlen = Complex(cos(ang), sin(ang)) // Correct angle for IFFT
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
        
        return FloatArray(n) { (result[it].re / n).toFloat() }
    }

    data class Complex(val re: Double, val im: Double) {
        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
        fun conj() = Complex(re, -im)
    }
}
