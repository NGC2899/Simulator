package com.example.matharium.fourier.state

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
    var colorArgb: Color,
    val initialFreq: String = "1.0",
    val initialAmp: String = "0.5",
    val initialPhase: String = "0.0"
) {
    var freq by mutableStateOf(initialFreq)
    var amp by mutableStateOf(initialAmp)
    var phase by mutableStateOf(initialPhase)
    var isExpanded by mutableStateOf(false)
    var isPaused by mutableStateOf(false)

    // Cached values for high-performance simulation loop
    var cachedFreq by mutableStateOf(initialFreq.toFloatOrNull() ?: 1.0f)
    var cachedAmp by mutableStateOf(initialAmp.toFloatOrNull() ?: 0.5f)
    var cachedPhase by mutableStateOf((initialPhase.toFloatOrNull() ?: 0.0f) * (Math.PI.toFloat() / 180f))

    fun updateCache() {
        cachedFreq = freq.toFloatOrNull() ?: 0.0f
        cachedAmp = amp.toFloatOrNull() ?: 0.0f
        cachedPhase = (phase.toFloatOrNull() ?: 0.0f) * (Math.PI.toFloat() / 180f)
    }
}

data class PathPoint(val offset: Offset, val error: Float)
