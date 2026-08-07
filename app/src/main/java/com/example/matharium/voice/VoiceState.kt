package com.example.matharium.voice

import androidx.compose.runtime.*
import kotlinx.coroutines.*

/**
 * Manages the state and orchestration for Voice Processing simulation.
 */
class VoiceState(
    val scope: CoroutineScope
) {
    var isRecording by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    
    var rawAudio by mutableStateOf<ShortArray?>(null)
    var processedAudio by mutableStateOf<ShortArray?>(null)
    var magnitudes by mutableStateOf<FloatArray?>(null)
    
    var nTerms by mutableFloatStateOf(64f)

    private var processJob: Job? = null

    fun startRecording() {
        isRecording = true
        scope.launch(Dispatchers.Default) {
            val audio = VoiceHardwareManager.recordAudio { isRecording }
            withContext(Dispatchers.Main) {
                rawAudio = audio
                isRecording = false
                if (audio != null) {
                    processAudio()
                }
            }
        }
    }

    fun playProcessed() {
        val audio = processedAudio ?: return
        isPlaying = true
        scope.launch(Dispatchers.Default) {
            VoiceHardwareManager.playAudio(audio)
            withContext(Dispatchers.Main) {
                isPlaying = false
            }
        }
    }

    fun clear() {
        rawAudio = null
        processedAudio = null
        magnitudes = null
    }

    /**
     * Re-processes the raw audio whenever nTerms change.
     */
    fun processAudio() {
        val audio = rawAudio ?: return
        processJob?.cancel()
        processJob = scope.launch(Dispatchers.Default) {
            val result = VoiceEngine.performSTFTFilter(audio, nTerms.toInt())
            withContext(Dispatchers.Main) {
                processedAudio = result.first
                magnitudes = result.second
            }
        }
    }
}

@Composable
fun rememberVoiceState(scope: CoroutineScope = rememberCoroutineScope()): VoiceState {
    return remember { VoiceState(scope) }
}