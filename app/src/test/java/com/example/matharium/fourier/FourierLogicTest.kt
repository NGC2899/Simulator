package com.example.matharium.fourier

import androidx.compose.ui.graphics.Color
import com.example.matharium.fourier.engine.FourierLogic
import com.example.matharium.fourier.state.FourierDisplayMode
import com.example.matharium.fourier.state.SignalInstance
import com.example.matharium.fourier.state.WaveType
import org.junit.Assert.assertTrue
import org.junit.Test

class FourierLogicTest {

    @Test
    fun testExtractPointsFromSVG_Robustness() {
        val svgContent = """
            <svg viewBox="0 0 100 100">
                <metadata>Some metadata that used to crash us</metadata>
                <path d="M 10,10 L 90,90" />
                <circle cx="50" cy="50" r="40" />
                <path d='M 0 0 L 10 0, 10 10, 0 10 Z' />
            </svg>
        """.trimIndent()

        val points = FourierLogic.extractPointsFromSVG(svgContent)
        assertTrue("Should extract points even with metadata and circles", points.isNotEmpty())
    }

    @Test
    fun testExtractPointsFromSVG_SingleQuotesAndCommas() {
        val svgContent = """<path d='M10,20 L30,40' />"""
        val points = FourierLogic.extractPointsFromSVG(svgContent)
        assertTrue("Should handle single quotes and commas", points.isNotEmpty())
    }

    @Test
    fun testExtractPointsFromSVG_ScientificNotation() {
        val svgContent = """<path d="M0,0 L1.2e-1,1.2e-1" />"""
        val points = FourierLogic.extractPointsFromSVG(svgContent)
        assertTrue("Should handle scientific notation", points.isNotEmpty())
    }

    @Test
    fun testLocaleSensitiveParsing() {
        val value = 1.5f
        // Simulate a locale that uses comma as decimal separator
        val formattedWithComma = "1,50" 
        val parsedWithComma = formattedWithComma.toFloatOrNull()
        assertTrue("Standard toFloatOrNull fails with comma: $formattedWithComma", parsedWithComma == null)

        // Verify that forcing Locale.US during formatting fixes the issue
        val formattedWithUS = String.format(java.util.Locale.US, "%.2f", value)
        val parsedWithUS = formattedWithUS.toFloatOrNull()
        assertTrue("Parsing works when formatted with Locale.US: $formattedWithUS", parsedWithUS == 1.5f)
    }

    @Test
    fun testGetIdealValue_SVGAlignment() {
        val svgPoints = listOf(FourierLogic.MathPoint(0f, 1f)) // Math space (up positive)
        val target = FourierLogic.getIdealValue(
            time = 0f,
            waveType = WaveType.SVG,
            radiusBase = 100f,
            displayMode = FourierDisplayMode.COMPLEX,
            drawingPoints = emptyList(),
            drawingPoints2D = emptyList(),
            resampledPoints2D = emptyList(),
            svgPoints = svgPoints,
            formulaString = "",
            customFunctionSignals = emptyList()
        )
        // Math Y = 1, Radius = 100 -> Screen Y should be -100 (Y-down positive)
        assertTrue("SVG target should be Y-negated for screen: ${target.y}", target.y == -100f)
    }

    @Test
    fun testGetIdealValue_PureSignalDynamic() {
        val signal = SignalInstance(0, Color.Red, "1.0", "1.0", "0.0")
        signal.updateCache()
        val customSignals = listOf(signal)
        val harmonicAmplitudes = mapOf(0 to 2.0f) // Override amplitude
        
        val target = FourierLogic.getIdealValue(
            time = 0.25f, // t=1/4 cycle
            waveType = WaveType.PURE_SIGNAL,
            radiusBase = 100f,
            displayMode = FourierDisplayMode.CIRCULAR,
            drawingPoints = emptyList(),
            drawingPoints2D = emptyList(),
            resampledPoints2D = emptyList(),
            svgPoints = emptyList(),
            formulaString = "",
            customFunctionSignals = customSignals,
            harmonicAmplitudes = harmonicAmplitudes
        )
        // freq=1, time=0.25 -> angle = 2*PI*1*0.25 = PI/2. sin(PI/2)=1. y = -amp * sin = - (2.0 * 100) * 1 = -200
        assertTrue("Pure signal target should respect harmonic overrides: ${target.y}", Math.abs(target.y - (-200f)) < 0.1f)
    }

    @Test
    fun testFourierPhaseConvention_Sine() {
        val samples = List(1024) { i ->
            kotlin.math.sin(2.0 * kotlin.math.PI * i / 1024.0).toFloat()
        }
        val coeffs = FourierLogic.performDFT(samples, 1024)
        val h1 = coeffs[1] // Harmonic at f=1
        // Expect amp=1, phase=0 for A sin(2πft + φ)
        assertTrue("Sine phase should be 0: ${h1.second}", kotlin.math.abs(h1.second) < 0.01f)
        assertTrue("Sine amplitude should be 1: ${h1.first}", kotlin.math.abs(h1.first - 1f) < 0.01f)
    }

    @Test
    fun testFourierPhaseConvention_Cosine() {
        val samples = List(1024) { i ->
            kotlin.math.cos(2.0 * kotlin.math.PI * i / 1024.0).toFloat()
        }
        val coeffs = FourierLogic.performDFT(samples, 1024)
        val h1 = coeffs[1]
        // Cos(x) = Sin(x + π/2). Expect phase = π/2
        assertTrue("Cosine phase should be π/2: ${h1.second}", kotlin.math.abs(h1.second - (kotlin.math.PI.toFloat() / 2f)) < 0.01f)
    }

