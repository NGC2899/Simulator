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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp as lerpColor
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
    path: List<PathPoint>,
    showErrorGradient: Boolean,
    errorSensitivity: Float,
    waveStretch: Float,
    onClearPath: () -> Unit,
    windingFrequency: Float,
    customCoefficients: List<Pair<Float, Float>>,
    customCoefficients2D: List<FourierLogic.ComplexCoeff>,
    formulaCoefficients: List<Pair<Float, Float>> = emptyList(),
    svgCoefficients: List<FourierLogic.ComplexCoeff> = emptyList(),
    customFunctionSignals: List<SignalInstance>,
    colors: AppColors,
    pausedHarmonics: Map<Int, Boolean> = emptyMap(),
    removedHarmonics: Map<Int, Boolean> = emptyMap(),
    harmonicFrequencies: Map<Int, Float> = emptyMap(),
    harmonicAmplitudes: Map<Int, Float> = emptyMap(),
    harmonicPhases: Map<Int, Float> = emptyMap()
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusBasePx = with(density) { AppDesign.unitCircleRadius.toPx() }
    val unitScale = radiusBasePx
    val gridStepPx = unitScale / 2f
    val pixelsPerTimeUnit = with(density) { waveStretch.dp.toPx() }
    
    // Density-aware constants for drawing
    val labelOffsetX = with(density) { 25.dp.toPx() }
    val labelOffsetY = with(density) { AppDesign.spacingSmall.toPx() }
    val labelOffsetAxis = with(density) { AppDesign.spacingSmall.toPx() }
    val labelOffsetWrappingX = with(density) { AppDesign.phasorRadiusBase.toPx() }
    val waveStartX = with(density) { 180.dp.toPx() }
    val indicatorSize = with(density) { AppDesign.spacingExtraSmall.toPx() }

    // Perfect resolution for flagship devices (pathStep = 1)
    val pathStep = 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.canvasHeightSmall)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = 0.45f))
            .border(
                AppDesign.borderThin,
                colors.cardBorder.copy(alpha = 0.6f),
                RoundedCornerShape(AppDesign.radiusCard)
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val centerX = size.width * 0.20f
                    val centerY = size.height * 0.5f
                    val gridColor = colors.accentCyan.copy(alpha = AppDesign.opacityGrid)
                    val axisColor = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle)
                    val labelColor = colors.textSecondary.copy(alpha = AppDesign.opacityMedium).toArgb()
                    
                    val gridPath = Path()
                    val axisPath = Path()
                    
                    val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else centerX
                    val paint = android.graphics.Paint().apply {
                        color = labelColor
                        textSize = AppDesign.textCaption.toPx()
                        isAntiAlias = true
                    }

                    if (displayMode != FourierDisplayMode.WRAPPING) {
                        val step = gridStepPx
                        val left = -actualCenterX
                        val right = size.width - actualCenterX
                        val top = -centerY
                        val bottom = size.height - centerY

                        var gx = 0f
                        while (gx <= right) {
                            gridPath.moveTo(gx, top); gridPath.lineTo(gx, bottom); gx += step
                        }
                        gx = -step
                        while (gx >= left) {
                            gridPath.moveTo(gx, top); gridPath.lineTo(gx, bottom); gx -= step
                        }
                        var gy = 0f
                        while (gy <= bottom) {
                            gridPath.moveTo(left, gy); gridPath.lineTo(right, gy); gy += step
                        }
                        gy = -step
                        while (gy >= top) {
                            gridPath.moveTo(left, gy); gridPath.lineTo(right, gy); gy -= step
                        }
                        
                        axisPath.moveTo(left, 0f); axisPath.lineTo(right, 0f)
                        axisPath.moveTo(0f, top); axisPath.lineTo(0f, bottom)
                    } else {
                        val step = gridStepPx
                        val halfWidth = size.width / 2f
                        val halfHeight = size.height / 2f
                        var gx = 0f
                        while (gx <= halfWidth) {
                            gridPath.moveTo(gx, -halfHeight); gridPath.lineTo(gx, halfHeight)
                            if (gx > 0) { gridPath.moveTo(-gx, -halfHeight); gridPath.lineTo(-gx, halfHeight) }
                            gx += step
                        }
                        var gy = 0f
                        while (gy <= halfHeight) {
                            gridPath.moveTo(-halfWidth, gy); gridPath.lineTo(halfWidth, gy)
                            if (gy > 0) { gridPath.moveTo(-halfWidth, -gy); gridPath.lineTo(-halfWidth, -gy) }
                            gy += step
                        }
                        axisPath.moveTo(-halfWidth, 0f); axisPath.lineTo(halfWidth, 0f)
                        axisPath.moveTo(0f, -halfHeight); axisPath.lineTo(0f, halfHeight)
                    }

                    onDrawBehind {
                        if (displayMode != FourierDisplayMode.WRAPPING) {
                            translate(actualCenterX, centerY) {
                                drawPath(gridPath, gridColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                drawPath(axisPath, axisColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                
                                // Optimized Label Drawing (only draw every 2nd step to save performance)
                                val step = gridStepPx * 2
                                val left = -actualCenterX
                                val right = size.width - actualCenterX
                                val top = -centerY
                                val bottom = size.height - centerY

                                paint.textAlign = android.graphics.Paint.Align.CENTER
                                var lx = step
                                while (lx <= right) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetX, paint) }
                                    lx += step
                                }
                                lx = -step
                                while (lx >= left) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetX, paint) }
                                    lx -= step
                                }

                                paint.textAlign = android.graphics.Paint.Align.RIGHT
                                var ly = step
                                while (ly <= bottom) {
                                    val labelText = if (displayMode == FourierDisplayMode.COMPLEX) String.format(java.util.Locale.US, "%.1fi", -ly / unitScale) else String.format(java.util.Locale.US, "%.1f", -ly / unitScale)
                                    drawIntoCanvas { it.nativeCanvas.drawText(labelText, -labelOffsetAxis, ly + labelOffsetY, paint) }
                                    ly += step
                                }
                                ly = -step
                                while (ly >= top) {
                                    val labelText = if (displayMode == FourierDisplayMode.COMPLEX) String.format(java.util.Locale.US, "%.1fi", -ly / unitScale) else String.format(java.util.Locale.US, "%.1f", -ly / unitScale)
                                    drawIntoCanvas { it.nativeCanvas.drawText(labelText, -labelOffsetAxis, ly + labelOffsetY, paint) }
                                    ly -= step
                                }
                            }
                        } else {
                            translate(size.width / 2f, centerY) {
                                drawPath(gridPath, gridColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                drawPath(axisPath, axisColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                
                                val halfWidth = size.width / 2f
                                val halfHeight = size.height / 2f
                                val step = gridStepPx * 2 // Optimized step
                                
                                paint.textAlign = android.graphics.Paint.Align.CENTER
                                var lx = step
                                while (lx <= halfWidth) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetWrappingX, paint) }
                                    lx += step
                                }
                                lx = -step
                                while (lx >= -halfWidth) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetWrappingX, paint) }
                                    lx -= step
                                }

                                paint.textAlign = android.graphics.Paint.Align.RIGHT
                                var ly = step
                                while (ly <= halfHeight) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", -ly / unitScale), -labelOffsetAxis, ly + labelOffsetY, paint) }
                                    ly += step
                                }
                                ly = -step
                                while (ly >= -halfHeight) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", -ly / unitScale), -labelOffsetAxis, ly + labelOffsetY, paint) }
                                    ly -= step
                                }
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width * 0.20f
            val centerY = size.height * 0.5f

            if ((displayMode == FourierDisplayMode.CIRCULAR) || (displayMode == FourierDisplayMode.COMPLEX)) {
                val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else centerX
                translate(actualCenterX, centerY) {

                    var x = 0f
                    var y = 0f

                    val termsToDraw = when (waveType) {
                        WaveType.PURE_SIGNAL -> nTerms.coerceAtMost(customFunctionSignals.size)
                        WaveType.MY_SIGNAL_2D -> nTerms.coerceAtMost(customCoefficients2D.size).coerceAtMost(250)
                        WaveType.SVG -> nTerms.coerceAtMost(svgCoefficients.size).coerceAtMost(250)
                        else -> nTerms.coerceAtMost(50)
                    }

                    for (i in 0 until termsToDraw) {
                        if (waveType == WaveType.SINE && i > 0) continue
                        
                        if (waveType == WaveType.PURE_SIGNAL) {
                            if (customFunctionSignals[i].isPaused) continue
                        } else {
                            if (removedHarmonics[i] == true) continue
                            if (pausedHarmonics[i] == true) continue
                        }

                        val prevX = x
                        val prevY = y

                        val n = harmonicFrequencies[i] ?: when (waveType) {
                            WaveType.SINE -> 1f
                            WaveType.SQUARE -> (i * 2 + 1).toFloat()
                            WaveType.SAWTOOTH -> (i + 1).toFloat()
                            WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                            WaveType.MY_SIGNAL -> i.toFloat()
                            WaveType.FORMULA -> i.toFloat()
                            WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].freq.toFloat() else 0f
                            WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].freq.toFloat() else 0f
                            WaveType.PURE_SIGNAL -> if (i < customFunctionSignals.size) customFunctionSignals[i].freq.toFloatOrNull() ?: 0f else 0f
                        }

                        val baseN = when (waveType) {
                            WaveType.SINE -> 1f
                            WaveType.SQUARE -> (i * 2 + 1).toFloat()
                            WaveType.SAWTOOTH -> (i + 1).toFloat()
                            WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                            else -> 1f
                        }

                        val (defaultAmp, analyzedPhase) = when (waveType) {
                            WaveType.SINE -> Pair(1f, 0f)
                            WaveType.SQUARE -> {
                                Pair(4f / (baseN * PI.toFloat()), 0f)
                            }
                            WaveType.SAWTOOTH -> {
                                val sign = if (baseN.toInt() % 2 == 0) -1f else 1f
                                Pair((2f / (baseN * PI.toFloat())) * sign, 0f)
                            }
                            WaveType.TRIANGLE -> {
                                val sign = if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                                Pair((8f / (baseN * baseN * PI.toFloat() * PI.toFloat())) * sign, 0f)
                            }
                            // alpha = pi/2 - phi
                            WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (customCoefficients[i].first to (PI.toFloat() / 2f - customCoefficients[i].second)) else (0f to 0f)
                            WaveType.FORMULA -> if (i < formulaCoefficients.size) (formulaCoefficients[i].first to (PI.toFloat() / 2f - formulaCoefficients[i].second)) else (0f to 0f)
                            WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) (customCoefficients2D[i].amp to customCoefficients2D[i].phase) else (0f to 0f)
                            WaveType.SVG -> if (i < svgCoefficients.size) (svgCoefficients[i].amp to svgCoefficients[i].phase) else (0f to 0f)
                            WaveType.PURE_SIGNAL -> if (i < customFunctionSignals.size) {
                                val ampInput = harmonicAmplitudes[i] ?: (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f)
                                val phaseInput = customFunctionSignals[i].cachedPhase
                                ampInput to phaseInput
                            } else (0f to 0f)
                        }

                        val amp = harmonicAmplitudes[i] ?: defaultAmp
                        val phase = harmonicPhases[i] ?: analyzedPhase

                        if (kotlin.math.abs(amp) < 0.005f && i > 0) continue

                        val angle = 2 * PI.toFloat() * n * time
                        val totalAngle = angle + phase
                        val nextX = x + (amp * radiusBasePx) * cos(totalAngle)
                        val nextY = y - (amp * radiusBasePx) * sin(totalAngle)

                        drawCircle(
                            color = (if (waveType == WaveType.PURE_SIGNAL && i < customFunctionSignals.size) customFunctionSignals[i].color else colors.accentCyan).copy(alpha = AppDesign.opacityLow * 2f),
                            radius = kotlin.math.abs(amp * radiusBasePx),
                            center = Offset(prevX, prevY),
                            style = Stroke(width = AppDesign.strokeThin.toPx())
                        )
                        x = nextX
                        y = nextY

                        drawLine(
                            color = (if (waveType == WaveType.PURE_SIGNAL && i < customFunctionSignals.size) customFunctionSignals[i].color else colors.accentCyan).copy(alpha = AppDesign.opacityMedium),
                            start = Offset(prevX, prevY),
                            end = Offset(x, y),
                            strokeWidth = AppDesign.strokeThin.toPx() + 0.5f // ~1.5dp for slightly thicker line
                        )
                    }

                    drawCircle(colors.accentViolet, indicatorSize, Offset(x, y))

                    if (displayMode == FourierDisplayMode.CIRCULAR) {
                        drawLine(
                            color = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle),
                            start = Offset(x, y),
                            end = Offset(waveStartX, y),
                            strokeWidth = AppDesign.strokeThin.toPx()
                        )

                        if (showErrorGradient && path.isNotEmpty()) {
                            // Max error for full violet color (scaled to be meaningful)
                            // Using inverted dynamic sensitivity threshold (Higher value = Higher sensitivity)
                            val maxErr = (101f - errorSensitivity).coerceAtLeast(1f)
                            val currentPathStep = pathStep * 2
                            for (i in 0 until path.size - 1 step currentPathStep) {
                                val p1 = path[i]
                                val p2 = path[(i + currentPathStep).coerceAtMost(path.size - 1)]
                                val lerp = (p1.error / maxErr).coerceIn(0f, 1f)
                                val segmentColor = lerpColor(colors.accentCyan, colors.accentViolet, lerp)
                                drawLine(
                                    color = segmentColor,
                                    start = Offset(waveStartX + (time - p1.offset.x) * pixelsPerTimeUnit, p1.offset.y),
                                    end = Offset(waveStartX + (time - p2.offset.x) * pixelsPerTimeUnit, p2.offset.y),
                                    strokeWidth = AppDesign.strokeStandard.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        } else {
                            val wavePath = Path()
                            if (path.isNotEmpty()) {
                            wavePath.moveTo(waveStartX, path[0].offset.y)
                            // Performance optimization: dynamic step for drawing the wave
                            val currentPathStep = pathStep * 2
                            if (path.size > 1) {
                                for (i in 1 until path.size step currentPathStep) {
                                    wavePath.lineTo(waveStartX + (time - path[i].offset.x) * pixelsPerTimeUnit, path[i].offset.y)
                                }
                                // Ensure the very last point is always connected
                                wavePath.lineTo(waveStartX + (time - path.last().offset.x) * pixelsPerTimeUnit, path.last().offset.y)
                            }
                        }

                            drawPath(
                                path = wavePath,
                                color = colors.accentCyan,
                                style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    } else {
                        if (showErrorGradient && path.isNotEmpty()) {
                            val maxErr = (101f - errorSensitivity).coerceAtLeast(1f)
                            for (i in 0 until path.size - 1 step pathStep) {
                                val p1 = path[i]
                                val p2 = path[(i + pathStep).coerceAtMost(path.size - 1)]
                                val lerp = (p1.error / maxErr).coerceIn(0f, 1f)
                                val segmentColor = lerpColor(colors.accentCyan, colors.accentViolet, lerp)
                                drawLine(
                                    color = segmentColor,
                                    start = p1.offset,
                                    end = p2.offset,
                                    strokeWidth = AppDesign.strokeStandard.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        } else {
                            val tracePath = Path()
                            if (path.isNotEmpty()) {
                                tracePath.moveTo(path[0].offset.x, path[0].offset.y)
                                // Performance optimization: dynamic step for 2D tracing
                                if (path.size > 1) {
                                    for (i in 1 until path.size step pathStep) {
                                        tracePath.lineTo(path[i].offset.x, path[i].offset.y)
                                    }
                                    tracePath.lineTo(path.last().offset.x, path.last().offset.y)
                                }
                            }
                            drawPath(
                                path = tracePath,
                                color = colors.accentCyan,
                                style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            } else if (displayMode == FourierDisplayMode.WRAPPING) {
                translate(size.width / 2f, centerY) {
                    val wrappedPath = Path()
                    var sumX = 0f
                    var sumY = 0f

                    if (path.isNotEmpty()) {
                        var processedCount = 0
                        // Use dynamic step to maintain performance on low-end devices
                        // and eliminating the "orbital jitter" caused by index shifting.
                        for (i in path.indices step pathStep) {
                            val point = path[i].offset
                            val t = point.x
                            val amplitude = point.y // Pure wrapping: amplitude is exactly the signal value
                            val angle = -2 * PI.toFloat() * windingFrequency * t

                            val wx = amplitude * cos(angle)
                            val wy = amplitude * sin(angle)

                            if (i == 0) wrappedPath.moveTo(wx, wy)
                            else wrappedPath.lineTo(wx, wy)

                            // Average only the signal part (point.y) to get true center of mass
                            sumX += point.y * cos(angle)
                            sumY += point.y * sin(angle)
                            processedCount++
                        }

                        drawPath(
                            path = wrappedPath,
                            color = colors.accentCyan.copy(alpha = AppDesign.opacityTrace),
                            style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                        )

                        val avgX = if (processedCount > 0) sumX / processedCount else 0f
                        val avgY = if (processedCount > 0) sumY / processedCount else 0f
                        drawCircle(colors.accentHell, indicatorSize + 1f, Offset(avgX, avgY))

                        drawLine(
                            colors.textSecondary.copy(alpha = AppDesign.opacityMedium),
                            Offset.Zero,
                            Offset(avgX, avgY),
                            AppDesign.strokeThin.toPx()
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
                    IconButton(
                        onClick = { if (nTerms < 250) onNTermsChange(nTerms + 1) },
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_outline),
                            null,
                            tint = colors.accentCyan,
                        )
                    }

                    Slider(
                        value = nTerms.toFloat(),
                        onValueChange = { onNTermsChange(it.toInt()) },
                        valueRange = 1f..250f,
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

                    IconButton(
                        onClick = { if (nTerms > 1) onNTermsChange(nTerms - 1) },
                        modifier = Modifier.size(AppDesign.iconSmallMedium)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.remove),
                            null,
                            tint = colors.accentCyan,
                        )
                    }

                    Text(
                        text = nTerms.toString(),
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
