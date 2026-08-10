package com.example.matharium.fourier.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    nTermsProvider: () -> Int,
    intendedNTermsProvider: () -> Int,
    onIntendedNTermsChange: (Int) -> Unit,
    onActiveNTermsChange: (Int) -> Unit,
    timeProvider: () -> Float,
    pathX: FloatArray,
    pathY: FloatArray,
    pathError: FloatArray,
    pathCountProvider: () -> Int,
    trailPointerProvider: () -> Int,
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
    isAnalyzing: Boolean = false,
    isSynthesizing: Boolean = false,
    cachedHarmonics: List<FourierLogic.Harmonic> = emptyList()
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    val layoutConstants = remember(density, waveStretch) {
        object {
            val radiusBasePx = with(density) { AppDesign.unitCircleRadius.toPx() }
            val unitScale = radiusBasePx
            val gridStepPx = unitScale / 2f
            val pixelsPerTimeUnit = with(density) { waveStretch.dp.toPx() }
            val labelOffsetX = with(density) { 25.dp.toPx() }
            val labelOffsetY = with(density) { AppDesign.spacingSmall.toPx() }
            val labelOffsetAxis = with(density) { AppDesign.spacingSmall.toPx() }
            val labelOffsetWrappingX = with(density) { AppDesign.phasorRadiusBase.toPx() }
            val waveStartX = with(density) { 180.dp.toPx() }
            val indicatorSize = with(density) { AppDesign.spacingExtraSmall.toPx() }
        }
    }

    val pathStep = 1
    val trailSize = 2000

    val labelCache = remember(displayMode, layoutConstants.unitScale) { mutableMapOf<Float, String>() }
    fun getLabel(value: Float, isComplex: Boolean): String {
        return labelCache.getOrPut(value) {
            if (isComplex) String.format(java.util.Locale.US, "%.1fi", value)
            else String.format(java.util.Locale.US, "%.1f", value)
        }
    }

    val reusableWavePath = remember { Path() }
    val reusableTracePath = remember { Path() }
    val reusableWrappedPath = remember { Path() }

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
        modifier = Modifier.fillMaxWidth().height(AppDesign.canvasHeightSmall).clip(RoundedCornerShape(AppDesign.radiusCard)).background(colors.cardSurface.copy(alpha = 0.45f)).border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.6f), RoundedCornerShape(AppDesign.radiusCard))
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize().drawWithCache {
                val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else size.width * 0.20f
                val centerY = size.height * 0.5f
                val staticGridPath = Path()
                val staticAxisPath = Path()
                val step = layoutConstants.gridStepPx
                
                if (displayMode != FourierDisplayMode.WRAPPING) {
                    val left = -actualCenterX; val right = size.width - actualCenterX
                    val top = -centerY; val bottom = size.height - centerY
                    var gx = 0f
                    while (gx <= right) { staticGridPath.moveTo(gx, top); staticGridPath.lineTo(gx, bottom); gx += step }
                    gx = -step
                    while (gx >= left) { staticGridPath.moveTo(gx, top); staticGridPath.lineTo(gx, bottom); gx -= step }
                    var gy = 0f
                    while (gy <= bottom) { staticGridPath.moveTo(left, gy); staticGridPath.lineTo(right, gy); gy += step }
                    gy = -step
                    while (gy >= top) { staticGridPath.moveTo(left, gy); staticGridPath.lineTo(right, gy); gy -= step }
                    staticAxisPath.moveTo(left, 0f); staticAxisPath.lineTo(right, 0f)
                    staticAxisPath.moveTo(0f, top); staticAxisPath.lineTo(0f, bottom)
                } else {
                    val hw = size.width / 2f; val hh = size.height / 2f
                    var gx = 0f
                    while (gx <= hw) { staticGridPath.moveTo(gx, -hh); staticGridPath.lineTo(gx, hh); if (gx > 0) { staticGridPath.moveTo(-gx, -hh); staticGridPath.lineTo(-gx, hh) }; gx += step }
                    var gy = 0f
                    while (gy <= hh) { staticGridPath.moveTo(-hw, gy); staticGridPath.lineTo(hw, gy); if (gy > 0) { staticGridPath.moveTo(-hw, -gy); staticGridPath.lineTo(-hw, -gy) }; gy += step }
                    staticAxisPath.moveTo(-hw, 0f); staticAxisPath.lineTo(hw, 0f); staticAxisPath.moveTo(0f, -hh); staticAxisPath.lineTo(0f, hh)
                }

                onDrawBehind {
                    if (displayMode != FourierDisplayMode.WRAPPING) {
                        translate(actualCenterX, centerY) {
                            drawPath(staticGridPath, gridColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                            drawPath(staticAxisPath, axisColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                            val labelStep = layoutConstants.gridStepPx * 2
                            val right = size.width - actualCenterX; val left = -actualCenterX
                            val bottom = size.height - centerY

                            textPaint.textAlign = android.graphics.Paint.Align.CENTER
                            var lx = labelStep
                            while (lx <= right) { drawIntoCanvas { it.nativeCanvas.drawText(getLabel(lx / layoutConstants.unitScale, false), lx, layoutConstants.labelOffsetX, textPaint) }; lx += labelStep }
                            lx = -labelStep
                            while (lx >= left) { drawIntoCanvas { it.nativeCanvas.drawText(getLabel(lx / layoutConstants.unitScale, false), lx, layoutConstants.labelOffsetX, textPaint) }; lx -= labelStep }

                            textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                            var ly = labelStep
                            while (ly <= bottom) { val txt = getLabel(-ly / layoutConstants.unitScale, displayMode == FourierDisplayMode.COMPLEX); drawIntoCanvas { it.nativeCanvas.drawText(txt, -layoutConstants.labelOffsetAxis, ly + layoutConstants.labelOffsetY, textPaint) }; ly += labelStep }
                            ly = -labelStep
                            while (ly >= -centerY) { val txt = getLabel(-ly / layoutConstants.unitScale, displayMode == FourierDisplayMode.COMPLEX); drawIntoCanvas { it.nativeCanvas.drawText(txt, -layoutConstants.labelOffsetAxis, ly + layoutConstants.labelOffsetY, textPaint) }; ly -= labelStep }
                        }
                    } else {
                        translate(size.width / 2f, centerY) {
                            drawPath(staticGridPath, gridColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                            drawPath(staticAxisPath, axisColor, style = Stroke(width = AppDesign.strokeThin.toPx()))
                            val labelStep = layoutConstants.gridStepPx * 2
                            val hw = size.width / 2f; val hh = size.height / 2f
                            textPaint.textAlign = android.graphics.Paint.Align.CENTER
                            var lx = labelStep
                            while (lx <= hw) { drawIntoCanvas { it.nativeCanvas.drawText(getLabel(lx / layoutConstants.unitScale, false), lx, layoutConstants.labelOffsetWrappingX, textPaint) }; lx += labelStep }
                            lx = -labelStep
                            while (lx >= -hw) { drawIntoCanvas { it.nativeCanvas.drawText(getLabel(lx / layoutConstants.unitScale, false), lx, layoutConstants.labelOffsetWrappingX, textPaint) }; lx -= labelStep }
                            textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                            var ly = labelStep
                            while (ly <= hh) { drawIntoCanvas { it.nativeCanvas.drawText(getLabel(-ly / layoutConstants.unitScale, false), -layoutConstants.labelOffsetAxis, ly + layoutConstants.labelOffsetY, textPaint) }; ly += labelStep }
                            ly = -labelStep
                            while (ly >= -hh) { drawIntoCanvas { it.nativeCanvas.drawText(getLabel(-ly / layoutConstants.unitScale, false), -layoutConstants.labelOffsetAxis, ly + layoutConstants.labelOffsetY, textPaint) }; ly -= labelStep }
                        }
                    }
                }
            }
        ) {
            val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else size.width * 0.20f
            val centerY = size.height * 0.5f

            if ((displayMode == FourierDisplayMode.CIRCULAR) || (displayMode == FourierDisplayMode.COMPLEX)) {
                translate(actualCenterX, centerY) {
                    var x = 0f; var y = 0f
                    val harmonics = cachedHarmonics
                    val termsToDraw = if (waveType == WaveType.PURE_SIGNAL) harmonics.size else nTermsProvider()
                    val currentTime = timeProvider()

                    for (i in 0 until termsToDraw) {
                        if (i >= harmonics.size) break
                        val isPaused = if (waveType == WaveType.PURE_SIGNAL && i < customFunctionSignals.size) {
                            customFunctionSignals[i].isPaused || pausedHarmonics[i] == true
                        } else {
                            pausedHarmonics[i] == true
                        }
                        if (removedHarmonics[i] == true || isPaused) continue
                        val h = harmonics[i]
                        val prevX = x; val prevY = y
                        val n = harmonicFrequencies[i] ?: h.freq; val amp = harmonicAmplitudes[i] ?: h.amp; val phase = harmonicPhases[i] ?: h.phase
                        if (kotlin.math.abs(amp) < 0.005f && i > 0) continue
                        val totalAngle = (2 * PI.toFloat() * n * currentTime) + phase
                        val nextX = x + (amp * layoutConstants.radiusBasePx) * cos(totalAngle.toDouble()).toFloat()
                        val nextY = y - (amp * layoutConstants.radiusBasePx) * sin(totalAngle.toDouble()).toFloat()
                        val termColor = if (h.colorArgb != 0) Color(h.colorArgb) else colors.accentCyan
                        drawCircle(color = termColor.copy(alpha = AppDesign.opacityLow * 2f), radius = kotlin.math.abs(amp * layoutConstants.radiusBasePx), center = Offset(prevX, prevY), style = Stroke(width = AppDesign.strokeThin.toPx()))
                        x = nextX; y = nextY
                        drawLine(color = termColor.copy(alpha = AppDesign.opacityMedium), start = Offset(prevX, prevY), end = Offset(x, y), strokeWidth = AppDesign.strokeThin.toPx() + 0.5f)
                    }

                    drawCircle(colors.accentViolet, layoutConstants.indicatorSize, Offset(x, y))

                    if (displayMode == FourierDisplayMode.CIRCULAR) {
                        drawLine(color = axisColor, start = Offset(x, y), end = Offset(layoutConstants.waveStartX, y), strokeWidth = AppDesign.strokeThin.toPx())
                        val count = pathCountProvider()
                        if (count > 0) {
                            val tp = trailPointerProvider()
                            val startIdx = (tp - count + trailSize) % trailSize
                            if (showErrorGradient) {
                                val maxErr = (101f - errorSensitivity).coerceAtLeast(1f)
                                for (i in 0 until count - 1 step (pathStep * 2)) {
                                    val idx1 = (startIdx + i) % trailSize; val idx2 = (startIdx + i + 1) % trailSize
                                    val lerp = (pathError[idx1] / maxErr).coerceIn(0f, 1f)
                                    drawLine(color = lerpColor(colors.accentCyan, colors.accentViolet, lerp), start = Offset(layoutConstants.waveStartX + (currentTime - pathX[idx1]) * layoutConstants.pixelsPerTimeUnit, pathY[idx1]), end = Offset(layoutConstants.waveStartX + (currentTime - pathX[idx2]) * layoutConstants.pixelsPerTimeUnit, pathY[idx2]), strokeWidth = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                                }
                            } else {
                                reusableWavePath.reset()
                                reusableWavePath.moveTo(layoutConstants.waveStartX + (currentTime - pathX[startIdx]) * layoutConstants.pixelsPerTimeUnit, pathY[startIdx])
                                for (i in 1 until count step (pathStep * 2)) {
                                    val idx = (startIdx + i) % trailSize
                                    reusableWavePath.lineTo(layoutConstants.waveStartX + (currentTime - pathX[idx]) * layoutConstants.pixelsPerTimeUnit, pathY[idx])
                                }
                                drawPath(path = reusableWavePath, color = colors.accentCyan, style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    } else {
                        val count = pathCountProvider()
                        if (count > 0) {
                            val tp = trailPointerProvider()
                            val startIdx = (tp - count + trailSize) % trailSize
                            if (showErrorGradient) {
                                val maxErr = (101f - errorSensitivity).coerceAtLeast(1f)
                                for (i in 0 until count - 1 step pathStep) {
                                    val idx1 = (startIdx + i) % trailSize; val idx2 = (startIdx + i + 1) % trailSize
                                    val lerp = (pathError[idx1] / maxErr).coerceIn(0f, 1f)
                                    drawLine(color = lerpColor(colors.accentCyan, colors.accentViolet, lerp), start = Offset(pathX[idx1], pathY[idx1]), end = Offset(pathX[idx2], pathY[idx2]), strokeWidth = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                                }
                            } else {
                                reusableTracePath.reset()
                                reusableTracePath.moveTo(pathX[startIdx], pathY[startIdx])
                                for (i in 1 until count step pathStep) {
                                    val idx = (startIdx + i) % trailSize
                                    reusableTracePath.lineTo(pathX[idx], pathY[idx])
                                }
                                drawPath(path = reusableTracePath, color = colors.accentCyan, style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    }
                }
            } else if (displayMode == FourierDisplayMode.WRAPPING) {
                translate(size.width / 2f, centerY) {
                    val count = pathCountProvider()
                    if (count > 0) {
                        reusableWrappedPath.reset()
                        val tp = trailPointerProvider()
                        val startIdx = (tp - count + trailSize) % trailSize
                        var sumX = 0f; var sumY = 0f; var processed = 0
                        for (i in 0 until count step pathStep) {
                            val idx = (startIdx + i) % trailSize
                            val angle = -2 * PI.toFloat() * windingFrequency * pathX[idx]
                            val wx = pathY[idx] * cos(angle.toDouble()).toFloat(); val wy = pathY[idx] * sin(angle.toDouble()).toFloat()
                            if (i == 0) reusableWrappedPath.moveTo(wx, wy) else reusableWrappedPath.lineTo(wx, wy)
                            sumX += wx; sumY += wy; processed++
                        }
                        drawPath(path = reusableWrappedPath, color = colors.accentCyan.copy(alpha = AppDesign.opacityTrace), style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round))
                        if (processed > 0) {
                            val ax = sumX / processed; val ay = sumY / processed
                            drawCircle(colors.accentHell, layoutConstants.indicatorSize + 1f, Offset(ax, ay))
                            drawLine(colors.textSecondary.copy(alpha = AppDesign.opacityMedium), Offset.Zero, Offset(ax, ay), AppDesign.strokeThin.toPx())
                        }
                    }
                }
            }
        }

        if (isAnalyzing) {
            Box(modifier = Modifier.padding(AppDesign.radiusLarge).align(Alignment.TopCenter).clip(RoundedCornerShape(AppDesign.radiusMedium)).background(colors.cardSurface.copy(alpha = 0.8f)).border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(AppDesign.radiusMedium)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.accentCyan, strokeWidth = 2.dp)
                    Text("Analyzing Signal...", color = colors.textPrimary, fontSize = AppDesign.textCaption, fontWeight = FontWeight.Bold)
                }
            }
        }

        FourierLeftSidebar(displayMode = displayMode, onDisplayModeChange = onDisplayModeChange, onClearPath = onClearPath, colors = colors)
        FourierRightSidebar(displayNTermsProvider = intendedNTermsProvider, onDisplayNTermsChange = onIntendedNTermsChange, onActiveNTermsChange = onActiveNTermsChange, colors = colors)
    }
}
