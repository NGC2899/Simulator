package com.example.matharium.fourier

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher

@Composable
fun WaveTypeSelector(
    state: FourierState,
    svgPickerLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    val colors = state.colors
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
            if (type == WaveType.MY_SIGNAL_2D || type == WaveType.FORMULA) return@forEach
            val selected = (state.waveType == type) ||
                    (type == WaveType.MY_SIGNAL && state.waveType == WaveType.MY_SIGNAL_2D) ||
                    (type == WaveType.PURE_SIGNAL && state.waveType == WaveType.FORMULA)

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
                        state.waveType = type
                        state.path.clear()
                        if (type == WaveType.SVG) {
                            state.running = false
                            state.time = 0f
                            svgPickerLauncher.launch("image/svg+xml")
                        }
                        if (type == WaveType.MY_SIGNAL && state.drawingPoints.isEmpty()) {
                            repeat(state.samplesCount) { state.drawingPoints.add(0f) }
                        }
                    }
                    .padding(horizontal = AppDesign.spacingMedium),
                contentAlignment = Alignment.Center
            ) {
                val labelText = when (type) {
                    WaveType.MY_SIGNAL -> "Draw"
                    WaveType.PURE_SIGNAL -> "Custom"
                    WaveType.SVG -> "Import SVG"
                    else -> type.name.lowercase().replaceFirstChar { it.titlecase(Locale.US) }
                }
                Text(
                    labelText,
                    color = if (selected) colors.accentCyan else colors.textSecondary,
                    fontSize = AppDesign.textBody,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DrawingCanvas(state: FourierState) {
    val colors = state.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(AppDesign.chipHeight + 8.dp),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("1D Drawing" to WaveType.MY_SIGNAL, "2D Drawing" to WaveType.MY_SIGNAL_2D).forEach { (label, type) ->
                val selected = state.waveType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(AppDesign.chipHeight)
                        .clip(RoundedCornerShape(AppDesign.radiusSmall))
                        .background(if (selected) colors.accentCyan.copy(0.1f) else Color.Transparent)
                        .border(AppDesign.borderThin, if (selected) colors.accentCyan else colors.cardBorder.copy(0.3f), RoundedCornerShape(AppDesign.radiusSmall))
                        .clickable { state.waveType = type; state.path.clear() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) colors.accentCyan else colors.textSecondary, fontSize = AppDesign.textSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(AppDesign.spacingMedium))
        Text("Draw your wave below", color = colors.accentCyan, fontSize = AppDesign.textBody, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(AppDesign.radiusSmall))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDesign.drawingAreaHeight)
                .background(colors.cardSurface.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusSmall))
                .border(BorderStroke(AppDesign.borderThin, Brush.linearGradient(listOf(colors.accentCyan, colors.accentViolet))), RoundedCornerShape(AppDesign.radiusSmall))
                .pointerInput(state.waveType) {
                    if (state.waveType == WaveType.MY_SIGNAL) {
                        var lastIndex = -1
                        var lastY = 0f
                        detectDragGestures(
                            onDragStart = { offset ->
                                state.running = false
                                state.path.clear()
                                state.time = 0f
                                lastIndex = ((offset.x / size.width.toFloat()) * state.samplesCount).toInt().coerceIn(0, state.samplesCount - 1)
                                lastY = (offset.y - size.height / 2f).coerceIn(-size.height / 2f, size.height / 2f)
                                state.drawingPoints[lastIndex] = lastY
                            },
                            onDrag = { change, _ ->
                                val x = change.position.x
                                val y = (change.position.y - size.height / 2f).coerceIn(-size.height / 2f, size.height / 2f)
                                val currentIndex = ((x / size.width.toFloat()) * state.samplesCount).toInt().coerceIn(0, state.samplesCount - 1)
                                if (lastIndex != -1) {
                                    val start = minOf(lastIndex, currentIndex)
                                    val end = maxOf(lastIndex, currentIndex)
                                    for (i in start..end) {
                                        val t = if (end == start) 1f else (i - lastIndex).toFloat() / (currentIndex - lastIndex)
                                        state.drawingPoints[i] = (lastY + (y - lastY) * t).coerceIn(-size.height / 2f, size.height / 2f)
                                    }
                                }
                                lastIndex = currentIndex
                                lastY = y
                                state.calculateDFT()
                            },
                            onDragEnd = { state.prefs.drawingPoints = state.drawingPoints.toList() }
                        )
                    } else {
                        detectDragGestures(
                            onDragStart = { offset ->
                                state.running = false
                                state.path.clear()
                                state.time = 0f
                                state.drawingPoints2D.clear()
                                state.drawingPoints2D.add(offset - Offset(size.width / 2f, size.height / 2f))
                            },
                            onDrag = { change, _ ->
                                val halfWidth = size.width / 2f
                                val halfHeight = size.height / 2f
                                state.drawingPoints2D.add(Offset((change.position.x - halfWidth).coerceIn(-halfWidth, halfWidth), (change.position.y - halfHeight).coerceIn(-halfHeight, halfHeight)))
                            },
                            onDragEnd = { state.prefs.drawingPoints2D = state.drawingPoints2D.toList() }
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                if (state.waveType == WaveType.MY_SIGNAL) {
                    drawLine(colors.textSecondary.copy(alpha = 0.2f), Offset(0f, h / 2), Offset(w, h / 2), AppDesign.strokeThin.toPx())
                    if (state.drawingPoints.size == state.samplesCount) {
                        val p = Path()
                        for (i in 0 until state.samplesCount) {
                            val px = i.toFloat() / state.samplesCount * w
                            val py = h / 2 + state.drawingPoints[i]
                            if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                        }
                        drawPath(p, colors.accentCyan, style = Stroke(AppDesign.borderStandard.toPx()))
                    }
                } else {
                    val gridColor = colors.textSecondary.copy(alpha = 0.1f)
                    drawLine(gridColor, Offset(w / 2, 0f), Offset(w / 2, h), 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), 1.dp.toPx())
                    if (state.drawingPoints2D.isNotEmpty()) {
                        val p = Path()
                        val center = Offset(w / 2f, h / 2f)
                        p.moveTo(state.drawingPoints2D[0].x + center.x, state.drawingPoints2D[0].y + center.y)
                        for (i in 1 until state.drawingPoints2D.size) { p.lineTo(state.drawingPoints2D[i].x + center.x, state.drawingPoints2D[i].y + center.y) }
                        drawPath(p, colors.accentCyan, style = Stroke(AppDesign.borderStandard.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
        }

        Box(
            modifier = Modifier.padding(top = AppDesign.spacingMedium).height(AppDesign.buttonHeightSmall).clip(RoundedCornerShape(AppDesign.radiusButton))
                .background(colors.accentHell.copy(alpha = AppDesign.opacityLow)).border(AppDesign.borderThin, colors.accentHell.copy(alpha = AppDesign.opacityMedium), RoundedCornerShape(AppDesign.radiusButton))
                .clickable {
                    if (state.waveType == WaveType.MY_SIGNAL) state.clearDrawing()
                    else if (state.waveType == WaveType.MY_SIGNAL_2D) state.clearDrawing2D()
                    else if (state.waveType == WaveType.SVG) state.clearSVG()
                    state.running = false
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = AppDesign.spacingMedium)) {
                Icon(painter = painterResource(id = R.drawable.trash_outline), null, tint = colors.accentHell, modifier = Modifier.size(AppDesign.iconSmall))
                Spacer(Modifier.width(AppDesign.spacingSmall))
                Text("Clear Drawing", fontSize = AppDesign.textSmall, color = colors.accentHell, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CustomSignalSettings(state: FourierState) {
    val colors = state.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(AppDesign.chipHeight + 8.dp),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f).height(AppDesign.chipHeight).clip(RoundedCornerShape(AppDesign.radiusSmall))
                    .background(if (state.waveType == WaveType.PURE_SIGNAL) colors.accentCyan.copy(0.1f) else Color.Transparent)
                    .border(AppDesign.borderThin, if (state.waveType == WaveType.PURE_SIGNAL) colors.accentCyan else colors.cardBorder.copy(0.3f), RoundedCornerShape(AppDesign.radiusSmall))
                    .clickable { state.waveType = WaveType.PURE_SIGNAL; state.path.clear() },
                contentAlignment = Alignment.Center
            ) {
                Text("Signal", color = if (state.waveType == WaveType.PURE_SIGNAL) colors.accentCyan else colors.textSecondary, fontSize = AppDesign.textSmall, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier.weight(1f).height(AppDesign.chipHeight).clip(RoundedCornerShape(AppDesign.radiusSmall))
                    .background(if (state.waveType == WaveType.FORMULA) colors.accentCyan.copy(0.1f) else Color.Transparent)
                    .border(AppDesign.borderThin, if (state.waveType == WaveType.FORMULA) colors.accentCyan else colors.cardBorder.copy(0.3f), RoundedCornerShape(AppDesign.radiusSmall))
                    .clickable { state.waveType = WaveType.FORMULA; state.path.clear() },
                contentAlignment = Alignment.Center
            ) {
                Text("Formula", color = if (state.waveType == WaveType.FORMULA) colors.accentCyan else colors.textSecondary, fontSize = AppDesign.textSmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(AppDesign.spacingMedium))

        if (state.waveType == WaveType.PURE_SIGNAL) {
            Text("Define your signal components", color = colors.accentCyan, fontSize = AppDesign.textBody, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(AppDesign.radiusSmall))
            Row(modifier = Modifier.fillMaxWidth().height(AppDesign.buttonHeightSmall), horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(AppDesign.radiusButton))
                        .border(BorderStroke(AppDesign.borderThin, Brush.linearGradient(listOf(colors.accentCyan, colors.accentViolet))), RoundedCornerShape(AppDesign.radiusButton))
                        .clickable {
                            val last = state.customFunctionSignals.lastOrNull()
                            state.customFunctionSignals.add(SignalInstance(state.nextSignalId, Color.hsv(kotlin.random.Random.nextFloat() * 360f, 0.7f, 0.9f), last?.freq ?: "1.0", last?.amp ?: "0.5", last?.phase ?: "0.0"))
                            state.nextSignalId++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(id = R.drawable.add_outline), null, modifier = Modifier.size(AppDesign.iconSmall), tint = colors.accentCyan)
                        Spacer(Modifier.width(AppDesign.spacingSmall))
                        Text("Add Component", fontSize = AppDesign.textBody, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.weight(0.5f).fillMaxHeight().clip(RoundedCornerShape(AppDesign.radiusButton)).background(colors.accentHell.copy(alpha = 0.1f))
                        .border(BorderStroke(AppDesign.borderThin, colors.accentHell.copy(alpha = 0.3f)), RoundedCornerShape(AppDesign.radiusButton))
                        .clickable { state.customFunctionSignals.clear(); state.nextSignalId = 0; state.running = false; state.path.clear() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(id = R.drawable.trash_outline), null, tint = colors.accentHell, modifier = Modifier.size(AppDesign.iconSmall))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear", fontSize = AppDesign.textBody, color = colors.accentHell, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.customFunctionSignals.isNotEmpty()) {
                Spacer(Modifier.height(AppDesign.spacingMedium))
                Column(verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)) {
                    val displayList = if (state.isSignalsExpanded) state.customFunctionSignals else state.customFunctionSignals.take(5)
                    displayList.forEach { signal ->
                        SignalSettingsCard(signal = signal, colors = colors, showDel = state.customFunctionSignals.size > 1, onParameterChange = { state.path.clear(); state.prefs.saveFourierSignals(state.customFunctionSignals.toList()) }, onDel = { state.customFunctionSignals.remove(signal); state.removedHarmonics.clear(); state.prefs.saveFourierSignals(state.customFunctionSignals.toList()); if (state.customFunctionSignals.isEmpty()) state.nextSignalId = 0 })
                    }
                    if (state.customFunctionSignals.size > 5) {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = AppDesign.spacingSmall).clip(RoundedCornerShape(AppDesign.radiusSmall)).background(colors.accentCyan.copy(alpha = 0.05f)).border(BorderStroke(AppDesign.borderThin, colors.accentCyan.copy(alpha = 0.1f)), RoundedCornerShape(AppDesign.radiusSmall)).clickable { state.isSignalsExpanded = !state.isSignalsExpanded }, contentAlignment = Alignment.Center) {
                            Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(if (state.isSignalsExpanded) "Show Less" else "Show All Components (${state.customFunctionSignals.size})", color = colors.accentCyan, fontWeight = FontWeight.Bold, fontSize = AppDesign.textBody)
                                Spacer(Modifier.width(AppDesign.spacingSmall))
                                Icon(if (state.isSignalsExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline), null, tint = colors.accentCyan, modifier = Modifier.size(AppDesign.spacingMedium))
                            }
                        }
                    }
                }
            }
        } else {
            Text("Mathematical Formula", color = colors.accentCyan, fontSize = AppDesign.textBody, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(AppDesign.radiusSmall))
            OutlinedTextField(value = state.formulaString, onValueChange = { state.formulaString = it; state.calculateDFT(); state.path.clear(); state.time = 0f }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. abs(sin(x))", color = colors.textSecondary.copy(0.5f)) }, singleLine = true, shape = RoundedCornerShape(AppDesign.radiusSmall), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.accentCyan, unfocusedBorderColor = colors.cardBorder.copy(0.3f), cursorColor = colors.accentCyan))
            Spacer(Modifier.height(AppDesign.spacingSmall))
            SymmetryMessage(result = state.symmetryResult, colors = colors)
            Text("Use 'x' as variable (-π to π). Supported: sin, cos, abs, sqrt, ^, etc.", color = colors.textSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun SVGSettings(
    state: FourierState,
    svgPickerLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    val colors = state.colors
    val density = androidx.compose.ui.platform.LocalContext.current.resources.displayMetrics.density
    Column {
        Text("SVG Path Preview", color = colors.accentCyan, fontSize = AppDesign.textBody, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(AppDesign.radiusSmall))
        Box(modifier = Modifier.fillMaxWidth().height(AppDesign.drawingAreaHeight).background(colors.cardSurface.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusSmall)).border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.3f), RoundedCornerShape(AppDesign.radiusSmall))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gridColor = colors.textSecondary.copy(alpha = 0.1f)
                drawLine(gridColor, Offset(w / 2, 0f), Offset(w / 2, h), 1.dp.toPx())
                drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), 1.dp.toPx())
                if (state.svgPoints.isNotEmpty()) {
                    val p = Path()
                    val center = Offset(w / 2f, h / 2f)
                    val previewScale = 60 * density
                    p.moveTo(state.svgPoints[0].x * previewScale + center.x, -state.svgPoints[0].y * previewScale + center.y)
                    for (i in 1 until state.svgPoints.size) { p.lineTo(state.svgPoints[i].x * previewScale + center.x, -state.svgPoints[i].y * previewScale + center.y) }
                    drawPath(p, colors.accentCyan, style = Stroke(AppDesign.borderStandard.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
        Spacer(Modifier.height(AppDesign.spacingMedium))
        
        Row(horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium)) {
            // Import/Change button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeightSmall)
                    .clip(RoundedCornerShape(AppDesign.radiusButton))
                    .background(colors.accentCyan.copy(alpha = AppDesign.opacityLow))
                    .border(AppDesign.borderThin, colors.accentCyan.copy(alpha = AppDesign.opacityMedium), RoundedCornerShape(AppDesign.radiusButton))
                    .clickable { svgPickerLauncher.launch("image/svg+xml") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = AppDesign.spacingMedium)) {
                    Icon(painter = painterResource(id = R.drawable.cloud), null, tint = colors.accentCyan, modifier = Modifier.size(AppDesign.iconSmall))
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(if (state.svgPoints.isEmpty()) "Import SVG" else "Change SVG", fontSize = AppDesign.textSmall, color = colors.accentCyan, fontWeight = FontWeight.Bold)
                }
            }

            // Clear button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeightSmall)
                    .clip(RoundedCornerShape(AppDesign.radiusButton))
                    .background(colors.accentHell.copy(alpha = AppDesign.opacityLow))
                    .border(AppDesign.borderThin, colors.accentHell.copy(alpha = AppDesign.opacityMedium), RoundedCornerShape(AppDesign.radiusButton))
                    .clickable { state.clearSVG() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = AppDesign.spacingMedium)) {
                    Icon(painter = painterResource(id = R.drawable.trash_outline), null, tint = colors.accentHell, modifier = Modifier.size(AppDesign.iconSmall))
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text("Clear SVG", fontSize = AppDesign.textSmall, color = colors.accentHell, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SimulatorEnvironmentSettings(state: FourierState) {
    val colors = state.colors
    var isExpanded by remember { mutableStateOf(false) }
    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(AppDesign.spacingLarge).animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Simulator Settings", fontSize = AppDesign.textHeadline, fontWeight = FontWeight.Bold)
                Icon(if (isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline), null, tint = colors.textSecondary, modifier = Modifier.size(AppDesign.iconSmall))
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(AppDesign.spacingLarge))
                    LabeledSlider(label = "Animation Speed", valueDisplay = String.format(Locale.US, "%.1fx", state.speed), value = state.speed, range = 0.1f..3f, colors = colors) { state.speed = it }
                    AnimatedVisibility(visible = state.displayMode == FourierDisplayMode.CIRCULAR) {
                        LabeledSlider(label = "Wave Stretch", valueDisplay = String.format(Locale.US, "%.0f dp", state.waveStretch), value = state.waveStretch, range = 30f..300f, colors = colors) { state.waveStretch = it }
                    }
                    ToggleRow(label = "Enable error gradient (Doesn't function properly!)", checked = state.showErrorGradient, onCheckedChange = { state.showErrorGradient = it }, colors = colors)
                    AnimatedVisibility(visible = state.showErrorGradient) {
                        LabeledSlider(label = "Error Sensitivity", valueDisplay = String.format(Locale.US, "%.0f%%", state.errorSensitivity), value = state.errorSensitivity, range = 1f..100f, colors = colors) { state.errorSensitivity = it }
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
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = AppDesign.spacingSmall), colors = CardDefaults.cardColors(containerColor = colors.accentCyan.copy(alpha = 0.08f)), border = BorderStroke(1.dp, colors.accentCyan.copy(alpha = 0.2f)), shape = RoundedCornerShape(AppDesign.radiusSmall)) {
            Row(modifier = Modifier.padding(AppDesign.spacingMedium), verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.cloud), null, tint = colors.accentCyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(AppDesign.spacingSmall))
                Text(text = message, color = colors.textPrimary, fontSize = AppDesign.textSmall, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SignalSettingsCard(signal: SignalInstance, colors: AppColors, showDel: Boolean, onParameterChange: () -> Unit, onDel: () -> Unit) {
    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppDesign.spacingMedium).animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth().clickable { signal.isExpanded = !signal.isExpanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(AppDesign.coloredIndicator).background(signal.color, CircleShape).border(1.dp, signal.color.copy(alpha = 0.4f), CircleShape))
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text("Component #${signal.id}", color = colors.textPrimary, fontSize = AppDesign.textHeadline, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { signal.isPaused = !signal.isPaused; onParameterChange() }, modifier = Modifier.size(AppDesign.iconLarge + 4.dp)) { Icon(painter = if (signal.isPaused) painterResource(id = R.drawable.caret_forward_outline) else painterResource(id = R.drawable.pause_outline), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(AppDesign.iconSmallMedium)) }
                    if (showDel) {
                        Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                        IconButton(onClick = onDel, modifier = Modifier.size(AppDesign.iconLarge + 4.dp)) { Icon(painterResource(id = R.drawable.trash_outline), null, tint = colors.accentHell.copy(alpha = 0.8f), modifier = Modifier.size(AppDesign.iconSmallMedium)) }
                    }
                    Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                    Icon(if (signal.isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline), null, tint = colors.textSecondary, modifier = Modifier.size(AppDesign.iconSmall))
                }
            }
            AnimatedVisibility(visible = signal.isExpanded) {
                Column(modifier = Modifier.padding(top = AppDesign.spacingMedium)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium)) {
                        SignalField(label = "Freq", unit = "Hz", icon = painterResource(id = R.drawable.caret_forward_outline), value = signal.freq, colors = colors, onValueChange = { signal.freq = it; signal.updateCache(); onParameterChange() })
                        SignalField(label = "Amp", unit = "", icon = painterResource(id = R.drawable.add_outline), value = signal.amp, colors = colors, onValueChange = { signal.amp = it; signal.updateCache(); onParameterChange() })
                        SignalField(label = "Phase", unit = "°", icon = rememberVectorPainter(Icons.Default.Refresh), value = signal.phase, colors = colors, onValueChange = { signal.phase = it; signal.updateCache(); onParameterChange() })
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SignalField(label: String, unit: String, icon: Painter, value: String, colors: AppColors, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text("$label ($unit)", color = colors.textSecondary, fontSize = AppDesign.textOverline, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(AppDesign.spacingSmall))
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold), cursorBrush = SolidColor(colors.accentCyan), modifier = Modifier.fillMaxWidth().background(colors.cardSurface.copy(alpha = 0.15f), RoundedCornerShape(AppDesign.radiusSmall)).border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusSmall)).padding(horizontal = AppDesign.spacingSmall, vertical = AppDesign.spacingSmall))
    }
}
