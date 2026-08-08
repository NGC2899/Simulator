package com.example.matharium.fourier.engine

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.example.matharium.fourier.state.*
import kotlin.math.*

object FourierLogic {

    fun performDFT(drawingPoints: List<Float>, samplesCount: Int): List<Pair<Float, Float>> {
        val coeffs = mutableListOf<Pair<Float, Float>>()
        
        // n=0 (DC offset / constant term)
        var re0 = 0.0
        for (i in 0 until samplesCount) {
            re0 += drawingPoints[i]
        }
        re0 /= samplesCount
        coeffs.add(re0.toFloat() to 0f)

        // Calculate Harmonics up to 250
        // We use a high-precision O(N*K) DFT to ensure integer frequencies align perfectly
        for (k in 1..250) {
            var re = 0.0
            var im = 0.0
            val angleFactor = 2.0 * PI * k / samplesCount
            for (i in 0 until samplesCount) {
                val angle = -angleFactor * i
                re += drawingPoints[i] * cos(angle)
                im += drawingPoints[i] * sin(angle)
            }
            // In discrete Fourier series for periodic signals, the amplitude is 2/N * |X|
            re /= (samplesCount / 2.0)
            im /= (samplesCount / 2.0)

            val amp = sqrt(re * re + im * im).toFloat()
            val phase = atan2(im, re).toFloat()
            coeffs.add(amp to phase)
        }
        return coeffs
    }

