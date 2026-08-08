package com.example.matharium.app

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

interface AppColors {
    val bgTop:          Color
    val bgBottom:       Color
    val cardSurface:    Color
    val cardBorder:     Color
    val accentCyan:     Color
    val accentViolet:   Color
    val accentHell:     Color
    val textPrimary:    Color
    val textSecondary:  Color
    val textOnAccent:   Color
    val fieldBorder:    Color
    val fieldFocused:   Color
    val fieldLabel:     Color
    val pendulum:       Color
    val trailStart:     Color
    val trailEnd:       Color
    val pivot:          Color
    val yellow:         Color
    val gray:           Color
    val isDark:         Boolean
}

object DarkColors : AppColors {
    override val bgTop:          Color = Color(0xFF060B18)
    override val bgBottom:       Color = Color(0xFF0D1126)
    override val cardSurface:    Color = Color(0xFF1A2040)
    override val cardBorder:     Color = Color(0xFF2A3560)
    override val accentCyan:     Color = Color(0xFF00F5D4)
    override val accentViolet:   Color = Color(0xFF9619DA)
    override val accentHell:     Color = Color(0xFFF5007F)
    override val textPrimary:    Color = Color(0xFFE2EAF4)
    override val textSecondary:  Color = Color(0xFF7A8BB0)
    override val textOnAccent:   Color = Color(0xFF060B18)
    override val fieldBorder:    Color = Color(0xFF2A3560)
    override val fieldFocused:   Color = Color(0xFF00F5D4)
    override val fieldLabel:     Color = Color(0xFF7A8BB0)
    override val pendulum:       Color = Color(0xFFEEEEEE)
    override val trailStart:     Color = Color(0x8000F5D4)
    override val trailEnd:       Color = Color(0x00000000)
    override val pivot:          Color = Color(0xFFFFFFFF)
    override val yellow:         Color = Color(0xFFFFEB3B)
    override val gray:           Color = Color(0xFFCBCBCB)
    override val isDark:         Boolean = true
}

object LightColors : AppColors {
    override val bgTop:          Color = Color(0xFFE8EEF8)
    override val bgBottom:       Color = Color(0xFFF4F7FF)
    override val cardSurface:    Color = Color(0xFFFFFFFF)
    override val cardBorder:     Color = Color(0xFFCDD5E8)
    override val accentCyan:     Color = Color(0xFF0891B2)
    override val accentViolet:   Color = Color(0xFF9F29E0)
    override val accentHell:     Color = Color(0xFFFF62B4)
    override val textPrimary:    Color = Color(0xFF1E293B)
    override val textSecondary:  Color = Color(0xFF64748B)
    override val textOnAccent:   Color = Color(0xFFFFFFFF)
    override val fieldBorder:    Color = Color(0xFFCDD5E8)
    override val fieldFocused:   Color = Color(0xFF0891B2)
    override val fieldLabel:     Color = Color(0xFF64748B)
    override val pendulum:       Color = Color(0xFF151515)
    override val trailStart:     Color = Color(0x800891B2)
    override val trailEnd:       Color = Color(0x00000000)
    override val pivot:          Color = Color(0xFF1E293B)
    override val yellow:         Color = Color(0xFFDECC2D)
    override val gray:           Color = Color(0xFFA2A2A2)
    override val isDark:         Boolean = false
}

val LocalAppColors = staticCompositionLocalOf<AppColors> { DarkColors }
