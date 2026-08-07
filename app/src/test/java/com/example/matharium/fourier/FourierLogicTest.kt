package com.example.matharium.fourier

import androidx.compose.ui.geometry.Offset
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
        val svgPoints = listOf(Offset(0f, 1f)) // Math space (up positive)
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
    fun testGetIdealValue_SquareWave() {
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
        assertTrue("Square wave first half should be -100 (up)", targetFirstHalf.y == -100f)

        val targetSecondHalf = FourierLogic.getIdealValue(
            time = 0.75f,
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
        assertTrue("Square wave second half should be 100 (down)", targetSecondHalf.y == 100f)
    }
}
