package com.example.matharium.fourier

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourierSettingsCard(
    waveType: WaveType,
    onWaveTypeChange: (WaveType) -> Unit,
    onRunningChange: (Boolean) -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    windingFrequency: Float,
    onWindingFrequencyChange: (Float) -> Unit,
    displayMode: FourierDisplayMode,
    drawingPoints: MutableList<Float>,
    drawingPoints2D: MutableList<Offset>,
    svgPoints: MutableList<Offset>,
    customFunctionSignals: MutableList<SignalInstance>,
    formulaString: String,
    onFormulaChange: (String) -> Unit,
    onCalculateDFT: () -> Unit,
    onCalculateDFT2D: () -> Unit,
    onClearCustomCoefficients: () -> Unit,
    onClearCustomCoefficients2D: () -> Unit,
    onClearSVGCoefficients: () -> Unit,
    onClearPath: () -> Unit,
    onResetTime: () -> Unit,
    onResetHasStarted: () -> Unit,
    svgPickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    nextSignalId: Int,
    onNextSignalIdChange: (Int) -> Unit,
    isSignalsExpanded: Boolean,
    onSignalsExpandedChange: (Boolean) -> Unit,
    colors: AppColors,
    prefs: AppPreferences,
    samplesCount: Int
) {
    var isSettingsExpanded by remember { mutableStateOf(true) }

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.spacingLarge)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSettingsExpanded = !isSettingsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Simulator Settings",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    if (isSettingsExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                    null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
            }

            AnimatedVisibility(visible = isSettingsExpanded) {
                Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
                    Text("Wave Type", color = colors.textSecondary, fontSize = AppDesign.textBody)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDesign.chipHeight + 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppDesign.radiusSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WaveType.entries.forEach { type ->
                            if (type == WaveType.MY_SIGNAL_2D || type == WaveType.FORMULA) return@forEach // Skip merged types
                            val selected = (waveType == type) ||
                                    (type == WaveType.MY_SIGNAL && waveType == WaveType.MY_SIGNAL_2D) ||
                                    (type == WaveType.PURE_SIGNAL && waveType == WaveType.FORMULA)
                            
                            val isSpecialType = type == WaveType.MY_SIGNAL || type == WaveType.PURE_SIGNAL || type == WaveType.SVG

                            Box(
                                modifier = Modifier
                                    .height(AppDesign.chipHeight)
                                    .clip(RoundedCornerShape(AppDesign.radiusSmall))
                                    .background(if (selected && !isSpecialType) colors.accentCyan.copy(alpha = 0.05f) else Color.Transparent)
                                    .border(
                                        AppDesign.borderThin,
                                        if (isSpecialType && selected) {
                                            Brush.linearGradient(listOf(colors.accentCyan, colors.accentViolet))
                                        } else {
                                            SolidColor(if (selected) colors.accentCyan else colors.cardBorder.copy(alpha = AppDesign.opacityMedium))
                                        },
                                        RoundedCornerShape(AppDesign.radiusSmall)
                                    )
                                    .clickable {
                                        onWaveTypeChange(type)
                                        onClearPath()
                                        if (type == WaveType.SVG) {
                                            onRunningChange(false)
                                            onClearPath()
                                            onResetTime()
                                            svgPickerLauncher.launch("image/svg+xml")
                                        }
                                        if (type == WaveType.MY_SIGNAL && drawingPoints.isEmpty()) {
                                            repeat(samplesCount) { drawingPoints.add(0f) }
                                        }
                                    }
                                    .padding(horizontal = AppDesign.spacingMedium),
                                contentAlignment = Alignment.Center
                            ) {
                                val labelText = when (type) {
                                    WaveType.MY_SIGNAL -> "Draw"
                                    WaveType.PURE_SIGNAL -> "Custom"
                                    WaveType.SVG -> "Import SVG"
                                    else -> type.name.lowercase().replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
                                    }
                                }
                                Text(
                                    labelText,
                                    color = if (selected) colors.accentCyan else colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height( AppDesign.spacingLarge))
                    HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.2f))

                    val customUiType = when (waveType) {
                        WaveType.MY_SIGNAL, WaveType.MY_SIGNAL_2D -> 1
                        WaveType.PURE_SIGNAL, WaveType.FORMULA -> 2
                        else -> 0
                    }

                    AnimatedContent(
                        targetState = customUiType,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "CustomSettingsUI"
                    ) { type ->
                        when (type) {
                            1 -> {
                                Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .height(AppDesign.chipHeight + 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(AppDesign.radiusSmall))
                                                .background(if (waveType == WaveType.MY_SIGNAL) colors.accentCyan.copy(0.1f) else Color.Transparent)
                                                .border(
                                                    AppDesign.borderThin,
                                                    if (waveType == WaveType.MY_SIGNAL) colors.accentCyan else colors.cardBorder.copy(0.3f),
                                                    RoundedCornerShape(AppDesign.radiusSmall)
                                                )
                                                .clickable { onWaveTypeChange(WaveType.MY_SIGNAL); onClearPath() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("1D Drawing", color = if (waveType == WaveType.MY_SIGNAL) colors.accentCyan else colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(AppDesign.radiusSmall))
                                                .background(if (waveType == WaveType.MY_SIGNAL_2D) colors.accentCyan.copy(0.1f) else Color.Transparent)
                                                .border(
                                                    AppDesign.borderThin,
                                                    if (waveType == WaveType.MY_SIGNAL_2D) colors.accentCyan else colors.cardBorder.copy(0.3f),
                                                    RoundedCornerShape(AppDesign.radiusSmall)
                                                )
                                                .clickable { onWaveTypeChange(WaveType.MY_SIGNAL_2D); onClearPath() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("2D Drawing", color = if (waveType == WaveType.MY_SIGNAL_2D) colors.accentCyan else colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(Modifier.height(AppDesign.spacingMedium))

                                    Text(
                                        "Draw your wave below",
                                        color = colors.accentCyan,
                                        fontSize = AppDesign.textBody,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(AppDesign.radiusSmall))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(AppDesign.drawingAreaHeight)
                                            .background(
                                                colors.cardSurface.copy(alpha = 0.2f),
                                                RoundedCornerShape(AppDesign.radiusSmall)
                                            )
                                            .border(
                                                BorderStroke(
                                                    AppDesign.borderThin,
                                                    Brush.linearGradient(
                                                        listOf(
                                                            colors.accentCyan,
                                                            colors.accentViolet
                                                        )
                                                    )
                                                ),
                                                RoundedCornerShape(AppDesign.radiusSmall)
                                            )
                                            .pointerInput(waveType) {
                                                if (waveType == WaveType.MY_SIGNAL) {
                                                    var lastIndex = -1
                                                    var lastY = 0f

                                                    detectDragGestures(
                                                        onDragStart = { offset ->
                                                            onRunningChange(false)
                                                            onClearPath()
                                                            onResetTime()

                                                            lastIndex =
                                                                ((offset.x / size.width.toFloat()) * samplesCount).toInt()
                                                                    .coerceIn(0, samplesCount - 1)
                                                            lastY = (offset.y - size.height / 2f).coerceIn(
                                                                -size.height / 2f,
                                                                size.height / 2f
                                                            )
                                                            drawingPoints[lastIndex] = lastY
                                                        },
                                                        onDrag = { change, _ ->
                                                            val x = change.position.x
                                                            val y =
                                                                (change.position.y - size.height / 2f).coerceIn(
                                                                    -size.height / 2f,
                                                                    size.height / 2f
                                                                )
                                                            val currentIndex =
                                                                ((x / size.width.toFloat()) * samplesCount).toInt()
                                                                    .coerceIn(0, samplesCount - 1)

                                                            if (lastIndex != -1) {
                                                                val start = minOf(lastIndex, currentIndex)
                                                                val end = maxOf(lastIndex, currentIndex)
                                                                val limit = size.height / 2f

                                                                for (i in start..end) {
                                                                    val t =
                                                                        if (end == start) 1f else (i - lastIndex).toFloat() / (currentIndex - lastIndex)
                                                                    val interpolatedY =
                                                                        lastY + (y - lastY) * t
                                                                    drawingPoints[i] =
                                                                        interpolatedY.coerceIn(
                                                                            -limit,
                                                                            limit
                                                                        )
                                                                }
                                                            }

                                                            lastIndex = currentIndex
                                                            lastY = y
                                                            onCalculateDFT()
                                                        },
                                                        onDragEnd = {
                                                            prefs.drawingPoints = drawingPoints.toList()
                                                        }
                                                    )
                                                } else {
                                                    detectDragGestures(
                                                        onDragStart = { offset ->
                                                            onRunningChange(false)
                                                            onClearPath()
                                                            onResetTime()
                                                            drawingPoints2D.clear()
                                                            val centered = offset - Offset(size.width / 2f, size.height / 2f)
                                                            drawingPoints2D.add(centered)
                                                        },
                                                        onDrag = { change, _ ->
                                                            val halfWidth = size.width / 2f
                                                            val halfHeight = size.height / 2f
                                                            val x = (change.position.x - halfWidth).coerceIn(-halfWidth, halfWidth)
                                                            val y = (change.position.y - halfHeight).coerceIn(-halfHeight, halfHeight)

                                                            drawingPoints2D.add(Offset(x, y))
                                                            onCalculateDFT2D()
                                                        },
                                                        onDragEnd = {
                                                            prefs.drawingPoints2D = drawingPoints2D.toList()
                                                        }
                                                    )
                                                }
                                            }
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val w = size.width
                                            val h = size.height

                                            if (waveType == WaveType.MY_SIGNAL) {
                                                drawLine(
                                                    colors.textSecondary.copy(alpha = 0.2f),
                                                    Offset(0f, h / 2),
                                                    Offset(w, h / 2),
                                                    AppDesign.strokeThin
                                                )

                                                if (drawingPoints.size == samplesCount) {
                                                    val p = Path()
                                                    for (i in 0 until samplesCount) {
                                                        val px = i.toFloat() / samplesCount * w
                                                        val py = h / 2 + drawingPoints[i]
                                                        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                                                    }
                                                    drawPath(
                                                        p,
                                                        colors.accentCyan,
                                                        style = Stroke(AppDesign.borderStandard.toPx())
                                                    )
                                                }
                                            } else {
                                                val gridColor = colors.textSecondary.copy(alpha = 0.1f)
                                                drawLine(gridColor, Offset(w / 2, 0f), Offset(w / 2, h), 1f)
                                                drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), 1f)

                                                if (drawingPoints2D.isNotEmpty()) {
                                                    val p = Path()
                                                    val center = Offset(w / 2f, h / 2f)
                                                    p.moveTo(drawingPoints2D[0].x + center.x, drawingPoints2D[0].y + center.y)
                                                    for (i in 1 until drawingPoints2D.size) {
                                                        p.lineTo(drawingPoints2D[i].x + center.x, drawingPoints2D[i].y + center.y)
                                                    }
                                                    drawPath(
                                                        p,
                                                        colors.accentCyan,
                                                        style = Stroke(AppDesign.borderStandard.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(top = AppDesign.spacingMedium)
                                            .height(AppDesign.buttonHeightSmall)
                                            .clip(RoundedCornerShape(AppDesign.radiusButton))
                                            .background(colors.accentHell.copy(alpha = AppDesign.opacityLow))
                                            .border(
                                                AppDesign.borderThin,
                                                colors.accentHell.copy(alpha = AppDesign.opacityMedium),
                                                RoundedCornerShape(AppDesign.radiusButton)
                                            )
                                            .clickable {
                                                if (waveType == WaveType.MY_SIGNAL) {
                                                    drawingPoints.clear()
                                                    repeat(samplesCount) { drawingPoints.add(0f) }
                                                    prefs.drawingPoints = emptyList<Float>()
                                                    onClearCustomCoefficients()
                                                } else if (waveType == WaveType.MY_SIGNAL_2D) {
                                                    drawingPoints2D.clear()
                                                    prefs.drawingPoints2D = emptyList<Offset>()
                                                    onClearCustomCoefficients2D()
                                                } else if (waveType == WaveType.SVG) {
                                                    svgPoints.clear()
                                                    onClearSVGCoefficients()
                                                }
                                                onClearPath()
                                                onResetTime()
                                                onRunningChange(false)
                                                onResetHasStarted()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = AppDesign.spacingMedium)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.trash_outline),
                                                null,
                                                tint = colors.accentHell,
                                                modifier = Modifier.size(AppDesign.iconSmall)
                                            )
                                            Spacer(Modifier.width(AppDesign.spacingSmall))
                                            Text(
                                                "Clear Drawing",
                                                fontSize = AppDesign.textSmall,
                                                color = colors.accentHell,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            2 -> {
                                Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .height(AppDesign.chipHeight + 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(AppDesign.radiusSmall))
                                                .background(if (waveType == WaveType.PURE_SIGNAL) colors.accentCyan.copy(0.1f) else Color.Transparent)
                                                .border(
                                                    AppDesign.borderThin,
                                                    if (waveType == WaveType.PURE_SIGNAL) colors.accentCyan else colors.cardBorder.copy(0.3f),
                                                    RoundedCornerShape(AppDesign.radiusSmall)
                                                )
                                                .clickable { onWaveTypeChange(WaveType.PURE_SIGNAL); onClearPath() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Signal", color = if (waveType == WaveType.PURE_SIGNAL) colors.accentCyan else colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(AppDesign.radiusSmall))
                                                .background(if (waveType == WaveType.FORMULA) colors.accentCyan.copy(0.1f) else Color.Transparent)
                                                .border(
                                                    AppDesign.borderThin,
                                                    if (waveType == WaveType.FORMULA) colors.accentCyan else colors.cardBorder.copy(0.3f),
                                                    RoundedCornerShape(AppDesign.radiusSmall)
                                                )
                                                .clickable { onWaveTypeChange(WaveType.FORMULA); onClearPath() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Formula", color = if (waveType == WaveType.FORMULA) colors.accentCyan else colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(Modifier.height(AppDesign.spacingMedium))

                                    if (waveType == WaveType.PURE_SIGNAL) {
                                        Text(
                                            "Define your signal components",
                                            color = colors.accentCyan,
                                            fontSize = AppDesign.textBody,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(AppDesign.radiusSmall))

                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .height(AppDesign.chipHeight + 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
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
                                                        val last = customFunctionSignals.lastOrNull()
                                                        val nextFreq = last?.freq ?: "1.0"
                                                        val nextAmp = last?.amp ?: "50.0"
                                                        val color = Color.hsv(
                                                            kotlin.random.Random.nextFloat() * 360f,
                                                            0.7f,
                                                            0.9f
                                                        )
                                                        customFunctionSignals.add(
                                                            SignalInstance(
                                                                nextSignalId,
                                                                color,
                                                                nextFreq,
                                                                nextAmp
                                                            )
                                                        )
                                                        onNextSignalIdChange(nextSignalId + 1)
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
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        "Add",
                                                        fontSize = AppDesign.textSmall,
                                                        color = colors.textPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

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
                                                    .clickable {
                                                        customFunctionSignals.clear()
                                                        onNextSignalIdChange(0)
                                                        onRunningChange(false)
                                                        onResetHasStarted()
                                                        onClearPath()
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
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        "Clear",
                                                        fontSize = AppDesign.textSmall,
                                                        color = colors.accentHell,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        if (customFunctionSignals.isNotEmpty()) {
                                            Spacer(Modifier.height(AppDesign.spacingMedium))
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                                            ) {
                                                val displayList =
                                                    if (isSignalsExpanded) customFunctionSignals else customFunctionSignals.take(
                                                        5
                                                    )
                                                displayList.forEachIndexed { _, signal ->
                                                    SignalSettingsCard(
                                                        signal = signal,
                                                        colors = colors,
                                                        showDel = customFunctionSignals.size > 1,
                                                        onParameterChange = {
                                                            onResetHasStarted()
                                                            onClearPath()
                                                            prefs.saveFourierSignals(customFunctionSignals.toList())
                                                        },
                                                        onDel = {
                                                            customFunctionSignals.remove(signal)
                                                            if (customFunctionSignals.isEmpty()) {
                                                                onNextSignalIdChange(0)
                                                            }
                                                        }
                                                    )
                                                }

                                                if (customFunctionSignals.size > 5) {
                                                    TextButton(
                                                        onClick = { onSignalsExpandedChange(!isSignalsExpanded) },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                if (isSignalsExpanded) "Show Less" else "Show All Components (${customFunctionSignals.size})",
                                                                color = colors.accentCyan,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Icon(
                                                                if (isSignalsExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                                                                null,
                                                                tint = colors.accentCyan,
                                                                modifier = Modifier.size(AppDesign.iconTiny)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            "Mathematical Formula",
                                            color = colors.accentCyan,
                                            fontSize = AppDesign.textBody,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(AppDesign.radiusSmall))

                                        OutlinedTextField(
                                            value = formulaString,
                                            onValueChange = {
                                                onFormulaChange(it)
                                                onCalculateDFT()
                                                onClearPath()
                                                onResetTime()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = {
                                                Text(
                                                    "e.g. abs(sin(x))",
                                                    color = colors.textSecondary.copy(0.5f)
                                                )
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(AppDesign.radiusSmall),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colors.accentCyan,
                                                unfocusedBorderColor = colors.cardBorder.copy(0.3f),
                                                cursorColor = colors.accentCyan
                                            )
                                        )

                                        Spacer(Modifier.height(AppDesign.spacingSmall))

                                        Text(
                                            "Use 'x' as variable (0 to 2π). Supported: sin, cos, abs, sqrt, ^, etc.",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            else -> {
                                if (waveType == WaveType.SVG) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = AppDesign.radiusLarge)
                                            .height(AppDesign.buttonHeightSmall)
                                            .clip(RoundedCornerShape(AppDesign.radiusButton))
                                            .background(colors.accentHell.copy(alpha = AppDesign.opacityLow))
                                            .border(
                                                AppDesign.borderThin,
                                                colors.accentHell.copy(alpha = AppDesign.opacityMedium),
                                                RoundedCornerShape(AppDesign.radiusButton)
                                            )
                                            .clickable {
                                                svgPoints.clear()
                                                onClearSVGCoefficients()
                                                onClearPath()
                                                onResetTime()
                                                onRunningChange(false)
                                                onResetHasStarted()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = AppDesign.spacingMedium)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.trash_outline),
                                                null,
                                                tint = colors.accentHell,
                                                modifier = Modifier.size(AppDesign.iconSmall)
                                            )
                                            Spacer(Modifier.width(AppDesign.spacingSmall))
                                            Text(
                                                "Clear SVG",
                                                fontSize = AppDesign.textSmall,
                                                color = colors.accentHell,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(AppDesign.radiusLarge))

                    LabeledSlider(
                        label = "Animation Speed",
                        valueDisplay = String.format(Locale.US, "%.1fx", speed),
                        value = speed,
                        range = 0.1f..3f,
                        colors = colors
                    ) { onSpeedChange(it) }

                    AnimatedVisibility(visible = displayMode == FourierDisplayMode.WRAPPING) {
                        LabeledSlider(
                            label = "Winding Frequency",
                            valueDisplay = String.format(
                                Locale.US,
                                "%.2f Hz",
                                windingFrequency
                            ),
                            value = windingFrequency,
                            range = 0.1f..5f,
                            colors = colors
                        ) { onWindingFrequencyChange(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun SignalSettingsCard(
    signal: SignalInstance,
    colors: AppColors,
    showDel: Boolean,
    onParameterChange: () -> Unit,
    onDel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(
            AppDesign.borderThin,
            colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
            RoundedCornerShape(AppDesign.radiusCard)
        ),
        shape = RoundedCornerShape(AppDesign.radiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface.copy(alpha = 0.3f)),
//        border = BorderStroke(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(AppDesign.spacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(AppDesign.coloredIndicator)
                            .background(signal.color,
                            RoundedCornerShape(100.dp)),
                    )
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(
                        "Component #${signal.id}",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    if (showDel) {
                        IconButton(
                            onClick = onDel,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.trash_outline),
                                null,
                                tint = colors.accentHell.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer( modifier = Modifier.width(AppDesign.spacingSmall))
                    IconButton(
                        onClick = { signal.isExpanded = !signal.isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (signal.isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                            null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = signal.isExpanded) {
                Column(modifier = Modifier.padding(top = AppDesign.spacingSmall)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(AppDesign.chipHeight + 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                    ) {
                        SignalField(
                            label = "Freq (Hz)",
                            value = signal.freq,
                            colors = colors,
                            onValueChange = {
                                signal.freq = it
                                onParameterChange()
                            }
                        )
                        SignalField(
                            label = "Amp (px)",
                            value = signal.amp,
                            colors = colors,
                            onValueChange = {
                                signal.amp = it
                                onParameterChange()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SignalField(
    label: String,
    value: String,
    colors: AppColors,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(label, color = colors.textSecondary, fontSize = AppDesign.textOverline)
        Spacer( modifier = Modifier.height( AppDesign.spacingSmall))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(colors.accentCyan),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.cardSurface.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusCard))
                .border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.1f), RoundedCornerShape(AppDesign.radiusCard))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
