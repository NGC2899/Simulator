package com.example.matharium.pendulum

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun DoublePendulumVisualizer(
    colors: AppColors,
    pendulums: SnapshotStateList<PendulumInstance>,
    running: Boolean,
    onHasStartedChange: (Boolean) -> Unit,
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    prefs: AppPreferences
) {
    var draggingPendulumId by remember { mutableStateOf<Int?>(null) }
    var draggingBobType by remember { mutableStateOf(DragTarget.NONE) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.canvasHeightSmall)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = AppDesign.opacityGlass))
            .border(
                AppDesign.borderThin,
                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                RoundedCornerShape(AppDesign.radiusCard)
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(displayMode, running, scale, pendulums.size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (running) return@detectDragGestures
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = offset - center

                            if (displayMode == DisplayMode.SIMULATION || displayMode == DisplayMode.COMPLEX) {
                                for (p in pendulums.asReversed()) {
                                    if ((touch - p.bob1 * scale).getDistance() < AppDesign.sidebarButtonSize.toPx() * DoublePendulumConstants.COLOR_SATURATION_ALT) {
                                        draggingPendulumId = p.id; draggingBobType =
                                            DragTarget.BOB1; onHasStartedChange(false); return@detectDragGestures
                                    }
                                    if ((touch - p.bob2 * scale).getDistance() < AppDesign.sidebarButtonSize.toPx() * DoublePendulumConstants.COLOR_SATURATION_ALT) {
                                        draggingPendulumId = p.id; draggingBobType =
                                            DragTarget.BOB2; onHasStartedChange(false); return@detectDragGestures
                                    }
                                }
                            } else if (displayMode == DisplayMode.GRAPH) {
                                val graphScale = scale * DoublePendulumConstants.GRAPH_RENDER_SCALE
                                for (p in pendulums.asReversed()) {
                                    val currentPos = Offset(
                                        p.t1.toFloatOrNull() ?: 0f,
                                        p.t2.toFloatOrNull() ?: 0f
                                    )
                                    if ((touch - currentPos * graphScale).getDistance() < AppDesign.sidebarButtonSize.toPx() * DoublePendulumConstants.COLOR_SATURATION_ALT) {
                                        draggingPendulumId = p.id
                                        draggingBobType = DragTarget.ANGLE_DOT
                                        onHasStartedChange(false)
                                        return@detectDragGestures
                                    }
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (running || draggingPendulumId == null) return@detectDragGestures
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = (change.position - center)
                            val p = pendulums.find { it.id == draggingPendulumId }
                                ?: return@detectDragGestures

                            if (displayMode == DisplayMode.SIMULATION || displayMode == DisplayMode.COMPLEX) {
                                val touchScaled = touch / scale
                                if (draggingBobType == DragTarget.BOB1) {
                                    p.t1 = String.format(
                                        Locale.US,
                                        "%.1f",
                                        atan2(
                                            touchScaled.x.toDouble(),
                                            touchScaled.y.toDouble()
                                        ) * 180.0 / PI
                                    )
                                } else {
                                    val rel = touchScaled - p.bob1
                                    p.t2 = String.format(
                                        Locale.US,
                                        "%.1f",
                                        atan2(rel.x.toDouble(), rel.y.toDouble()) * 180.0 / PI
                                    )
                                }
                                p.updatePositions()
                            } else if (displayMode == DisplayMode.GRAPH) {
                                val graphScale = scale * DoublePendulumConstants.GRAPH_RENDER_SCALE
                                val touchScaled = touch / graphScale
                                p.t1 =
                                    String.format(Locale.US, "%.1f", touchScaled.x.toDouble())
                                p.t2 =
                                    String.format(Locale.US, "%.1f", touchScaled.y.toDouble())
                                p.updatePositions()
                            }
                        },
                        onDragEnd = {
                            draggingPendulumId = null; draggingBobType = DragTarget.NONE
                            prefs.savePendulums(pendulums.toList())
                        },
                        onDragCancel = {
                            draggingPendulumId = null; draggingBobType = DragTarget.NONE
                        }
                    )
                }
        ) {
            when (displayMode) {
                DisplayMode.SIMULATION, DisplayMode.COMPLEX -> {
                    translate(size.width / 2f, size.height / 2f) {
                        pendulums.forEach { p ->
                            if (displayMode != DisplayMode.COMPLEX) {
                                drawChaosTrail(p.trail, p.currentColor, scale)
                            }
                            drawLine(
                                p.currentColor,
                                Offset.Zero,
                                p.bob1 * scale,
                                AppDesign.strokeThick,
                                StrokeCap.Round
                            )
                            drawLine(
                                p.currentColor,
                                p.bob1 * scale,
                                p.bob2 * scale,
                                AppDesign.strokeThick,
                                StrokeCap.Round
                            )
                            drawCircle(
                                p.currentColor,
                                AppDesign.radiusSmall.toPx() * DoublePendulumConstants.BOB_RADIUS_SCALE,
                                p.bob1 * scale
                            )
                            drawCircle(
                                p.currentColor,
                                AppDesign.radiusSmall.toPx(),
                                p.bob2 * scale
                            )

                            if (displayMode == DisplayMode.COMPLEX) {
                                // Draw velocity vectors
                                val v1x =
                                    p.logic.lengthOne * p.logic.omegaOne * kotlin.math.cos(p.logic.thetaOne)
                                val v1y =
                                    -p.logic.lengthOne * p.logic.omegaOne * kotlin.math.sin(p.logic.thetaOne)
                                val v2x =
                                    v1x + p.logic.lengthTwo * p.logic.omegaTwo * kotlin.math.cos(
                                        p.logic.thetaTwo
                                    )
                                val v2y =
                                    v1y - p.logic.lengthTwo * p.logic.omegaTwo * kotlin.math.sin(
                                        p.logic.thetaTwo
                                    )

                                drawLine(
                                    p.currentColor.copy(alpha = AppDesign.opacityGlassBorder),
                                    p.bob1 * scale,
                                    p.bob1 * scale + Offset(
                                        v1x.toFloat(),
                                        v1y.toFloat()
                                    ) * DoublePendulumConstants.VECTOR_LINE_SCALE,
                                    AppDesign.strokeStandard,
                                    StrokeCap.Round
                                )
                                drawLine(
                                    p.currentColor.copy(alpha = AppDesign.opacityGlassBorder),
                                    p.bob2 * scale,
                                    p.bob2 * scale + Offset(
                                        v2x.toFloat(),
                                        v2y.toFloat()
                                    ) * DoublePendulumConstants.VECTOR_LINE_SCALE,
                                    AppDesign.strokeStandard,
                                    StrokeCap.Round
                                )
                            }
                        }
                        drawCircle(
                            colors.pivot,
                            AppDesign.spacingExtraSmall.toPx(),
                            Offset.Zero
                        )
                    }
                }

                DisplayMode.GRAPH -> {
                    translate(size.width / 2f, size.height / 2f) {
                        // Draw Grid/Axes
                        val axisColor = colors.textSecondary.copy(AppDesign.opacityMedium)
                        drawLine(
                            axisColor,
                            Offset(-size.width / 2, 0f),
                            Offset(size.width / 2, 0f),
                            AppDesign.strokeThin
                        )
                        drawLine(
                            axisColor,
                            Offset(0f, -size.height / 2),
                            Offset(0f, size.height / 2),
                            AppDesign.strokeThin
                        )

                        val graphScale = scale * DoublePendulumConstants.GRAPH_RENDER_SCALE
                        pendulums.forEach { p ->
                            if (p.angleTrail.size > 1) {
                                // Optimization: Use a single Path instead of thousands of drawLine calls
                                val path = Path().apply {
                                    val start = p.angleTrail.first() * graphScale
                                    moveTo(start.x, start.y)
                                    // Step optimization for high density trails
                                    val step = if (p.angleTrail.size > 200) 2 else 1
                                    for (i in step until p.angleTrail.size step step) {
                                        val point = p.angleTrail[i] * graphScale
                                        lineTo(point.x, point.y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = p.currentColor.copy(AppDesign.opacityMedium),
                                    style = Stroke(width = AppDesign.strokeStandard, cap = StrokeCap.Round)
                                )
                            }

                            // Current position dot
                            val currentAngle = p.angleTrail.lastOrNull()
                            val currentT1 =
                                if (running) currentAngle?.x ?: (p.t1.toFloatOrNull()
                                    ?: 0f) else (p.t1.toFloatOrNull() ?: 0f)
                            val currentT2 =
                                if (running) currentAngle?.y ?: (p.t2.toFloatOrNull()
                                    ?: 0f) else (p.t2.toFloatOrNull() ?: 0f)
                            drawCircle(
                                p.currentColor,
                                AppDesign.radiusSmall.toPx() * DoublePendulumConstants.BOB_RADIUS_SCALE,
                                Offset(currentT1, currentT2) * graphScale
                            )
                        }
                    }
                }
            }
        }

        // ── Sidebar Navigation ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = AppDesign.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium)
        ) {
            DisplayModeButton(
                icon = Icons.Default.Timeline,
                selected = displayMode == DisplayMode.SIMULATION,
                colors = colors
            ) { onDisplayModeChange(DisplayMode.SIMULATION) }

            DisplayModeButton(
                icon = Icons.AutoMirrored.Filled.ShowChart,
                selected = displayMode == DisplayMode.GRAPH,
                colors = colors
            ) { onDisplayModeChange(DisplayMode.GRAPH) }

            DisplayModeButton(
                icon = Icons.Default.AllInclusive,
                selected = displayMode == DisplayMode.COMPLEX,
                colors = colors
            ) { onDisplayModeChange(DisplayMode.COMPLEX) }

            Spacer(Modifier.height(AppDesign.spacingSmall))

            // Clear Trails Button
            SidebarActionButton(
                icon = painterResource(id = R.drawable.trash_line_outline),
                colors = colors,
                onClick = { pendulums.forEach { it.trail.clear(); it.angleTrail.clear() } }
            )
        }

        // ── Zoom Handler (Right Sidebar) ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = AppDesign.spacingMedium)
                .width(AppDesign.sidebarButtonSize)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .height(AppDesign.termsBoxHeight)
                    .background(
                        colors.cardSurface.copy(AppDesign.opacityMedium),
                        RoundedCornerShape(AppDesign.radiusCard)
                    )
                    .border(
                        1.dp,
                        colors.cardBorder.copy(AppDesign.opacityMedium),
                        RoundedCornerShape(AppDesign.radiusCard)
                    )
                    .padding(vertical = AppDesign.spacingMedium),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_outline),
                        null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    )

                    Slider(
                        value = scale,
                        onValueChange = { onScaleChange(it) },
                        valueRange = DoublePendulumConstants.ZOOM_RANGE_START..DoublePendulumConstants.ZOOM_RANGE_END,
                        modifier = Modifier
                            .weight(1f)
                            .layout { measurable: Measurable, constraints ->
                                val placeable = measurable.measure(
                                    constraints.copy(
                                        minWidth = constraints.minHeight,
                                        maxWidth = constraints.maxHeight,
                                        minHeight = constraints.minWidth,
                                        maxHeight = constraints.maxWidth
                                    )
                                )
                                layout(placeable.height, placeable.width) {
                                    placeable.placeWithLayer(
                                        x = -(placeable.width - placeable.height) / 2,
                                        y = (placeable.width - placeable.height) / 2
                                    ) {
                                        rotationZ = -90f
                                    }
                                }
                            },
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accentCyan,
                            activeTrackColor = colors.accentCyan,
                            inactiveTrackColor = colors.fieldBorder.copy(AppDesign.opacityMedium)
                        )
                    )

                    Icon(
                        Icons.Default.Remove,
                        null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    )

                    Text(
                        "${(scale / DoublePendulumConstants.ZOOM_DISPLAY_DIVISOR).toInt()}%",
                        color = colors.textPrimary,
                        fontSize = AppDesign.textCaption,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = AppDesign.spacingTiny)
                    )
                }
            }
        }
    }
}
