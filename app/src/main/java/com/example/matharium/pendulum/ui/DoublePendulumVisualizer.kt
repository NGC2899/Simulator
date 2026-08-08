package com.example.matharium.pendulum.ui

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
import com.example.matharium.fourier.ui.DisplayModeButton
import com.example.matharium.fourier.ui.SidebarActionButton
import com.example.matharium.pendulum.engine.*
import com.example.matharium.pendulum.state.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun DoublePendulumVisualizer(
    colors: AppColors,
    pendulums: SnapshotStateList<PendulumInstance>,
    running: Boolean,
    onHasStartedChange: (Boolean) -> Unit,
    displayMode: com.example.matharium.pendulum.state.PendulumDisplayMode,
    onDisplayModeChange: (com.example.matharium.pendulum.state.PendulumDisplayMode) -> Unit,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    prefs: AppPreferences
) {
    var draggingPendulumId by remember { mutableStateOf<Int?>(null) }
    var draggingBobType by remember { mutableStateOf(com.example.matharium.pendulum.state.PendulumDragTarget.NONE) }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val vectorLineScalePx = with(density) { DoublePendulumConstants.VECTOR_LINE_SCALE.dp.toPx() }

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

                            if (displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.SIMULATION || displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX) {
                                for (p in pendulums.asReversed()) {
                                    if ((touch - p.bob1 * scale).getDistance() < AppDesign.sidebarButtonSize.toPx() * DoublePendulumConstants.COLOR_SATURATION_ALT) {
                                        draggingPendulumId = p.id; draggingBobType =
                                            com.example.matharium.pendulum.state.PendulumDragTarget.BOB1; onHasStartedChange(false); return@detectDragGestures
                                    }
                                    if ((touch - p.bob2 * scale).getDistance() < AppDesign.sidebarButtonSize.toPx() * DoublePendulumConstants.COLOR_SATURATION_ALT) {
                                        draggingPendulumId = p.id; draggingBobType =
                                            com.example.matharium.pendulum.state.PendulumDragTarget.BOB2; onHasStartedChange(false); return@detectDragGestures
                                    }
                                }
                            } else if (displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.GRAPH) {
                                val graphScale = scale * DoublePendulumConstants.GRAPH_RENDER_SCALE
                                for (p in pendulums.asReversed()) {
                                    val currentPos = Offset(
                                        p.t1.toFloatOrNull() ?: 0f,
                                        p.t2.toFloatOrNull() ?: 0f
                                    )
                                    if ((touch - currentPos * graphScale).getDistance() < AppDesign.sidebarButtonSize.toPx() * DoublePendulumConstants.COLOR_SATURATION_ALT) {
                                        draggingPendulumId = p.id
                                        draggingBobType = com.example.matharium.pendulum.state.PendulumDragTarget.ANGLE_DOT
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

                            if (displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.SIMULATION || displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX) {
                                val touchScaled = touch / scale
                                if (draggingBobType == com.example.matharium.pendulum.state.PendulumDragTarget.BOB1) {
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
                            } else if (displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.GRAPH) {
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
                            draggingPendulumId = null; draggingBobType = com.example.matharium.pendulum.state.PendulumDragTarget.NONE
                            prefs.savePendulums(pendulums.toList())
                        },
                        onDragCancel = {
                            draggingPendulumId = null; draggingBobType = com.example.matharium.pendulum.state.PendulumDragTarget.NONE
                        }
                    )
                }
        ) {
            when (displayMode) {
                com.example.matharium.pendulum.state.PendulumDisplayMode.SIMULATION, com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX -> {
                    translate(size.width / 2f, size.height / 2f) {
                        pendulums.forEach { p ->
                            if (displayMode != com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX) {
                                drawChaosTrail(
                                    p.trailX, p.trailY, p.trailCount, p.trailPointer, p.trailSize,
                                    p.currentColor, scale
                                )
                            }
                            drawLine(
                                p.currentColor,
                                Offset.Zero,
                                p.bob1 * scale,
                                AppDesign.strokeThick.toPx(),
                                StrokeCap.Round
                            )
                            drawLine(
                                p.currentColor,
                                p.bob1 * scale,
                                p.bob2 * scale,
                                AppDesign.strokeThick.toPx(),
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

                            if (displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX) {
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
                                    ) * vectorLineScalePx,
                                    AppDesign.strokeStandard.toPx(),
                                    StrokeCap.Round
                                )
                                drawLine(
                                    p.currentColor.copy(alpha = AppDesign.opacityGlassBorder),
                                    p.bob2 * scale,
                                    p.bob2 * scale + Offset(
                                        v2x.toFloat(),
                                        v2y.toFloat()
                                    ) * vectorLineScalePx,
                                    AppDesign.strokeStandard.toPx(),
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

                com.example.matharium.pendulum.state.PendulumDisplayMode.GRAPH -> {
                    translate(size.width / 2f, size.height / 2f) {
                        // Draw Grid/Axes
                        val axisColor = colors.textSecondary.copy(AppDesign.opacityMedium)
                        drawLine(
                            axisColor,
                            Offset(-size.width / 2, 0f),
                            Offset(size.width / 2, 0f),
                            AppDesign.strokeThin.toPx()
                        )
                        drawLine(
                            axisColor,
                            Offset(0f, -size.height / 2),
                            Offset(0f, size.height / 2),
                            AppDesign.strokeThin.toPx()
                        )

                        val graphScale = scale * DoublePendulumConstants.GRAPH_RENDER_SCALE
                        pendulums.forEach { p ->
                            if (p.angleTrailCount > 1) {
                                val path = Path()
                                val startIdx = (p.angleTrailPointer - p.angleTrailCount + p.angleTrailSize) % p.angleTrailSize
                                
                                val firstPointX = p.trailAngleX[startIdx] * graphScale
                                val firstPointY = p.trailAngleY[startIdx] * graphScale
                                path.moveTo(firstPointX, firstPointY)
                                
                                val step = 2 // Optimized step
                                for (i in step until p.angleTrailCount step step) {
                                    val idx = (startIdx + i) % p.angleTrailSize
                                    path.lineTo(p.trailAngleX[idx] * graphScale, p.trailAngleY[idx] * graphScale)
                                }
                                
                                drawPath(
                                    path = path,
                                    color = p.currentColor.copy(AppDesign.opacityMedium),
                                    style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // Current position dot
                            val currentIdx = (p.angleTrailPointer - 1 + p.angleTrailSize) % p.angleTrailSize
                            val currentT1 = if (running && p.angleTrailCount > 0) p.trailAngleX[currentIdx] else (p.t1.toFloatOrNull() ?: 0f)
                            val currentT2 = if (running && p.angleTrailCount > 0) p.trailAngleY[currentIdx] else (p.t2.toFloatOrNull() ?: 0f)
                            
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
                imageVector = FluentIcons.FluentuiSystemIconsDataLine,
                selected = displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.SIMULATION,
                colors = colors
            ) { onDisplayModeChange(com.example.matharium.pendulum.state.PendulumDisplayMode.SIMULATION) }

            DisplayModeButton(
                imageVector = FluentIcons.FluentuiSystemIconsSineWaveDots,
                selected = displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.GRAPH,
                colors = colors
            ) { onDisplayModeChange(com.example.matharium.pendulum.state.PendulumDisplayMode.GRAPH) }

            DisplayModeButton(
                imageVector = FluentIcons.TablerBrandSpeedtest,
                selected = displayMode == com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX,
                colors = colors
            ) { onDisplayModeChange(com.example.matharium.pendulum.state.PendulumDisplayMode.COMPLEX) }

            Spacer(Modifier.height(AppDesign.spacingSmall))

            // Clear Trails Button
            SidebarActionButton(
                imageVector = FluentIcons.TablerClearAll,
                colors = colors,
                onClick = { pendulums.forEach { it.clearTrails() } }
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
                    IconButton(
                        onClick = { if (scale < DoublePendulumConstants.ZOOM_RANGE_END) onScaleChange(scale + DoublePendulumConstants.ZOOM_DISPLAY_DIVISOR) },
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_outline),
                            null,
                            tint = colors.accentCyan,
                        )
                    }

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

                    IconButton(
                        onClick = { if (scale > DoublePendulumConstants.ZOOM_RANGE_START) onScaleChange(scale - DoublePendulumConstants.ZOOM_DISPLAY_DIVISOR) },
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            null,
                            tint = colors.accentCyan,
                        )
                    }

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
