package com.example.matharium.fourier

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
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
    customFunctionSignals: MutableList<SignalInstance>,
    onCalculateDFT: () -> Unit,
    onCalculateDFT2D: () -> Unit,
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
        Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
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
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppDesign.radiusSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WaveType.entries.forEach { type ->
                            if (type == WaveType.MY_SIGNAL_2D) return@forEach // Skip MY_SIGNAL_2D as it's merged with MY_SIGNAL
                            val selected = (waveType == type) || (type == WaveType.MY_SIGNAL && waveType == WaveType.MY_SIGNAL_2D)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onWaveTypeChange(type)
                                    onClearPath()
                                    if (type == WaveType.SVG) {
                                        svgPickerLauncher.launch("image/svg+xml")
                                    }
                                    if (type == WaveType.MY_SIGNAL && drawingPoints.isEmpty()) {
                                        repeat(samplesCount) { drawingPoints.add(0f) }
                                    }
                                },
                                label = {
                                    val labelText = when (type) {
                                        WaveType.MY_SIGNAL -> "Draw"
                                        WaveType.CUSTOM_FUNCTION -> "Custom Function"
                                        WaveType.SVG -> "Import SVG"
                                        else -> type.name.lowercase().replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
                                        }
                                    }
                                    Text(labelText)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedLabelColor = colors.accentCyan,
                                    selectedContainerColor = colors.accentCyan.copy(0.0f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION || type == WaveType.SVG) && selected) Color.Transparent else colors.cardBorder.copy(
                                        alpha = AppDesign.opacityMedium
                                    ),
                                    selectedBorderColor = if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION || type == WaveType.SVG) && selected) Color.Transparent else colors.accentCyan,
                                    borderWidth = AppDesign.borderThin,
                                    selectedBorderWidth = if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION || type == WaveType.SVG) && selected) AppDesign.borderStandard else AppDesign.borderThin
                                ),
                                modifier =
                                    if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION || type == WaveType.SVG) && selected) {
                                        Modifier
                                            .border(
                                                AppDesign.borderStandard,
                                                Brush.linearGradient(
                                                    listOf(
                                                        colors.accentCyan,
                                                        colors.accentViolet
                                                    )
                                                ),
                                                RoundedCornerShape(AppDesign.radiusSmall)
                                            )
                                            .height(AppDesign.chipHeight)
                                    } else Modifier
                            )
                        }
                    }

                    AnimatedVisibility(visible = waveType == WaveType.MY_SIGNAL || waveType == WaveType.MY_SIGNAL_2D) {
                        Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(AppDesign.radiusSmall))
                                        .background(if (waveType == WaveType.MY_SIGNAL) colors.accentCyan.copy(0.1f) else Color.Transparent)
                                        .border(
                                            1.dp,
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
                                            1.dp,
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
                                        AppDesign.borderStandard,
                                        colors.accentHell.copy(alpha = AppDesign.opacityMedium),
                                        RoundedCornerShape(AppDesign.radiusButton)
                                    )
                                    .clickable {
                                        if (waveType == WaveType.MY_SIGNAL) {
                                            drawingPoints.clear()
                                            repeat(samplesCount) { drawingPoints.add(0f) }
                                            prefs.drawingPoints = emptyList<Float>()
                                        } else {
                                            drawingPoints2D.clear()
                                            prefs.drawingPoints2D = emptyList<Offset>()
                                        }
                                        onClearPath()
                                        onResetTime()
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

                    AnimatedVisibility(visible = waveType == WaveType.CUSTOM_FUNCTION) {
                        Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
                            Text(
                                "Define your signal components",
                                color = colors.accentCyan,
                                fontSize = AppDesign.textBody,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(AppDesign.radiusSmall))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AppDesign.buttonHeightSmall)
                                        .clip(RoundedCornerShape(AppDesign.radiusButton))
                                        .background(colors.cardSurface.copy(AppDesign.opacityLow))
                                        .border(
                                            BorderStroke(
                                                2.dp,
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
                                            val color = Color.hsv(kotlin.random.Random.nextFloat() * 360f, 0.7f, 0.9f)
                                            customFunctionSignals.add(SignalInstance(nextSignalId, color, nextFreq, nextAmp))
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
                                            2.dp,
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
                                    val displayList = if (isSignalsExpanded) customFunctionSignals else customFunctionSignals.take(5)
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
                                                    modifier = Modifier.size( AppDesign.iconTiny)
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
