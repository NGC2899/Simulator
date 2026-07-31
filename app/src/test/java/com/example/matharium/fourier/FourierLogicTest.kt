package com.example.matharium.fourier

import androidx.compose.ui.geometry.Offset
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
}
