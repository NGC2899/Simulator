package com.example.matharium.voice

import android.annotation.SuppressLint
import android.media.*
import android.util.Log
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Handles Android-specific Audio Hardware interaction.
 */
object VoiceHardwareManager {

    private const val TAG = "VoiceHardwareManager"

    @SuppressLint("MissingPermission")
    suspend fun recordAudio(isRecording: () -> Boolean): ShortArray? {
        val sampleRate = VoiceEngine.SAMPLE_RATE
        val windowSize = 256 // Minimal buffer chunk

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize.coerceAtLeast(windowSize * 2)
        )

        return try {
            val recordDurationMs = 3000
            val totalSamples = sampleRate * recordDurationMs / 1000
            val rawBuffer = ShortArray(totalSamples)

            audioRecord.startRecording()
            var samplesRead = 0
            while (samplesRead < totalSamples && isRecording()) {
                val read = audioRecord.read(rawBuffer, samplesRead, totalSamples - samplesRead)
                if (read > 0) samplesRead += read else break
                delay(10.milliseconds)
            }
            rawBuffer
        } catch (e: Exception) {
            Log.e(TAG, "Error during recording", e)
            null
        } finally {
            try {
                audioRecord.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping AudioRecord", e)
            }
            audioRecord.release()
        }
    }

    suspend fun playAudio(audioData: ShortArray) {
        val sampleRate = VoiceEngine.SAMPLE_RATE
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(audioData.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.play()
            delay((audioData.size.toFloat() / sampleRate * 1000).toLong().milliseconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error during playback", e)
        } finally {
            try {
                audioTrack.stop()
            } catch (e: Exception) { /* Ignore */ }
            audioTrack.release()
        }
    }
}