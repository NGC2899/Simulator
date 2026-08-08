package com.example.matharium.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accentCyan,
            secondary = colors.accentViolet,
            tertiary = colors.accentHell,
            surface = colors.bgTop,
            background = colors.bgTop,
            onSurface = colors.textPrimary,
            onBackground = colors.textPrimary,
            surfaceVariant = colors.cardSurface,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.cardBorder,
            error = colors.accentHell
        )
    } else {
        lightColorScheme(
            primary = colors.accentCyan,
            secondary = colors.accentViolet,
            tertiary = colors.accentHell,
            surface = colors.bgTop,
            background = colors.bgTop,
            onSurface = colors.textPrimary,
            onBackground = colors.textPrimary,
            surfaceVariant = colors.cardSurface,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.cardBorder,
            error = colors.accentHell
        )
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            val style = TextStyle(
                color = colors.textPrimary,
                fontSize = AppDesign.textBodyLarge,
                fontWeight = FontWeight.Normal
            )
            ProvideTextStyle(value = style) {
                content()
            }
        }
    }
}
