package com.example.matharium.fourier.ui

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
import com.example.matharium.R
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.FourierLogic
import com.example.matharium.fourier.state.*
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
    timeProvider: () -> Float,
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
    harmonicPhases: Map<Int, Float> = emptyMap(),
    isCalculating: Boolean = false,
    cachedHarmonics: List<FourierLogic.Harmonic> = emptyList()
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

    // Perfect resolution for flagship devices
    val pathStep = 1

    // OPTIMIZATION: Reusable Paths to avoid allocations per frame
    val reusableWavePath = remember { Path() }
    val reusableTracePath = remember { Path() }
    val reusableWrappedPath = remember { Path() }

    // OPTIMIZATION: Cache static paint and grid settings
    val axisColor = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle)
    val gridColor = colors.accentCyan.copy(alpha = AppDesign.opacityGrid)
    val labelColor = colors.textSecondary.copy(alpha = AppDesign.opacityMedium).toArgb()
    
    val textPaint = remember(labelColor, density) {
        android.graphics.Paint().apply {
            color = labelColor
            textSize = with(density) { AppDesign.textCaption.toPx() }
            isAntiAlias = true
        }
    }

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
                    // STATIC LAYER OPTIMIZATION:
                    // Pre-calculate and cache the grid and axes path. 
                    // This block only runs when displayMode, colors, or size change.
                    val centerX = size.width * 0.20f
                    val centerY = size.height * 0.5f
                    val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else centerX
                    
                    val staticGridPath = Path()
                    val staticAxisPath = Path()
                    
                    if (displayMode != FourierDisplayMode.WRAPPING) {
                        val step = gridStepPx
                        val left = -actualCenterX
                        val right = size.width - actualCenterX
                        val top = -centerY
                        val bottom = size.height - centerY

                        var gx = 0f
                        while (gx <= right) {
                            staticGridPath.moveTo(gx, top); staticGridPath.lineTo(gx, bottom); gx += step
                        }
                        gx = -step
                        while (gx >= left) {
                            staticGridPath.moveTo(gx, top); staticGridPath.lineTo(gx, bottom); gx -= step
                        }
                        var gy = 0f
                        while (gy <= bottom) {
                            staticGridPath.moveTo(left, gy); staticGridPath.lineTo(right, gy); gy += step
                        }
                        gy = -step
                        while (gy >= top) {
                            staticGridPath.moveTo(left, gy); staticGridPath.lineTo(right, gy); gy -= step
                        }
                        
                        staticAxisPath.moveTo(left, 0f); staticAxisPath.lineTo(right, 0f)
                        staticAxisPath.moveTo(0f, top); staticAxisPath.lineTo(0f, bottom)
                    } else {
                        val step = gridStepPx
                        val halfWidth = size.width / 2f
                        val halfHeight = size.height / 2f
                        var gx = 0f
                        while (gx <= halfWidth) {
                            staticGridPath.moveTo(gx, -halfHeight); staticGridPath.lineTo(gx, halfHeight)
                            if (gx > 0) { staticGridPath.moveTo(-gx, -halfHeight); staticGridPath.lineTo(-gx, halfHeight) }
                            gx += step
                        }
                        var gy = 0f
                        while (gy <= halfHeight) {
                            staticGridPath.moveTo(-halfWidth, gy); staticGridPath.lineTo(halfWidth, gy)
                            if (gy > 0) { staticGridPath.moveTo(-halfWidth, -gy); staticGridPath.lineTo(-halfWidth, -gy) }
                            gy += step
                        }
                        staticAxisPath.moveTo(-halfWidth, 0f); staticAxisPath.lineTo(halfWidth, 0f)
                        staticAxisPath.moveTo(0f, -halfHeight); staticAxisPath.lineTo(0f, halfHeight)
                    }

                    onDrawBehind {
                        // Drawing static grid from cache
                        if (displayMode != FourierDisplayMode.WRAPPING) {
                            translate(actualCenterX, centerY) {
                                drawPath(staticGridPath, gridColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                drawPath(staticAxisPath, axisColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                
                                val step = gridStepPx * 2
                                val left = -actualCenterX
                                val right = size.width - actualCenterX
                                val top = -centerY
                                val bottom = size.height - centerY

                                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                                var lx = step
                                while (lx <= right) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetX, textPaint) }
                                    lx += step
                                }
                                lx = -step
                                while (lx >= left) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetX, textPaint) }
                                    lx -= step
                                }

                                textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                                var ly = step
                                while (ly <= bottom) {
                                    val labelText = if (displayMode == FourierDisplayMode.COMPLEX) String.format(java.util.Locale.US, "%.1fi", -ly / unitScale) else String.format(java.util.Locale.US, "%.1f", -ly / unitScale)
                                    drawIntoCanvas { it.nativeCanvas.drawText(labelText, -labelOffsetAxis, ly + labelOffsetY, textPaint) }
                                    ly += step
                                }
                                ly = -step
                                while (ly >= top) {
                                    val labelText = if (displayMode == FourierDisplayMode.COMPLEX) String.format(java.util.Locale.US, "%.1fi", -ly / unitScale) else String.format(java.util.Locale.US, "%.1f", -ly / unitScale)
                                    drawIntoCanvas { it.nativeCanvas.drawText(labelText, -labelOffsetAxis, ly + labelOffsetY, textPaint) }
                                    ly -= step
                                }
                            }
                        } else {
                            translate(size.width / 2f, centerY) {
                                drawPath(staticGridPath, gridColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                drawPath(staticAxisPath, axisColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                                
                                val halfWidth = size.width / 2f
                                val halfHeight = size.height / 2f
                                val step = gridStepPx * 2
                                
                                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                                var lx = step
                                while (lx <= halfWidth) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetWrappingX, textPaint) }
                                    lx += step
                                }
                                lx = -step
                                while (lx >= -halfWidth) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", lx / unitScale), lx, labelOffsetWrappingX, textPaint) }
                                    lx -= step
                                }

                                textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                                var ly = step
                                while (ly <= halfHeight) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", -ly / unitScale), -labelOffsetAxis, ly + labelOffsetY, textPaint) }
                                    ly += step
                                }
                                ly = -step
                                while (ly >= -halfHeight) {
                                    drawIntoCanvas { it.nativeCanvas.drawText(String.format(java.util.Locale.US, "%.1f", -ly / unitScale), -labelOffsetAxis, ly + labelOffsetY, textPaint) }
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
                    
                    val harmonics = cachedHarmonics
                    val termsToDraw = if (waveType == WaveType.PURE_SIGNAL) harmonics.size else nTerms
                    val currentTime = timeProvider()

                    for (i in 0 until termsToDraw) {
                        if (i >= harmonics.size) break
                        if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue
                        
                        val h = harmonics[i]
                        val prevX = x
                        val prevY = y

                        val n = harmonicFrequencies[i] ?: h.freq
                        val amp = harmonicAmplitudes[i] ?: h.amp
                        val phase = harmonicPhases[i] ?: h.phase

                        if (kotlin.math.abs(amp) < 0.005f && i > 0) continue

                        val totalAngle = (2 * PI.toFloat() * n * currentTime) + phase
                        val nextX = x + (amp * radiusBasePx) * cos(totalAngle.toDouble()).toFloat()
                        val nextY = y - (amp * radiusBasePx) * sin(totalAngle.toDouble()).toFloat()

                        val termColor = if (h.colorArgb != 0) Color(h.colorArgb) else colors.accentCyan
                        drawCircle(
                            color = termColor.copy(alpha = AppDesign.opacityLow * 2f),
                            radius = kotlin.math.abs(amp * radiusBasePx),
                            center = Offset(prevX, prevY),
                            style = Stroke(width = AppDesign.strokeThin.toPx())
                        )
                        x = nextX
                        y = nextY

                        drawLine(
                            color = termColor.copy(alpha = AppDesign.opacityMedium),
                            start = Offset(prevX, prevY),
                            end = Offset(x, y),
                            strokeWidth = AppDesign.strokeThin.toPx() + 0.5f
                        )
                    }

                    drawCircle(colors.accentViolet, indicatorSize, Offset(x, y))

                    if (displayMode == FourierDisplayMode.CIRCULAR) {
                        drawLine(
                            color = axisColor,
                            start = Offset(x, y),
                            end = Offset(waveStartX, y),
                            strokeWidth = AppDesign.strokeThin.toPx()
                        )

                        if (showErrorGradient && path.isNotEmpty()) {
                            val maxErr = (101f - errorSensitivity).coerceAtLeast(1f)
                            val currentPathStep = pathStep * 2
                            for (i in 0 until path.size - 1 step currentPathStep) {
                                val p1 = path[i]
                                val p2 = path[(i + currentPathStep).coerceAtMost(path.size - 1)]
                                val lerp = (p1.error / maxErr).coerceIn(0f, 1f)
                                drawLine(
                                    color = lerpColor(colors.accentCyan, colors.accentViolet, lerp),
                                    start = Offset(waveStartX + (currentTime - p1.offset.x) * pixelsPerTimeUnit, p1.offset.y),
                                    end = Offset(waveStartX + (currentTime - p2.offset.x) * pixelsPerTimeUnit, p2.offset.y),
                                    strokeWidth = AppDesign.strokeStandard.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        } else if (path.isNotEmpty()) {
                            reusableWavePath.reset()
                            
                            val startX = waveStartX + (currentTime - path[0].offset.x) * pixelsPerTimeUnit
                            reusableWavePath.moveTo(startX, path[0].offset.y)
                            
                            val currentPathStep = pathStep * 2
                            for (i in 1 until path.size step currentPathStep) {
                                reusableWavePath.lineTo(waveStartX + (currentTime - path[i].offset.x) * pixelsPerTimeUnit, path[i].offset.y)
                            }
                            reusableWavePath.lineTo(waveStartX + (currentTime - path.last().offset.x) * pixelsPerTimeUnit, path.last().offset.y)

                            drawPath(
                                path = reusableWavePath,
                                color = colors.accentCyan,
                                style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    } else if (path.isNotEmpty()) {
                        if (showErrorGradient) {
                            val maxErr = (101f - errorSensitivity).coerceAtLeast(1f)
                            for (i in 0 until path.size - 1 step pathStep) {
                                val p1 = path[i]; val p2 = path[(i + pathStep).coerceAtMost(path.size - 1)]
                                val lerp = (p1.error / maxErr).coerceIn(0f, 1f)
                                drawLine(
                                    color = lerpColor(colors.accentCyan, colors.accentViolet, lerp),
                                    start = p1.offset, end = p2.offset,
                                    strokeWidth = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round
                                )
                            }
                        } else {
                            reusableTracePath.reset()
                            reusableTracePath.moveTo(path[0].offset.x, path[0].offset.y)
                            for (i in 1 until path.size step pathStep) {
                                reusableTracePath.lineTo(path[i].offset.x, path[i].offset.y)
                            }
                            reusableTracePath.lineTo(path.last().offset.x, path.last().offset.y)
                            drawPath(
                                path = reusableTracePath,
                                color = colors.accentCyan,
                                style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            } else if (displayMode == FourierDisplayMode.WRAPPING) {
                translate(size.width / 2f, centerY) {
                    if (path.isNotEmpty()) {
                        reusableWrappedPath.reset()
                        var sumX = 0f; var sumY = 0f; var processedCount = 0
                        for (i in path.indices step pathStep) {
                            val point = path[i].offset
                            val angle = -2 * PI.toFloat() * windingFrequency * point.x
                            val wx = point.y * cos(angle.toDouble()).toFloat()
                            val wy = point.y * sin(angle.toDouble()).toFloat()

                            if (i == 0) reusableWrappedPath.moveTo(wx, wy)
                            else reusableWrappedPath.lineTo(wx, wy)

                            sumX += wx; sumY += wy; processedCount++
                        }

                        drawPath(
                            path = reusableWrappedPath,
                            color = colors.accentCyan.copy(alpha = AppDesign.opacityTrace),
                            style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                        )

                        if (processedCount > 0) {
                            val avgX = sumX / processedCount; val avgY = sumY / processedCount
                            drawCircle(colors.accentHell, indicatorSize + 1f, Offset(avgX, avgY))
                            drawLine(colors.textSecondary.copy(alpha = AppDesign.opacityMedium), Offset.Zero, Offset(avgX, avgY), AppDesign.strokeThin.toPx())
                        }
                    }
                }
            }
        }

        // Status Indicators
        if (isCalculating) {
            Box(
                modifier = Modifier
                    .padding(AppDesign.radiusLarge)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(AppDesign.radiusMedium))
                    .background(colors.cardSurface.copy(alpha = 0.8f))
                    .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(AppDesign.radiusMedium))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colors.accentCyan,
                        strokeWidth = 2.dp
                    )
                    Text(
                        "Analyzing Signal...",
                        color = colors.textPrimary,
                        fontSize = AppDesign.textCaption,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Sidebar Navigation
        FourierLeftSidebar(
            displayMode = displayMode,
            onDisplayModeChange = onDisplayModeChange,
            onClearPath = onClearPath,
            colors = colors
        )

        // Terms Handler (Right Sidebar)
        FourierRightSidebar(
            nTerms = nTerms,
            onNTermsChange = onNTermsChange,
            colors = colors
        )
    }
}