    @Test
    fun testFourierPhaseConvention_Mixed() {
        val samples = List(1024) { i ->
            val t = i.toDouble() / 1024.0
            (kotlin.math.sin(2.0 * kotlin.math.PI * 1.0 * t) + 0.5 * kotlin.math.sin(2.0 * kotlin.math.PI * 3.0 * t)).toFloat()
        }
        val coeffs = FourierLogic.performDFT(samples, 1024)
        val h1 = coeffs[1]
        val h3 = coeffs[3]
        assertTrue("h1 amp should be 1: ${h1.first}", kotlin.math.abs(h1.first - 1f) < 0.01f)
        assertTrue("h3 amp should be 0.5: ${h3.first}", kotlin.math.abs(h3.first - 0.5f) < 0.01f)
        assertTrue("h1 phase should be 0: ${h1.second}", kotlin.math.abs(h1.second) < 0.01f)
        assertTrue("h3 phase should be 0: ${h3.second}", kotlin.math.abs(h3.second) < 0.01f)
    }

    @Test
    fun testFourierDCOffset_Parabola() {
        // f(x) = x^2 on [-PI, PI] sampled over 1024 points
        val samples = List(1024) { i ->
            val x = (i.toDouble() / 1024.0) * 2.0 * kotlin.math.PI - kotlin.math.PI
            (x * x).toFloat()
        }
        val coeffs = FourierLogic.performDFT(samples, 1024)
        
        // DC component (k=0) should be PI^2 / 3 ~= 3.289
        val h0 = coeffs[0]
        val expectedDC = (kotlin.math.PI * kotlin.math.PI / 3.0).toFloat()
        assertTrue("DC offset should be PI^2/3 (~3.289): ${h0.first}", kotlin.math.abs(h0.first - expectedDC) < 0.1f)
        assertTrue("DC phase should be PI/2: ${h0.second}", kotlin.math.abs(h0.second - (kotlin.math.PI.toFloat() / 2f)) < 0.01f)
        
        // h1 should be -4/1^2 = -4. In sin convention with phase PI/2, it's -4 sin(x + PI/2) = -4 cos(x).
        // Since PI^2/3 + 4(-1)^1 cos(x) = PI^2/3 - 4 cos(x). Correct.
        val h1 = coeffs[1]
        assertTrue("h1 amp should be ~4.0: ${h1.first}", kotlin.math.abs(kotlin.math.abs(h1.first) - 4.0f) < 0.1f)
    }

    @Test
    fun testFourierSymmetry_Gaussian() {
        // f(x) = 2^(-x^2) is even.
        val samples = List(1024) { i ->
            val x = (i.toDouble() / 1024.0) * 2.0 * kotlin.math.PI - kotlin.math.PI
            java.lang.Math.pow(2.0, -(x * x)).toFloat()
        }
        val coeffs = FourierLogic.performDFT(samples, 1024)
        
        // Even signal should have phases PI/2 or -PI/2 in our A sin(2pi ft + phi) convention.
        // (Since sin(x + PI/2) = cos(x))
        for (k in 1..10) {
            val h = coeffs[k]
            if (h.first > 0.001f) {
                // Normalize phase to [-PI/2, PI/2] by adding/subtracting PI
                var normalizedPhase = h.second
                while (normalizedPhase > kotlin.math.PI.toFloat() / 2f) normalizedPhase -= kotlin.math.PI.toFloat()
                while (normalizedPhase < -kotlin.math.PI.toFloat() / 2f) normalizedPhase += kotlin.math.PI.toFloat()
                
                val isCosineLike = kotlin.math.abs(kotlin.math.abs(normalizedPhase) - (kotlin.math.PI.toFloat() / 2f)) < 0.1f
                assertTrue("Harmonic $k should be cosine-like (abs phase PI/2): ${h.second}", isCosineLike)
            }
        }
    }

    @Test
    fun testFourierSymmetry_Cubic() {
        // f(x) = x^3 is odd about x=0. 
        // In our [0, 1] domain, x=0 is t=0.5.
        val samples = List(1024) { i ->
            val x = (i.toDouble() / 1024.0) * 2.0 * kotlin.math.PI - kotlin.math.PI
            (x * x * x).toFloat()
        }
        val coeffs = FourierLogic.performDFT(samples, 1024)
        
        // Odd signal about t=0.5.
        // Recall b_n_sampled = (-1)^n * b_n_math.
        // Odd function on [0, 1] should have phases 0 or PI.
        for (k in 1..10) {
            val h = coeffs[k]
            if (h.first > 0.001f) {
                var normalizedPhase = h.second
                while (normalizedPhase > kotlin.math.PI.toFloat() / 2f) normalizedPhase -= kotlin.math.PI.toFloat()
                while (normalizedPhase < -kotlin.math.PI.toFloat() / 2f) normalizedPhase += kotlin.math.PI.toFloat()
                
                val isSineLike = kotlin.math.abs(normalizedPhase) < 0.1f
                assertTrue("Harmonic $k should be sine-like (abs phase near 0): ${h.second}", isSineLike)
            }
        }
    }
}
