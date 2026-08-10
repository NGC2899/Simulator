package com.example.matharium.app

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun GlassCard(
    colors: AppColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = AppDesign.opacityGlass))
            .border(
                AppDesign.borderThin,
                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                RoundedCornerShape(AppDesign.radiusCard)
            )
    ) {
        content()
    }
}

@Composable
fun LabeledSlider(
    label: String,
    valueDisplay: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    colors: AppColors,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = AppDesign.spacingSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = colors.textPrimary,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valueDisplay,
                color = colors.accentCyan,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(colors.accentCyan.copy(AppDesign.opacityLow), RoundedCornerShape(AppDesign.radiusSmall))
                    .padding(horizontal = AppDesign.spacingSmall, vertical = AppDesign.spacingTiny)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentCyan,
                activeTrackColor = colors.accentCyan,
                inactiveTrackColor = colors.fieldBorder.copy(AppDesign.opacityMedium)
            ),
            modifier = Modifier.padding(top = AppDesign.spacingTiny)
        )
    }
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.textFieldHeight)
            .clip(RoundedCornerShape(AppDesign.radiusSmall))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = AppDesign.spacingExtraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.accentCyan,
                uncheckedColor = colors.fieldBorder,
                checkmarkColor = colors.textOnAccent
            )
        )
        Spacer(Modifier.width(AppDesign.spacingSmall))
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = AppDesign.textBodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun rememberAppVibrator(): (Boolean) -> Unit {
    val context = LocalContext.current
    val prefs = LocalAppPrefs.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    return { enabled ->
        if (enabled && prefs.hapticFeedbackEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        }
    }
}

fun Modifier.hapticClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = this.composed {
    val vibrate = rememberAppVibrator()

    this.clickable(
        enabled = enabled,
        onClick = {
            vibrate(enabled)
            onClick()
        }
    )
}
