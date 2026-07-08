package com.example.matharium.pendulum

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.matharium.app.*

@Composable
fun EnergyDashboard(
    modifier: Modifier,
    pendulums: List<PendulumInstance>,
    colors: AppColors
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Kinetic Energy",
            color = colors.textSecondary,
            fontSize = AppDesign.textOverline,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = AppDesign.spacingExtraSmall)
        )
        pendulums.take(DoublePendulumConstants.INITIAL_DISPLAY_COUNT / 2 + 1).forEach { p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(AppDesign.radiusSmall)
                        .clip(CircleShape)
                        .background(p.currentColor)
                )
                Spacer(Modifier.width(DoublePendulumConstants.SPACER_TINY))
                LinearProgressIndicator(
                    progress = { (p.kineticEnergy.toFloat() * DoublePendulumConstants.ENERGY_PROGRESS_MULTIPLIER).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(DoublePendulumConstants.ENERGY_PROGRESS_WIDTH)
                        .height(AppDesign.spacingExtraSmall),
                    color = p.currentColor,
                    trackColor = Color.White.copy(alpha = AppDesign.opacityLow)
                )
            }
            Spacer(Modifier.height(AppDesign.spacingExtraSmall))
        }
    }
}

@Composable
fun PendulumSettingsCard(
    p: PendulumInstance,
    colors: AppColors,
    showDel: Boolean,
    onParameterChange: () -> Unit,
    onDel: () -> Unit
) {
    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(AppDesign.spacingSmall)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { p.isExpanded = !p.isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(AppDesign.radiusSmall)
                            .clip(CircleShape)
                            .background(p.currentColor)
                    )
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(
                        "Pendulum  ${p.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppDesign.textBodyLarge
                    )
                }
                Row {
                    if (showDel) IconButton(
                        onClick = onDel,
                        modifier = Modifier.size(AppDesign.iconLarge)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = colors.accentHell,
                            modifier = Modifier.size(AppDesign.iconSmall)
                        )
                    }
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Icon(
                        if (p.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = colors.textSecondary
                    )
                }
            }
            AnimatedVisibility(
                visible = p.isExpanded,
                enter = expandVertically(animationSpec = tween(AppDesign.animDurationStandard)) + fadeIn(
                    animationSpec = tween(
                        AppDesign.animDurationStandard
                    )
                ),
                exit = shrinkVertically(animationSpec = tween(AppDesign.animDurationStandard)) + fadeOut(
                    animationSpec = tween(
                        AppDesign.animDurationStandard
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(top = AppDesign.spacingSmall),
                    horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                ) {
                    Column(Modifier.weight(1f)) {
                        PendulumField("L1", p.l1, colors) {
                            p.l1 = it; onParameterChange()
                        }; PendulumField("θ1 (°)", p.t1, colors) { p.t1 = it; onParameterChange() }
                    }
                    Column(Modifier.weight(1f)) {
                        PendulumField("L2", p.l2, colors) {
                            p.l2 = it; onParameterChange()
                        }; PendulumField("θ2 (°)", p.t2, colors) { p.t2 = it; onParameterChange() }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawChaosTrail(trail: List<Offset>, color: Color, scale: Float) {
    if (trail.size < 2) return
    for (i in 0 until trail.size - 1) {
        val alpha = (i.toFloat() / trail.size) * DoublePendulumConstants.COLOR_SATURATION_ALT
        drawLine(
            color.copy(alpha = alpha),
            trail[i] * scale,
            trail[i + 1] * scale,
            AppDesign.strokeThick,
            StrokeCap.Round
        )
    }
}

@Composable
fun PendulumField(
    label: String,
    value: String,
    colors: AppColors,
    onValueChange: (String) -> Unit
) {
    val isError = value.toDoubleOrNull() == null
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = AppDesign.textCaption) },
        textStyle = LocalTextStyle.current.copy(fontSize = AppDesign.textBody),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDesign.spacingSmall),
        shape = RoundedCornerShape(AppDesign.radiusSmall),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.fieldFocused,
            focusedLabelColor = colors.accentCyan,
            unfocusedBorderColor = colors.fieldBorder,
            errorBorderColor = colors.accentHell,
            errorLabelColor = colors.accentHell
        )
    )
}
