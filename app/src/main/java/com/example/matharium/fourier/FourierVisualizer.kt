package com.example.matharium.fourier

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.R
import com.example.matharium.app.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FourierVisualizerBox(
    displayMode: FourierDisplayMode,
    onDisplayModeChange: (FourierDisplayMode) -> Unit,
    waveType: WaveType,
    nTerms: Int,
    onNTermsChange: (Int) -> Unit,
    time: Float,
    path: MutableList<Offset>,
    onClearPath: () -> Unit,
    windingFrequency: Float,
    customCoefficients: List<Pair<Float, Float>>,
    customCoefficients2D: List<FourierLogic.ComplexCoeff>,
    formulaCoefficients: List<Pair<Float, Float>> = emptyList(),
    svgCoefficients: List<FourierLogic.ComplexCoeff> = emptyList(),
    customFunctionSignals: List<SignalInstance>,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.canvasHeightSmall)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = 0.45f))
            .border(
                1.dp,
                colors.cardBorder.copy(alpha = 0.6f),
                RoundedCornerShape(AppDesign.radiusCard)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width * 0.3f
            val centerY = size.height * 0.5f
            val radiusBase = 100f

            if ((displayMode == FourierDisplayMode.CIRCULAR) || (displayMode == FourierDisplayMode.COMPLEX)) {
                val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else centerX
                translate(actualCenterX, centerY) {
                    // Draw Grid
                    val gridColor = colors.accentCyan.copy(alpha = AppDesign.opacityGrid)
                    val step = 60f
                    val left = -actualCenterX
                    val right = size.width - actualCenterX
                    val top = -centerY
                    val bottom = size.height - centerY

                    var gx = 0f
                    while (gx <= right) {
                        drawLine(gridColor, Offset(gx, top), Offset(gx, bottom), AppDesign.strokeThin)
                        gx += step
                    }
                    gx = -step
                    while (gx >= left) {
                        drawLine(gridColor, Offset(gx, top), Offset(gx, bottom), AppDesign.strokeThin)
                        gx -= step
                    }
                    var gy = 0f
                    while (gy <= bottom) {
                        drawLine(gridColor, Offset(left, gy), Offset(right, gy), AppDesign.strokeThin)
                        gy += step
                    }
                    gy = -step
                    while (gy >= top) {
                        drawLine(gridColor, Offset(left, gy), Offset(right, gy), AppDesign.strokeThin)
                        gy -= step
                    }

                    // Main Axes
                    val axisColor = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle)
                    drawLine(axisColor, Offset(left, 0f), Offset(right, 0f), AppDesign.strokeThin)
                    drawLine(axisColor, Offset(0f, top), Offset(0f, bottom), AppDesign.strokeThin)

                    var x = 0f
                    var y = 0f

                    val termsToDraw = if (waveType == WaveType.CUSTOM_FUNCTION) {
                        nTerms.coerceAtMost(customFunctionSignals.size)
                    } else {
                        nTerms.coerceAtMost(15) // Reduced from 25 for better performance
                    }

                    for (i in 0 until termsToDraw) {
                        val prevX = x
                        val prevY = y

                        val n = when (waveType) {
                            WaveType.SINE -> 1f
                            WaveType.SQUARE -> (i * 2 + 1).toFloat()
                            WaveType.SAWTOOTH -> (i + 1).toFloat()
                            WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                            WaveType.MY_SIGNAL -> i.toFloat()
                            WaveType.FORMULA -> i.toFloat()
                            WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].freq.toFloat() else 0f
                            WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].freq.toFloat() else 0f
                            WaveType.CUSTOM_FUNCTION -> if (i < customFunctionSignals.size) customFunctionSignals[i].freq.toFloatOrNull() ?: 0f else 0f
                        }

                        val radius = when (waveType) {
                            WaveType.SINE -> if (i == 0) -radiusBase else 0f
                            WaveType.SQUARE -> -radiusBase * (4f / (n * PI.toFloat()))
                            WaveType.SAWTOOTH -> -radiusBase * (2f / (n * PI.toFloat()))
                            WaveType.TRIANGLE -> -radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat()))
                            WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first else 0f
                            WaveType.FORMULA -> if (i < formulaCoefficients.size) formulaCoefficients[i].first else 0f
                            WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].amp else 0f
                            WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].amp else 0f
                            WaveType.CUSTOM_FUNCTION -> if (i < customFunctionSignals.size) customFunctionSignals[i].amp.toFloatOrNull() ?: 0f else 0f
                        }

                        if (kotlin.math.abs(radius) < 0.5f && i > 0) continue

                        val phase = when (waveType) {
                            WaveType.SINE -> 0f
                            WaveType.SQUARE -> 0f
                            WaveType.SAWTOOTH -> 0f
                            WaveType.TRIANGLE -> if (i % 2 != 0) PI.toFloat() else 0f
                            WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].second else 0f
                            WaveType.FORMULA -> if (i < formulaCoefficients.size) formulaCoefficients[i].second else 0f
                            WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].phase else 0f
                            WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].phase else 0f
                            WaveType.CUSTOM_FUNCTION -> 0f
                        }
                        val angle = 2 * PI.toFloat() * n * time + phase
                        x += radius * cos(angle)
                        y += radius * sin(angle)

                        drawCircle(
                            color = colors.accentCyan.copy(alpha = AppDesign.opacityLow * 2f),
                            radius = kotlin.math.abs(radius),
                            center = Offset(prevX, prevY),
                            style = Stroke(width = AppDesign.strokeThin)
                        )

                        drawLine(
                            color = colors.accentCyan.copy(alpha = AppDesign.opacityMedium),
                            start = Offset(prevX, prevY),
                            end = Offset(x, y),
                            strokeWidth = 1.5f
                        )
                    }

                    drawCircle(colors.accentViolet, AppDesign.spacingExtraSmall.toPx(), Offset(x, y))

                    if (displayMode == FourierDisplayMode.CIRCULAR) {
                        drawLine(
                            color = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle),
                            start = Offset(x, y),
                            end = Offset(180f, y),
                            strokeWidth = AppDesign.strokeThin
                        )

                        val wavePath = Path()
                        if (path.isNotEmpty()) {
                            wavePath.moveTo(180f, path[0].y)
                            // Performance optimization: higher step for drawing the wave
                            for (i in 1 until path.size step 4) {
                                wavePath.lineTo(180f + i * 0.8f, path[i].y)
                            }
                        }

                        drawPath(
                            path = wavePath,
                            color = colors.accentCyan,
                            style = Stroke(width = AppDesign.strokeThick, cap = StrokeCap.Round)
                        )
                    } else {
                        val tracePath = Path()
                        if (path.isNotEmpty()) {
                            tracePath.moveTo(path[0].x, path[0].y)
                            // Performance optimization: higher step for 2D tracing
                            for (i in 1 until path.size step 2) {
                                tracePath.lineTo(path[i].x, path[i].y)
                            }
                        }
                        drawPath(
                            path = tracePath,
                            color = colors.accentCyan,
                            style = Stroke(width = AppDesign.strokeStandard, cap = StrokeCap.Round)
                        )
                    }
                }
            } else if (displayMode == FourierDisplayMode.WRAPPING) {
                translate(size.width / 2f, centerY) {
                    val gridColor = colors.accentCyan.copy(alpha = AppDesign.opacityGrid)
                    val step = 60f

                    val halfWidth = size.width / 2f
                    val halfHeight = size.height / 2f

                    // Draw Grid Lines
                    var gx = 0f
                    while (gx <= halfWidth) {
                        drawLine(gridColor, Offset(gx, -halfHeight), Offset(gx, halfHeight), AppDesign.strokeThin)
                        if (gx > 0) drawLine(gridColor, Offset(-gx, -halfHeight), Offset(-gx, halfHeight), AppDesign.strokeThin)
                        gx += step
                    }
                    var gy = 0f
                    while (gy <= halfHeight) {
                        drawLine(gridColor, Offset(-halfWidth, gy), Offset(halfWidth, gy), AppDesign.strokeThin)
                        if (gy > 0) drawLine(gridColor, Offset(-halfWidth, -gy), Offset(halfWidth, -gy), AppDesign.strokeThin)
                        gy += step
                    }

                    // Main Axes
                    val axisColor = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle)
                    drawLine(axisColor, Offset(-halfWidth, 0f), Offset(halfWidth, 0f), AppDesign.strokeThin)
                    drawLine(axisColor, Offset(0f, -halfHeight), Offset(0f, halfHeight), AppDesign.strokeThin)

                    val wrappedPath = Path()
                    var sumX = 0f
                    var sumY = 0f
                    val baseRadius = 80f

                    if (path.isNotEmpty()) {
                        var processedCount = 0
                        // Use step 2 to match physics substeps, ensuring sampling consistency
                        // and eliminating the "orbital jitter" caused by index shifting.
                        for (i in path.indices step 2) {
                            val point = path[i]
                            val t = point.x
                            val amplitude = point.y + baseRadius
                            val angle = -2 * PI.toFloat() * windingFrequency * t

                            val wx = amplitude * cos(angle)
                            val wy = amplitude * sin(angle)

                            if (i == 0) wrappedPath.moveTo(wx, wy)
                            else wrappedPath.lineTo(wx, wy)

                            sumX += wx
                            sumY += wy
                            processedCount++
                        }

                        drawPath(
                            path = wrappedPath,
                            color = colors.accentCyan.copy(alpha = AppDesign.opacityTrace),
                            style = Stroke(width = AppDesign.strokeStandard, cap = StrokeCap.Round)
                        )

                        val avgX = if (processedCount > 0) sumX / processedCount else 0f
                        val avgY = if (processedCount > 0) sumY / processedCount else 0f
                        drawCircle(colors.accentHell, AppDesign.spacingExtraSmall.toPx() + 1f, Offset(avgX, avgY))

                        drawLine(
                            colors.textSecondary.copy(alpha = AppDesign.opacityMedium),
                            Offset.Zero,
                            Offset(avgX, avgY),
                            AppDesign.strokeThin
                        )
                    }
                }
            }
        }

        // Sidebar Navigation
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = AppDesign.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall + AppDesign.spacingExtraSmall / 2f)
        ) {
            DisplayModeButton(
                icon = Icons.Default.Timeline,
                selected = displayMode == FourierDisplayMode.CIRCULAR,
                colors = colors
            ) {
                if (displayMode != FourierDisplayMode.CIRCULAR) onClearPath()
                onDisplayModeChange(FourierDisplayMode.CIRCULAR)
            }

            DisplayModeButton(
                icon = Icons.Default.Adjust,
                selected = displayMode == FourierDisplayMode.WRAPPING,
                colors = colors
            ) {
                if (displayMode != FourierDisplayMode.WRAPPING) onClearPath()
                onDisplayModeChange(FourierDisplayMode.WRAPPING)
            }

            DisplayModeButton(
                icon = Icons.Default.Hub,
                selected = displayMode == FourierDisplayMode.COMPLEX,
                colors = colors
            ) {
                if (displayMode != FourierDisplayMode.COMPLEX) onClearPath()
                onDisplayModeChange(FourierDisplayMode.COMPLEX)
            }

            Spacer(Modifier.height(AppDesign.spacingSmall + AppDesign.spacingExtraSmall / 2f))

            SidebarActionButton(
                icon = painterResource(id = R.drawable.trash_line_outline),
                colors = colors,
                onClick = { onClearPath() }
            )
        }

        // Terms Handler (Right Sidebar)
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
                        colors.cardSurface.copy(alpha = AppDesign.opacityMedium),
                        RoundedCornerShape(AppDesign.radiusCard)
                    )
                    .border(
                        AppDesign.borderThin,
                        colors.cardBorder.copy(alpha = AppDesign.opacityLow * 2f),
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
                        value = nTerms.toFloat(),
                        onValueChange = { onNTermsChange(it.toInt()) },
                        valueRange = 1f..50f,
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
                            inactiveTrackColor = colors.fieldBorder.copy(alpha = AppDesign.opacityLow * 2f)
                        )
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.remove),
                        null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    )

                    Text(
                        text = nTerms.toString(),
                        color = colors.textPrimary,
                        fontSize = AppDesign.textCaption,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
