package com.example.myapplication.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.fourier.SignalInstance
import com.example.myapplication.pendulum.PendulumInstance

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isDarkTheme: Boolean
        get() = prefs.getBoolean("is_dark_theme", true)
        set(value) = prefs.edit().putBoolean("is_dark_theme", value).apply()

    // Fourier Settings
    var fourierNTerms: Int
        get() = prefs.getInt("fourier_n_terms", 5)
        set(value) = prefs.edit().putInt("fourier_n_terms", value).apply()

    var drawingPoints: List<Float>
        get() = prefs.getString("drawing_points", "")?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toFloatOrNull() } ?: emptyList()
        set(value) = prefs.edit().putString("drawing_points", value.joinToString(",")).apply()

    fun saveFourierSignals(signals: List<SignalInstance>) {
        val serialized = signals.joinToString(";") { "${it.id}|${it.color.toArgb()}|${it.freq}|${it.amp}" }
        prefs.edit().putString("fourier_signals", serialized).apply()
    }

    fun loadFourierSignals(accentColor: Color): List<SignalInstance> {
        val data = prefs.getString("fourier_signals", null) ?: return listOf(SignalInstance(0, accentColor))
        return data.split(";").filter { it.isNotEmpty() }.map {
            val parts = it.split("|")
            SignalInstance(
                id = parts[0].toInt(),
                color = Color(parts[1].toInt()),
                initialFreq = parts[2],
                initialAmp = parts[3]
            )
        }
    }

    // Pendulum Settings
    var pendulumScale: Float
        get() = prefs.getFloat("pendulum_scale", 100f)
        set(value) = prefs.edit().putFloat("pendulum_scale", value).apply()

    fun savePendulums(pendulums: List<PendulumInstance>) {
        val serialized = pendulums.joinToString(";") { 
            "${it.id}|${it.baseColor.toArgb()}|${it.t1}|${it.t2}|${it.l1}|${it.l2}" 
        }
        prefs.edit().putString("pendulums", serialized).apply()
    }

    fun loadPendulums(accentColor: Color): List<PendulumInstance> {
        val data = prefs.getString("pendulums", null) ?: return listOf(PendulumInstance(0, accentColor))
        return data.split(";").filter { it.isNotEmpty() }.map {
            val parts = it.split("|")
            PendulumInstance(
                id = parts[0].toInt(),
                baseColor = Color(parts[1].toInt()),
                t1Initial = parts[2],
                t2Initial = parts[3]
            ).apply {
                l1 = parts[4]
                l2 = parts[5]
            }
        }
    }
}
