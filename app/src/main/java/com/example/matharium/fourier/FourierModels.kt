package com.example.matharium.fourier

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class WaveType {
    MY_SIGNAL, MY_SIGNAL_2D, PURE_SIGNAL, FORMULA, SVG, SQUARE, SINE, SAWTOOTH, TRIANGLE
}

enum class FourierDisplayMode {
    CIRCULAR, WRAPPING, COMPLEX
}

class SignalInstance(
    val id: Int,
    var color: Color,
    initialFreq: String = "1.0",
    initialAmp: String = "0.5"
) {
    var freq by mutableStateOf(initialFreq)
    var amp by mutableStateOf(initialAmp)
    var isExpanded by mutableStateOf(false)
    var isPaused by mutableStateOf(false)

    // Cached values for high-performance simulation loop
    var cachedFreq: Float = initialFreq.toFloatOrNull() ?: 1.0f
    var cachedAmp: Float = initialAmp.toFloatOrNull() ?: 0.5f

    fun updateCache() {
        cachedFreq = freq.toFloatOrNull() ?: 0.0f
        cachedAmp = amp.toFloatOrNull() ?: 0.0f
    }
}

data class PathPoint(val offset: Offset, val error: Float)
