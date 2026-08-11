package com.example.matharium.fourier

import com.example.matharium.fourier.engine.FourierExpressionEvaluator
import com.example.matharium.fourier.engine.FourierLogic
import com.example.matharium.fourier.state.FourierDisplayMode
import com.example.matharium.fourier.state.SignalInstance
import com.example.matharium.fourier.state.WaveType
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

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
    fun testGetIdealValue_SineReversed() {
        // With reversed simulation fix: y = -A sin(-2PI*t + phi)
        // For f=1, t=0.25 (1/4 cycle), phi=0: y = -A sin(-PI/2) = -A * (-1) = A (Down)
        // Note: For 1D, we usually expect sin(x) to go UP then DOWN in space.
        // spatial_y(x) = y(t - x/v) = sin(-(t-x/v)) = sin(x/v - t).
        // At t=0, spatial_y(x) = sin(x/v) which goes UP. Correct.
        
        val target = FourierLogic.getIdealValue(
            time = 0.25f,
            waveType = WaveType.SINE,
            radiusBase = 100f,
            displayMode = FourierDisplayMode.CIRCULAR,
            drawingPoints = emptyList(),
            drawingPoints2D = emptyList(),
            resampledPoints2D = emptyList(),
            svgPoints = emptyList(),
            formulaString = "",
            customFunctionSignals = emptyList()
        )
        // t=0.25 -> angle = -2*PI*0.25 = -PI/2. approxY = -100 * sin(-PI/2) = 100 (Down)
        assertTrue("Sine ideal value at t=0.25 should be 100: ${target.y}", abs(target.y - 100f) < 0.1f)
    }

    @Test
    fun testGetIdealValue_SquareReversed() {
        val targetFirstHalf = FourierLogic.getIdealValue(
            time = 0.25f,
            waveType = WaveType.SQUARE,
            radiusBase = 100f,
            displayMode = FourierDisplayMode.CIRCULAR,
            drawingPoints = emptyList(),
            drawingPoints2D = emptyList(),
            resampledPoints2D = emptyList(),
            svgPoints = emptyList(),
            formulaString = "",
            customFunctionSignals = emptyList()
        )
        // angle = -PI/2, sin(angle)=-1, y = -100*(-1) = 100 (Down)
        assertTrue("Square wave first half should be 100 (down)", targetFirstHalf.y == 100f)
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

    @Test
    fun testFourierExpressionParser_Precedence() {
        // -x^2 at x=2 should be -4
        val e1 = FourierExpressionEvaluator.evaluate("-x^2", 2.0)
        assertTrue("Precedence -x^2 should be -4: $e1", abs(e1 - (-4.0)) < 1e-9)

        // (-x)^2 at x=2 should be 4
        val e2 = FourierExpressionEvaluator.evaluate("(-x)^2", 2.0)
        assertTrue("Precedence (-x)^2 should be 4: $e2", abs(e2 - 4.0) < 1e-9)

        // 2x at x=3 should be 6
        val e3 = FourierExpressionEvaluator.evaluate("2x", 3.0)
        assertTrue("Implicit mult 2x should be 6: $e3", abs(e3 - 6.0) < 1e-9)

        // 2(x+1) at x=2 should be 6
        val e4 = FourierExpressionEvaluator.evaluate("2(x+1)", 2.0)
        assertTrue("Implicit mult 2(x+1) should be 6: $e4", abs(e4 - 6.0) < 1e-9)

        // 2sin(x) at x=PI/2 should be 2
        val e5 = FourierExpressionEvaluator.evaluate("2sin(x)", PI / 2.0)
        assertTrue("Implicit mult 2sin(x) should be 2: $e5", abs(e5 - 2.0) < 1e-9)

        // 2^-x^2 at x=0 should be 1, at x=1 should be 0.5
        val e6 = FourierExpressionEvaluator.evaluate("2^-x^2", 0.0)
        assertTrue("2^-x^2 at 0 should be 1: $e6", abs(e6 - 1.0) < 1e-9)
        val e7 = FourierExpressionEvaluator.evaluate("2^-x^2", 1.0)
        assertTrue("2^-x^2 at 1 should be 0.5: $e7", abs(e7 - 0.5) < 1e-9)
    }

    @Test
    fun testFourierExpressionParser_UnaryAndExponents() {
        // -2^2 should be -4
        val e1 = FourierExpressionEvaluator.evaluate("-2^2", 0.0)
        assertTrue("-2^2 should be -4: $e1", abs(e1 - (-4.0)) < 1e-9)

        // 2^-2 should be 0.25
        val e2 = FourierExpressionEvaluator.evaluate("2^-2", 0.0)
        assertTrue("2^-2 should be 0.25: $e2", abs(e2 - 0.25) < 1e-9)

        // x^2^3 is x^(2^3) = x^8. at x=2, result 256
        val e3 = FourierExpressionEvaluator.evaluate("x^2^3", 2.0)
        assertTrue("x^2^3 should be x^8 = 256: $e3", abs(e3 - 256.0) < 1e-9)
    }

    @Test
    fun testFourierExpressionParser_NumericalValues() {
        val testValues = listOf(2.0, -2.0, 0.5)
        
        for (x in testValues) {
            // -x^2
            val r1 = FourierExpressionEvaluator.evaluate("-x^2", x)
            assertTrue("-x^2 at $x should be ${-(x*x)}: $r1", abs(r1 - (-(x*x))) < 1e-9)

            // (-x)^2
            val r2 = FourierExpressionEvaluator.evaluate("(-x)^2", x)
            assertTrue("(-x)^2 at $x should be ${(-x)*(-x)}: $r2", abs(r2 - ((-x)*(-x))) < 1e-9)

            // 2sin(x)
            val r3 = FourierExpressionEvaluator.evaluate("2sin(x)", x)
            assertTrue("2sin(x) at $x should be ${2*sin(x)}: $r3", abs(r3 - (2*sin(x))) < 1e-9)

            // 2^-x^2
            val r4 = FourierExpressionEvaluator.evaluate("2^-x^2", x)
            val expected4 = java.lang.Math.pow(2.0, -(x*x))
            assertTrue("2^-x^2 at $x should be $expected4: $r4", abs(r4 - expected4) < 1e-9)
        }
    }

    @Test
    fun testFourierSymmetryDetection() {
        val n = 1024
        
        // f(x) = x^2 (Even)
        val evenSamples = List(n) { i ->
            val x = (i.toDouble() / n) * 2.0 * PI - PI
            (x * x).toFloat()
        }
        val evenResult = FourierLogic.detectSymmetry(evenSamples)
        assertTrue("x^2 should be detected as primarily even: ${evenResult.evenPercent}", evenResult.evenPercent > 99.0f)
        assertTrue("x^2 should have low odd percentage: ${evenResult.oddPercent}", evenResult.oddPercent < 1.0f)

        // f(x) = x^3 (Odd)
        val oddSamples = List(n) { i ->
            val x = (i.toDouble() / n) * 2.0 * PI - PI
            (x * x * x).toFloat()
        }
        val oddResult = FourierLogic.detectSymmetry(oddSamples)
        assertTrue("x^3 should be detected as primarily odd: ${oddResult.oddPercent}", oddResult.oddPercent > 99.0f)
        assertTrue("x^3 should have low even percentage: ${oddResult.evenPercent}", oddResult.evenPercent < 1.0f)

        // f(x) = x^2 + x (Neither)
        val neitherSamples = List(n) { i ->
            val x = (i.toDouble() / n) * 2.0 * PI - PI
            (x * x + x).toFloat()
        }
        val neitherResult = FourierLogic.detectSymmetry(neitherSamples)
        assertTrue("x^2 + x should be neither perfectly even nor odd", 
            neitherResult.evenPercent > 10f && neitherResult.evenPercent < 90f)
    }
}
