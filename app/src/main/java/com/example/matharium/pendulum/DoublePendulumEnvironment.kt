package com.example.matharium.pendulum

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale

@Composable
fun DoublePendulumEnvironment(
    colors: AppColors,
    speedMultiplier: Float,
    onSpeedChange: (Float) -> Unit,
    gravityAmount: Float,
    onGravityChange: (Float) -> Unit,
    frictionEnabled: Boolean,
    onFrictionEnabledChange: (Boolean) -> Unit,
    frictionAmount: Float,
    onFrictionAmountChange: (Float) -> Unit,
    colorByVelocity: Boolean,
    onColorByVelocityChange: (Boolean) -> Unit
) {
    var isEnvExpanded by remember { mutableStateOf(false) }

    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isEnvExpanded = !isEnvExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Environment Settings",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    if (isEnvExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(
                        id = R.drawable.chevron_down_outline
                    ),
                    null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
            }

            AnimatedVisibility(visible = isEnvExpanded) {
                Column(modifier = Modifier.padding(top = AppDesign.spacingMedium)) {
                    // Speed Slider
                    LabeledSlider(
                        label = "Time Scale",
                        valueDisplay = String.format(Locale.US, "%.1fx", speedMultiplier),
                        value = speedMultiplier,
                        range = DoublePendulumConstants.SLIDER_SPEED_RANGE,
                        colors = colors
                    ) { onSpeedChange(it) }

                    // Gravity Slider
                    LabeledSlider(
                        label = "Gravity",
                        valueDisplay = "$gravityAmount m/s²",
                        value = gravityAmount,
                        range = DoublePendulumConstants.SLIDER_GRAVITY_RANGE,
                        colors = colors
                    ) { onGravityChange(it) }

                    Spacer(Modifier.height(AppDesign.spacingSmall))

                    // Friction Toggle
                    ToggleRow(
                        label = "Air Resistance",
                        checked = frictionEnabled,
                        onCheckedChange = { onFrictionEnabledChange(it) },
                        colors = colors
                    )

                    AnimatedVisibility(
                        visible = frictionEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LabeledSlider(
                            label = "Damping Factor",
                            valueDisplay = String.format(Locale.US, "%.4f", frictionAmount),
                            value = frictionAmount,
                            range = DoublePendulumConstants.SLIDER_FRICTION_RANGE,
                            colors = colors
                        ) {
                            onFrictionAmountChange(it)
                        }
                    }

                    Spacer(Modifier.height(AppDesign.spacingSmall))

                    // Visual Options
                    ToggleRow(
                        label = "Color by Velocity (Heatmap)",
                        checked = colorByVelocity,
                        onCheckedChange = { onColorByVelocityChange(it) },
                        colors = colors
                    )
                }
            }
        }
    }
}
