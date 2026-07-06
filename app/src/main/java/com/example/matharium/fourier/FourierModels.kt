package com.example.matharium.fourier

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class WaveType {
    MY_SIGNAL, VOICE, CUSTOM_FUNCTION, TRIANGLE, SINE, SAWTOOTH, SQUARE
}

enum class FourierDisplayMode {
    CIRCULAR, WRAPPING, COMPLEX
}

class SignalInstance(
    val id: Int,
    var color: Color,
    initialFreq: String = "1.0",
    initialAmp: String = "50.0"
) {
    var freq by mutableStateOf(initialFreq)
    var amp by mutableStateOf(initialAmp)
    var isExpanded by mutableStateOf(false)
}
