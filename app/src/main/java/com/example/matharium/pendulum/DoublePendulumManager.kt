package com.example.matharium.pendulum

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale
import kotlin.random.Random

@Composable
fun DoublePendulumManager(
    colors: AppColors,
    pendulums: SnapshotStateList<PendulumInstance>,
    running: Boolean,
    onRunningChange: (Boolean) -> Unit,
    hasStarted: Boolean,
    onHasStartedChange: (Boolean) -> Unit,
    nextId: Int,
    onNextIdChange: (Int) -> Unit,
    prefs: AppPreferences
) {
    var isPendulumManagerExpanded by remember { mutableStateOf(false) }
    var isPendulumsExpanded by remember { mutableStateOf(false) }

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier.padding(AppDesign.spacingLarge)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPendulumManagerExpanded = !isPendulumManagerExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pendulum Manager",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    if (isPendulumManagerExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(
                        id = R.drawable.chevron_down_outline
                    ),
                    null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
            }

            Spacer(Modifier.height(AppDesign.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
            ) {
                // Gradient Border Add Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(AppDesign.buttonHeightSmall)
                        .clip(RoundedCornerShape(AppDesign.radiusButton))
                        .background(colors.cardSurface.copy(AppDesign.opacityLow))
                        .border(
                            BorderStroke(
                                AppDesign.borderStandard,
                                Brush.linearGradient(
                                    listOf(colors.accentCyan, colors.accentViolet)
                                )
                            ),
                            RoundedCornerShape(AppDesign.radiusButton)
                        )
                        .clickable {
                            val nextT1 = "90.0"
                            val nextT2 = "90.0"
                            val color = Color.hsv(Random.nextFloat() * 360f, DoublePendulumConstants.COLOR_SATURATION, DoublePendulumConstants.COLOR_VALUE)
                            val newPendulum = PendulumInstance(nextId, color, nextT1, nextT2)
                            onNextIdChange(nextId + 1)
                            newPendulum.updatePositions()
                            pendulums.add(newPendulum)
                            if (!running) onHasStartedChange(false)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_outline),
                            null,
                            modifier = Modifier.size(AppDesign.iconSmall),
                            tint = colors.textPrimary
                        )
                        Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                        Text(
                            "Add",
                            fontSize = AppDesign.textSmall,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Chaos Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(AppDesign.buttonHeightSmall)
                        .clip(RoundedCornerShape(AppDesign.radiusButton))
                        .background(colors.accentViolet.copy(AppDesign.opacityLow))
                        .border(
                            AppDesign.borderStandard,
                            colors.accentViolet.copy(AppDesign.opacityMedium),
                            RoundedCornerShape(AppDesign.radiusButton)
                        )
                        .clickable {
                            if (pendulums.size < DoublePendulumConstants.MAX_PENDULUMS) {
                                var currentNextId = nextId
                                repeat(DoublePendulumConstants.CHAOS_BATCH_COUNT) {
                                    if (pendulums.size >= DoublePendulumConstants.MAX_PENDULUMS) return@repeat
                                    val nextT1Str = String.format(
                                        Locale.US,
                                        "%.1f",
                                        90.0 + (pendulums.size * DoublePendulumConstants.CHAOS_ANGLE_INCREMENT)
                                    )
                                    val randomColor =
                                        Color.hsv(Random.nextFloat() * 360f, DoublePendulumConstants.COLOR_SATURATION, DoublePendulumConstants.COLOR_VALUE)
                                    val newPendulum = PendulumInstance(
                                        currentNextId++,
                                        randomColor,
                                        nextT1Str,
                                        "90.0"
                                    )
                                    newPendulum.updatePositions()
                                    pendulums.add(newPendulum)
                                }
                                onNextIdChange(currentNextId)
                                if (!running) onHasStartedChange(false)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.cloud),
                            null,
                            modifier = Modifier.size(AppDesign.iconSmall),
                            tint = colors.accentViolet
                        )
                        Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                        Text(
                            "Chaos",
                            fontSize = AppDesign.textSmall,
                            color = colors.accentViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Remove All Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(AppDesign.buttonHeightSmall)
                        .clip(RoundedCornerShape(AppDesign.radiusButton))
                        .background(colors.accentHell.copy(AppDesign.opacityLow))
                        .border(
                            AppDesign.borderStandard,
                            colors.accentHell.copy(AppDesign.opacityMedium),
                            RoundedCornerShape(AppDesign.radiusButton)
                        )
                        .clickable(enabled = !running) {
                            onRunningChange(false)
                            onHasStartedChange(false)
                            pendulums.clear()
                            onNextIdChange(1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.trash_outline),
                            null,
                            tint = colors.accentHell,
                            modifier = Modifier.size(AppDesign.iconSmall)
                        )
                        Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                        Text(
                            "Clear",
                            fontSize = AppDesign.textSmall,
                            color = colors.accentHell,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isPendulumManagerExpanded,
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
                Column(
                    modifier = Modifier
                        .padding(top = AppDesign.spacingSmall)
                        .animateContentSize(animationSpec = tween(AppDesign.animDurationStandard)),
                    verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                ) {
                    if (pendulums.isEmpty()) Text(
                        "Add a Pendulum",
                        color = colors.accentCyan,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppDesign.spacingLarge),
                    ) else Text("")
                    // Truncated list for better performance
                    val displayList = if (isPendulumsExpanded) pendulums else pendulums.take(DoublePendulumConstants.INITIAL_DISPLAY_COUNT)
                    Column(verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)) {
                        displayList.forEachIndexed { _, p ->
                            PendulumSettingsCard(
                                p,
                                colors,
                                pendulums.size > 1 && !running,
                                onParameterChange = {
                                    onRunningChange(false)
                                    onHasStartedChange(false)
                                    p.updatePositions()
                                    prefs.savePendulums(pendulums.toList())
                                }) {
                                onRunningChange(false)
                                onHasStartedChange(false)
                                pendulums.remove(p)
                                if (pendulums.isEmpty()) {
                                    onNextIdChange(1)
                                }
                            }
                        }

                        if (pendulums.size > DoublePendulumConstants.INITIAL_DISPLAY_COUNT) {
                            TextButton(
                                onClick = { isPendulumsExpanded = !isPendulumsExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (isPendulumsExpanded) "Show Less" else "Show All Pendulums (${pendulums.size})",
                                        color = colors.accentCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = AppDesign.textBody
                                    )
                                    Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                                    Icon(
                                        if (isPendulumsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        null,
                                        tint = colors.accentCyan,
                                        modifier = Modifier.size(AppDesign.iconSmall)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
