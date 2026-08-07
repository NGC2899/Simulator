package com.example.matharium.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.matharium.fourier.state.SignalInstance
import com.example.matharium.pendulum.state.PendulumInstance

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isDarkTheme: Boolean
        get() = prefs.getBoolean("is_dark_theme", true)
        set(value) = prefs.edit().putBoolean("is_dark_theme", value).apply()

    var isAnimatedBackground: Boolean
        get() = prefs.getBoolean("is_animated_background", true)
        set(value) = prefs.edit().putBoolean("is_animated_background", value).apply()

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean("haptic_feedback_enabled", true)
        set(value) = prefs.edit().putBoolean("haptic_feedback_enabled", value).apply()

    // Fourier Settings
    var fourierNTerms: Int
        get() = prefs.getInt("fourier_n_terms", 5)
        set(value) = prefs.edit().putInt("fourier_n_terms", value).apply()

    var fourierWaveType: String
        get() = prefs.getString("fourier_wave_type", "SQUARE") ?: "SQUARE"
        set(value) = prefs.edit().putString("fourier_wave_type", value).apply()

    var fourierSpeed: Float
        get() = prefs.getFloat("fourier_speed", 1.0f)
        set(value) = prefs.edit().putFloat("fourier_speed", value).apply()

    var fourierDisplayMode: String
        get() = prefs.getString("fourier_display_mode", "CIRCULAR") ?: "CIRCULAR"
        set(value) = prefs.edit().putString("fourier_display_mode", value).apply()

    var fourierFormula: String
        get() = prefs.getString("fourier_formula", "abs(sin(x))") ?: "abs(sin(x))"
        set(value) = prefs.edit().putString("fourier_formula", value).apply()

    var fourierWindingFrequency: Float
        get() = prefs.getFloat("fourier_winding_frequency", 1.0f)
        set(value) = prefs.edit().putFloat("fourier_winding_frequency", value).apply()

    var fourierErrorSensitivity: Float
        get() = prefs.getFloat("fourier_error_sensitivity", 20.0f)
        set(value) = prefs.edit().putFloat("fourier_error_sensitivity", value).apply()

    var fourierWaveStretch: Float
        get() = prefs.getFloat("fourier_wave_stretch", 120.0f)
        set(value) = prefs.edit().putFloat("fourier_wave_stretch", value).apply()

    var fourierShowErrorGradient: Boolean
        get() = prefs.getBoolean("fourier_show_error_gradient", false)
        set(value) = prefs.edit().putBoolean("fourier_show_error_gradient", value).apply()

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

    var fourierSvgPoints: List<Offset>
        get() {
            val data = prefs.getString("fourier_svg_points", "") ?: ""
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
        set(value) = prefs.edit().putString("fourier_svg_points", value.joinToString(";") { "${it.x},${it.y}" }).apply()

    private fun escape(s: String) = s.replace("%", "%25").replace("|", "%7C").replace(";", "%3B")
    private fun unescape(s: String) = s.replace("%7C", "|").replace("%3B", ";").replace("%25", "%")

    fun saveFourierSignals(signals: List<SignalInstance>) {
        val serialized = signals.joinToString(";") { "${it.id}|${it.colorArgb}|${escape(it.freq)}|${escape(it.amp)}|${escape(it.phase)}" }
        prefs.edit().putString("fourier_signals", serialized).apply()
    }

    fun loadFourierSignals(defaultColorArgb: Int): List<SignalInstance> {
        val data = prefs.getString("fourier_signals", null) ?: return listOf(SignalInstance(0, defaultColorArgb))
        return try {
            data.split(";").filter { it.isNotEmpty() }.map {
                val parts = it.split("|")
                SignalInstance(
                    id = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    colorArgb = parts.getOrNull(1)?.toIntOrNull() ?: defaultColorArgb,
                    initialFreq = unescape(parts.getOrNull(2) ?: "1.0"),
                    initialAmp = unescape(parts.getOrNull(3) ?: "0.5"),
                    initialPhase = unescape(parts.getOrNull(4) ?: "0.0")
                )
            }
        } catch (e: Exception) {
            listOf(SignalInstance(0, defaultColorArgb))
        }
    }

    var customCoefficients: List<Pair<Float, Float>>
        get() {
            val data = prefs.getString("custom_coeffs", "") ?: ""
            if (data.isEmpty()) return emptyList()
            return data.split(";").filter { it.isNotEmpty() }.mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) {
                    val amp = parts[0].toFloatOrNull()
                    val phase = parts[1].toFloatOrNull()
                    if (amp != null && phase != null) amp to phase else null
                } else null
            }
        }
        set(value) = prefs.edit().putString("custom_coeffs", value.joinToString(";") { "${it.first},${it.second}" }).apply()

    var customCoefficients2D: List<com.example.matharium.fourier.engine.FourierLogic.ComplexCoeff>
        get() {
            val data = prefs.getString("custom_coeffs_2d", "") ?: ""
            if (data.isEmpty()) return emptyList()
            return data.split(";").filter { it.isNotEmpty() }.mapNotNull {
                val parts = it.split(",")
                if (parts.size == 3) {
                    val freq = parts[0].toIntOrNull()
                    val amp = parts[1].toFloatOrNull()
                    val phase = parts[2].toFloatOrNull()
                    if (freq != null && amp != null && phase != null) com.example.matharium.fourier.engine.FourierLogic.ComplexCoeff(freq, amp, phase) else null
                } else null
            }
        }
        set(value) = prefs.edit().putString("custom_coeffs_2d", value.joinToString(";") { "${it.freq},${it.amp},${it.phase}" }).apply()

    // Pendulum Settings
    var pendulumScale: Float
        get() = prefs.getFloat("pendulum_scale", 100f)
        set(value) = prefs.edit().putFloat("pendulum_scale", value).apply()

    var pendulumFrictionEnabled: Boolean
        get() = prefs.getBoolean("pendulum_friction_enabled", false)
        set(value) = prefs.edit().putBoolean("pendulum_friction_enabled", value).apply()

    var pendulumFrictionAmount: Float
        get() = prefs.getFloat("pendulum_friction_amount", 0.0002f)
        set(value) = prefs.edit().putFloat("pendulum_friction_amount", value).apply()

    var pendulumGravityAmount: Float
        get() = prefs.getFloat("pendulum_gravity_amount", 9.8f)
        set(value) = prefs.edit().putFloat("pendulum_gravity_amount", value).apply()

    var pendulumSpeedMultiplier: Float
        get() = prefs.getFloat("pendulum_speed_multiplier", 1f)
        set(value) = prefs.edit().putFloat("pendulum_speed_multiplier", value).apply()

    var pendulumColorByVelocity: Boolean
        get() = prefs.getBoolean("pendulum_color_by_velocity", false)
        set(value) = prefs.edit().putBoolean("pendulum_color_by_velocity", value).apply()

    fun savePendulums(pendulums: List<PendulumInstance>) {
        val serialized = pendulums.joinToString(";") {
            "${it.id}|${it.baseColor.toArgb()}|${escape(it.startT1)}|${escape(it.startT2)}|${escape(it.l1)}|${escape(it.l2)}"
        }
        prefs.edit().putString("pendulums", serialized).apply()
    }

    fun loadPendulums(accentColor: Color): List<PendulumInstance> {
        val data = prefs.getString("pendulums", null) ?: return listOf(PendulumInstance(0, accentColor).apply { updatePositions() })
        return try {
            data.split(";").filter { it.isNotEmpty() }.map {
                val parts = it.split("|")
                PendulumInstance(
                    id = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    baseColor = Color(parts.getOrNull(1)?.toIntOrNull() ?: accentColor.toArgb()),
                    t1Initial = unescape(parts.getOrNull(2) ?: "90.0"),
                    t2Initial = unescape(parts.getOrNull(3) ?: "90.0")
                ).apply {
                    l1 = unescape(parts.getOrNull(4) ?: "1.0")
                    l2 = unescape(parts.getOrNull(5) ?: "1.0")
                    updatePositions()
                }
            }
        } catch (e: Exception) {
            listOf(PendulumInstance(0, accentColor).apply { updatePositions() })
        }
    }
}