    fun performComplexDFT(points: List<MathPoint>): List<ComplexCoeff> {
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
                val angle = -2 * PI * k * i / n
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

    data class Harmonic(val freq: Float, val amp: Float, val phase: Float, val colorArgb: Int = 0)

    /**
     * Pre-calculates the first N harmonics for standard wave types.
     */
    fun calculateStandardHarmonics(waveType: WaveType, nTerms: Int): List<Harmonic> {
        val result = mutableListOf<Harmonic>()
        for (i in 0 until nTerms) {
            val freq: Float
            val amp: Float
            val phase: Float
            
            when (waveType) {
                WaveType.SINE -> {
                    if (i > 0) break
                    freq = 1f; amp = 1f; phase = 0f
                }
                WaveType.SQUARE -> {
                    freq = (i * 2 + 1).toFloat()
                    amp = 4f / (freq * PI.toFloat()); phase = 0f
                }
                WaveType.SAWTOOTH -> {
                    freq = (i + 1).toFloat()
                    val sign = if (freq.toInt() % 2 == 0) -1f else 1f
                    amp = (2f / (freq * PI.toFloat())) * sign; phase = 0f
                }
                WaveType.TRIANGLE -> {
                    freq = (i * 2 + 1).toFloat()
                    val sign = if (((freq.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                    amp = (8f / (freq * freq * PI.toFloat() * PI.toFloat())) * sign; phase = 0f
                }
                else -> { freq = 0f; amp = 0f; phase = 0f }
            }
            result.add(Harmonic(freq, amp, phase))
        }
        return result
    }

    /**
     * Data class for a point in math space.
     * Decoupled from Android UI classes.
     */
    data class MathPoint(val x: Float, val y: Float)

    /**
     * Creates a wavetable for the ideal signal across one period [0, 1].
     */
    fun generateWavetable(
        size: Int,
        waveType: WaveType,
        radiusBase: Float,
        drawingPoints: List<Float>,
        drawingPoints2D: List<MathPoint>,
        resampledPoints2D: List<MathPoint>,
        svgPoints: List<MathPoint>,
        formulaString: String,
        customFunctionSignals: List<SignalInstance>
    ): Array<MathPoint> {
        return Array(size) { i ->
            val t = i.toFloat() / size
            val result = getIdealValue(
                t, waveType, radiusBase, FourierDisplayMode.COMPLEX, // Force complex for both components
                drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                formulaString, customFunctionSignals
            )
            MathPoint(result.x, result.y)
        }
    }

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
    fun extractPointsFromSVG(svgContent: String): List<MathPoint> {
        val rawPoints = mutableListOf<MathPoint>()
        try {
            // Relaxed: Extract path data from all 'd' attributes, ignore unknown tags
            val dPattern = "d=(?:\"|')([^\"']+)(?:\"|')".toRegex()
            val dMatches = dPattern.findAll(svgContent)
            
            for (dMatch in dMatches) {
                val d = dMatch.groupValues[1]
                val tokens = mutableListOf<String>()
                // tokenRegex captures commands and numbers (including scientific notation). 
                // Commas and whitespace are naturally skipped as they don't match.
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
                                    rawPoints.add(MathPoint(currentX, currentY))
                                    i += 2
                                    while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                        val nextX = tokens[i].toFloatOrNull() ?: 0f
                                        val nextY = tokens[i+1].toFloatOrNull() ?: 0f
                                        currentX = if (command.isLowerCase()) currentX + nextX else nextX
                                        currentY = if (command.isLowerCase()) currentY + nextY else nextY
                                        rawPoints.add(MathPoint(currentX, currentY))
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
                                    rawPoints.add(MathPoint(currentX, currentY))
                                    i += 2
                                }
                            }
                            'h' -> {
                                while (i < tokens.size && !tokens[i][0].isLetter()) {
                                    val x = tokens[i].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) currentX += x else currentX = x
                                    rawPoints.add(MathPoint(currentX, currentY))
                                    i++
                                }
                            }
                            'v' -> {
                                while (i < tokens.size && !tokens[i][0].isLetter()) {
                                    val y = tokens[i].toFloatOrNull() ?: 0f
                                    if (command.isLowerCase()) currentY += y else currentY = y
                                    rawPoints.add(MathPoint(currentX, currentY))
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
                                        rawPoints.add(MathPoint(px, py))
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
                                        rawPoints.add(MathPoint(px, py))
                                    }
                                    currentX = ex; currentY = ey
                                    i += 4
                                }
                            }
                            'z' -> {
                                currentX = startX; currentY = startY
                                rawPoints.add(MathPoint(currentX, currentY))
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
            val resampledPoints = resamplePath(rawPoints, 1000)
            
            if (resampledPoints.isEmpty()) return emptyList()

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
                if (x.isNaN() || y.isNaN()) MathPoint(0f, 0f) else MathPoint(x, -y) // Negate Y to align with Math space
            }
            
        } catch (e: Exception) {
            Log.e("FourierLogic", "Error parsing SVG", e)
        }
        return emptyList()
    }

    /**
     * Resamples a path to have a uniform distribution of points.
     */
    fun resamplePath(points: List<MathPoint>, targetCount: Int): List<MathPoint> {
        if (points.size < 2) return points
        if (targetCount <= 1) return listOf(points[0])

        var totalLength = 0f
        val segmentLengths = mutableListOf<Float>()
        for (i in 0 until points.size - 1) {
            val dx = points[i+1].x - points[i].x
            val dy = points[i+1].y - points[i].y
            val d = sqrt(dx * dx + dy * dy)
            totalLength += d
            segmentLengths.add(d)
        }

        if (totalLength < 1e-6f) return List(targetCount) { points[0] }

        val resampled = mutableListOf<MathPoint>()
        val step = totalLength / (targetCount - 1)
        
        var accumulatedLength = 0f
        var currentSegment = 0
        
        resampled.add(points[0])
        
        for (i in 1 until targetCount - 1) {
            val targetDist = i * step
            while (currentSegment < segmentLengths.size - 1 && accumulatedLength + segmentLengths[currentSegment] < targetDist) {
                accumulatedLength += segmentLengths[currentSegment]
                currentSegment++
            }
            
            val distInSegment = targetDist - accumulatedLength
            val t = if (segmentLengths[currentSegment] > 0) distInSegment / segmentLengths[currentSegment] else 0f
            
            val p1 = points[currentSegment]
            val p2 = points[currentSegment + 1]
            resampled.add(MathPoint(
                p1.x + (p2.x - p1.x) * t,
                p1.y + (p2.y - p1.y) * t
            ))
        }
        
        resampled.add(points.last())
        return resampled
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

    fun getIdealValue(
        time: Float,
        waveType: WaveType,
        radiusBase: Float,
        displayMode: FourierDisplayMode,
        drawingPoints: List<Float>,
        drawingPoints2D: List<MathPoint>,
        resampledPoints2D: List<MathPoint>,
        svgPoints: List<MathPoint>,
        formulaString: String,
        customFunctionSignals: List<SignalInstance>,
        harmonicFrequencies: Map<Int, Float> = emptyMap(),
        harmonicAmplitudes: Map<Int, Float> = emptyMap(),
        harmonicPhases: Map<Int, Float> = emptyMap()
    ): MathPoint {
        // To unify and fix the error gradient logic, the "Ideal Value" now represents
        // the true mathematical or sampled target signal, NOT the Fourier approximation.
        // This allows the error gradient to reflect how well the series converges.

        val normalizedT = ((time % 1f) + 1f) % 1f
        var sumX = 0f
        var sumY = 0f

        when (waveType) {
            WaveType.SINE -> {
                val angle = 2 * PI.toFloat() * 1f * time
                sumX = radiusBase * cos(angle)
                sumY = -radiusBase * sin(angle)
            }
            WaveType.SQUARE -> {
                val angle = 2 * PI.toFloat() * 1f * time
                val value = if (sin(angle) >= 0) 1f else -1f
                sumX = 0f // Squares are usually 1D in this app's context
                sumY = -radiusBase * value
            }
            WaveType.SAWTOOTH -> {
                // Sawtooth from 1 to -1 over one period
                val value = 1f - 2f * normalizedT
                sumX = 0f
                sumY = -radiusBase * value
            }
            WaveType.TRIANGLE -> {
                // Triangle from 0 to 1 to 0 to -1 to 0
                val t = normalizedT * 4f
                val value = when {
                    t < 1f -> t
                    t < 3f -> 2f - t
                    else -> t - 4f
                }
                sumX = 0f
                sumY = -radiusBase * value
            }
            WaveType.MY_SIGNAL -> {
                if (drawingPoints.isNotEmpty()) {
                    val floatIdx = normalizedT * (drawingPoints.size - 1)
                    val i1 = floatIdx.toInt()
                    val i2 = (i1 + 1).coerceAtMost(drawingPoints.size - 1)
                    val frac = floatIdx - i1
                    // drawingPoints stores raw pixel offsets which are already screen-aligned (Y-down)
                    val y = drawingPoints[i1] * (1 - frac) + drawingPoints[i2] * frac
                    sumX = 0f
                    sumY = y
                }
            }
            WaveType.FORMULA -> {
                val x = normalizedT.toDouble() * 2.0 * PI - PI
                val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
                sumX = 0f
                sumY = -(if (eval.isFinite()) eval.toFloat() else 0f) * radiusBase
            }
            WaveType.MY_SIGNAL_2D -> {
                val source = if (resampledPoints2D.isNotEmpty()) resampledPoints2D else drawingPoints2D
                if (source.isNotEmpty()) {
                    val floatIdx = normalizedT * (source.size - 1)
                    val i1 = floatIdx.toInt()
                    val i2 = (i1 + 1) % source.size
                    val frac = floatIdx - i1
                    val p1 = source[i1]
                    val p2 = source[i2]
                    sumX = p1.x * (1 - frac) + p2.x * frac
                    sumY = p1.y * (1 - frac) + p2.y * frac
                }
            }
            WaveType.SVG -> {
                if (svgPoints.isNotEmpty()) {
                    val floatIdx = normalizedT * (svgPoints.size - 1)
                    val i1 = floatIdx.toInt()
                    val i2 = (i1 + 1) % svgPoints.size
                    val frac = floatIdx - i1
                    val p1 = svgPoints[i1]
                    val p2 = svgPoints[i2]
                    // svgPoints are now Math-aligned (Y-up), so we negate to get screen-aligned Y
                    sumX = (p1.x * (1 - frac) + p2.x * frac) * radiusBase
                    sumY = -(p1.y * (1 - frac) + p2.y * frac) * radiusBase
                }
            }
            WaveType.PURE_SIGNAL -> {
                // For Pure Signal (manual builder), the target is the sum of ALL active signals.
                for (i in customFunctionSignals.indices) {
                    val s = customFunctionSignals[i]
                    if (s.isPaused) continue
                    val freq = harmonicFrequencies[i] ?: s.cachedFreq
                    val amp = (harmonicAmplitudes[i] ?: s.cachedAmp) * radiusBase
                    val phase = harmonicPhases[i] ?: s.cachedPhase
                    val angle = 2 * PI.toFloat() * freq * time + phase
                    sumX += amp * cos(angle)
                    sumY += -amp * sin(angle)
                }
            }
        }

        return if (displayMode == FourierDisplayMode.COMPLEX) {
            MathPoint(sumX, sumY)
        } else {
            MathPoint(time, sumY)
        }
    }

    data class Complex(val re: Double, val im: Double) {
        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
        fun conj() = Complex(re, -im)
    }
}
