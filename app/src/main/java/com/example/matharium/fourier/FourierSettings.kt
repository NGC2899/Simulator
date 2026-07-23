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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.painter.Painter
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
    showErrorGradient: Boolean,
    onShowErrorGradientChange: (Boolean) -> Unit,
    errorSensitivity: Float,
    onErrorSensitivityChange: (Float) -> Unit,
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
    symmetryResult: FourierLogic.SymmetryResult?,
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
                                                    AppDesign.strokeThin.toPx()
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
                                                drawLine(gridColor, Offset(w / 2, 0f), Offset(w / 2, h), 1.dp.toPx())
                                                drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), 1.dp.toPx())

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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(AppDesign.buttonHeightSmall),
                                            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable {
                                                        val last = customFunctionSignals.lastOrNull()
                                                        val nextFreq = last?.freq ?: "1.0"
                                                        val nextAmp = last?.amp ?: "0.5"
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
                                                shape = RoundedCornerShape(AppDesign.radiusButton),
                                                color = colors.accentCyan.copy(alpha = 0.1f),
                                                border = BorderStroke(
                                                    1.dp,
                                                    Brush.linearGradient(
                                                        listOf(colors.accentCyan, colors.accentViolet)
                                                    )
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.add_outline),
                                                        null,
                                                        modifier = Modifier.size(AppDesign.iconSmall),
                                                        tint = colors.accentCyan
                                                    )
                                                    Spacer(Modifier.width(AppDesign.spacingSmall))
                                                    Text(
                                                        "Add Component",
                                                        fontSize = 12.sp,
                                                        color = colors.textPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Surface(
                                                modifier = Modifier
                                                    .weight(0.5f)
                                                    .fillMaxHeight()
                                                    .clickable {
                                                        customFunctionSignals.clear()
                                                        onNextSignalIdChange(0)
                                                        onRunningChange(false)
                                                        onResetHasStarted()
                                                        onClearPath()
                                                    },
                                                shape = RoundedCornerShape(AppDesign.radiusButton),
                                                color = colors.accentHell.copy(alpha = 0.1f),
                                                border = BorderStroke(
                                                    1.dp,
                                                    colors.accentHell.copy(alpha = 0.3f)
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.trash_outline),
                                                        null,
                                                        tint = colors.accentHell,
                                                        modifier = Modifier.size(AppDesign.iconSmall)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        "Clear",
                                                        fontSize = 12.sp,
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
                                                    Surface(
                                                        onClick = { onSignalsExpandedChange(!isSignalsExpanded) },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = AppDesign.spacingSmall),
                                                        shape = RoundedCornerShape(AppDesign.radiusSmall),
                                                        color = colors.accentCyan.copy(alpha = 0.05f),
                                                        border = BorderStroke(1.dp, colors.accentCyan.copy(alpha = 0.1f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(vertical = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(
                                                                if (isSignalsExpanded) "Show Less" else "Show All Components (${customFunctionSignals.size})",
                                                                color = colors.accentCyan,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp
                                                            )
                                                            Spacer(Modifier.width(AppDesign.spacingSmall))
                                                            Icon(
                                                                if (isSignalsExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                                                                null,
                                                                tint = colors.accentCyan,
                                                                modifier = Modifier.size(14.dp)
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

                                        SymmetryMessage(result = symmetryResult, colors = colors)

                                        Text(
                                            "Use 'x' as variable (-π to π). Supported: sin, cos, abs, sqrt, ^, etc.",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            else -> {
                                if (waveType == WaveType.SVG) {
                                    Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
                                        Text(
                                            "SVG Path Preview",
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
                                                    AppDesign.borderThin,
                                                    colors.cardBorder.copy(alpha = 0.3f),
                                                    RoundedCornerShape(AppDesign.radiusSmall)
                                                )
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val w = size.width
                                                val h = size.height
                                                val gridColor = colors.textSecondary.copy(alpha = 0.1f)
                                                drawLine(gridColor, Offset(w / 2, 0f), Offset(w / 2, h), 1.dp.toPx())
                                                drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), 1.dp.toPx())

                                                if (svgPoints.isNotEmpty()) {
                                                    val p = Path()
                                                    val center = Offset(w / 2f, h / 2f)
                                                    p.moveTo(svgPoints[0].x + center.x, svgPoints[0].y + center.y)
                                                    for (i in 1 until svgPoints.size) {
                                                        p.lineTo(svgPoints[i].x + center.x, svgPoints[i].y + center.y)
                                                    }
                                                    drawPath(
                                                        p,
                                                        colors.accentCyan,
                                                        style = Stroke(AppDesign.borderStandard.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(AppDesign.spacingMedium))

                                        Box(
                                            modifier = Modifier
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
                                                    prefs.fourierSvgPoints = emptyList()
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
                    }

                    Spacer(Modifier.height(AppDesign.radiusLarge))

                    LabeledSlider(
                        label = "Animation Speed",
                        valueDisplay = String.format(Locale.US, "%.1fx", speed),
                        value = speed,
                        range = 0.1f..3f,
                        colors = colors
                    ) { onSpeedChange(it) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppDesign.spacingSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ToggleRow(
                            label = "Enable error gradient",
                            checked = showErrorGradient,
                            onCheckedChange = { onShowErrorGradientChange(it) },
                            colors = colors
                        )
                    }

                    AnimatedVisibility(visible = showErrorGradient) {
                        LabeledSlider(
                            label = "Error Sensitivity",
                            valueDisplay = String.format(Locale.US, "%.0f%%", errorSensitivity),
                            value = errorSensitivity,
                            range = 1f..100f,
                            colors = colors
                        ) { onErrorSensitivityChange(it) }
                    }

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
fun SymmetryMessage(result: FourierLogic.SymmetryResult?, colors: AppColors) {
    if (result == null) return
    
    val message = when {
        result.evenPercent > 99.5f -> "This function is symmetric and is even therefore only cosine coefficients exist."
        result.oddPercent > 99.5f -> "This function is symmetric and is odd therefore only sine coefficients exist."
        result.evenPercent > 85f -> "This function is ${result.evenPercent.toInt()}% even so we can safely ignore sine coefficients."
        result.oddPercent > 85f -> "This function is ${result.oddPercent.toInt()}% odd so we can safely ignore cosine coefficients."
        else -> null
    }

    if (message != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppDesign.spacingSmall),
            colors = CardDefaults.cardColors(containerColor = colors.accentCyan.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, colors.accentCyan.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(AppDesign.radiusSmall)
        ) {
            Row(
                modifier = Modifier.padding(AppDesign.spacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cloud), // Or any suitable info icon
                    contentDescription = null,
                    tint = colors.accentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(AppDesign.spacingSmall))
                Text(
                    text = message,
                    color = colors.textPrimary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
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
    GlassCard(
        colors = colors,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(AppDesign.spacingMedium)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { signal.isExpanded = !signal.isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppDesign.coloredIndicator)
                            .background(
                                signal.color,
                                CircleShape
                            )
                            .border(1.dp, signal.color.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(
                        "Component #${signal.id}",
                        color = colors.textPrimary,
                        fontSize = AppDesign.textHeadline,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { signal.isPaused = !signal.isPaused; onParameterChange() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = if (signal.isPaused) painterResource(id = R.drawable.caret_forward_outline) else painterResource(id = R.drawable.pause_outline),
                            contentDescription = if (signal.isPaused) "Resume" else "Pause",
                            tint = if (signal.isPaused) colors.accentCyan else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (showDel) {
                        Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                        IconButton(
                            onClick = onDel,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.trash_outline),
                                null,
                                tint = colors.accentHell.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                    Icon(
                        if (signal.isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                        null,
                        tint = colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = signal.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = AppDesign.spacingMedium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium)
                    ) {
                        SignalField(
                            label = "Freq",
                            unit = "Hz",
                            icon = painterResource(id = R.drawable.caret_forward_outline),
                            value = signal.freq,
                            colors = colors,
                            onValueChange = {
                                signal.freq = it
                                onParameterChange()
                            }
                        )
                        SignalField(
                            label = "Amp",
                            unit = "",
                            icon = painterResource(id = R.drawable.add_outline),
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
    unit: String,
    icon: Painter,
    value: String,
    colors: AppColors,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(10.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "$label ($unit)",
                color = colors.textSecondary,
                fontSize = AppDesign.textOverline,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(AppDesign.spacingSmall))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(colors.accentCyan),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.cardSurface.copy(alpha = 0.15f), RoundedCornerShape(AppDesign.radiusSmall))
                .border(
                    AppDesign.borderThin,
                    colors.cardBorder.copy(alpha = 0.2f),
                    RoundedCornerShape(AppDesign.radiusSmall)
                )
                .padding(horizontal = AppDesign.spacingSmall, vertical = 10.dp)
        )
    }
}
