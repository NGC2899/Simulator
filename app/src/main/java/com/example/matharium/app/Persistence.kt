package com.example.matharium.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.matharium.fourier.SignalInstance
import com.example.matharium.pendulum.PendulumInstance

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

    var drawingPoints2D: List<Offset>
        get() {
            val data = prefs.getString("drawing_points_2d", "") ?: ""
            if (data.isEmpty()) return emptyList()
            return data.split(";").filter { it.isNotEmpty() }.mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) {
                    val x = parts[0].toFloatOrNull()
                    val y = parts[1].toFloatOrNull()
                    if (x != null && y != null) Offset(x, y) else null
                } else null
            }
        }
        set(value) = prefs.edit().putString("drawing_points_2d", value.joinToString(";") { "${it.x},${it.y}" }).apply()

    fun saveFourierSignals(signals: List<SignalInstance>) {
        val serialized = signals.joinToString(";") { "${it.id}|${it.color.toArgb()}|${it.freq}|${it.amp}" }
        prefs.edit().putString("fourier_signals", serialized).apply()
    }

    fun loadFourierSignals(accentColor: Color): List<SignalInstance> {
        val data = prefs.getString("fourier_signals", null) ?: return listOf(SignalInstance(0, accentColor))
        return try {
            data.split(";").filter { it.isNotEmpty() }.map {
                val parts = it.split("|")
                SignalInstance(
                    id = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    color = Color(parts.getOrNull(1)?.toIntOrNull() ?: accentColor.toArgb()),
                    initialFreq = parts.getOrNull(2) ?: "1.0",
                    initialAmp = parts.getOrNull(3) ?: "50.0"
                )
            }
        } catch (e: Exception) {
            listOf(SignalInstance(0, accentColor))
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
        return try {
            data.split(";").filter { it.isNotEmpty() }.map {
                val parts = it.split("|")
                PendulumInstance(
                    id = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    baseColor = Color(parts.getOrNull(1)?.toIntOrNull() ?: accentColor.toArgb()),
                    t1Initial = parts.getOrNull(2) ?: "90.0",
                    t2Initial = parts.getOrNull(3) ?: "90.0"
                ).apply {
                    l1 = parts.getOrNull(4) ?: "1.0"
                    l2 = parts.getOrNull(5) ?: "1.0"
                }
            }
        } catch (e: Exception) {
            listOf(PendulumInstance(0, accentColor))
        }
    }
}
