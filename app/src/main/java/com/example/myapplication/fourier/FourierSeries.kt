package com.example.myapplication.fourier

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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.app.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class WaveType {
    MY_SIGNAL, CUSTOM_FUNCTION, SQUARE, TRIANGLE, SINE, SAWTOOTH
}

enum class FourierDisplayMode {
    CIRCULAR, WRAPPING, COMPLEX
}

class SignalInstance(
    val id: Int,
    var color: Color,
    initialFreq: String = "1.0",
    initialAmp: String = "50.0"
) {
    var freq by mutableStateOf(initialFreq)
    var amp by mutableStateOf(initialAmp)
    var isExpanded by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourierSeries() {
    val colors = LocalAppColors.current
    val prefs = LocalAppPrefs.current

    var nTerms by remember { mutableIntStateOf(prefs.fourierNTerms) }
    var waveType by remember { mutableStateOf(WaveType.SQUARE) }
    var running by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var isSettingsExpanded by remember { mutableStateOf(true) }
    var displayMode by remember { mutableStateOf(FourierDisplayMode.CIRCULAR) }
    var windingFrequency by remember { mutableFloatStateOf(1.0f) }

    var time by remember { mutableFloatStateOf(0f) }
    val path = remember { mutableStateListOf<Offset>() }

    // --- Draw a Wave State ---
    val samplesCount = 200
    val drawingPoints = remember { 
        val saved = prefs.drawingPoints
        val list = mutableStateListOf<Float>()
        if (saved.isNotEmpty()) {
            list.addAll(saved)
        } else {
            repeat(samplesCount) { list.add(0f) }
        }
        list
    }
    var customCoefficients by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }

    // --- Custom Function State ---
    val customFunctionSignals = remember { 
        val list = mutableStateListOf<SignalInstance>()
        list.addAll(prefs.loadFourierSignals(colors.accentCyan))
        list
    }
    var nextSignalId by remember { mutableIntStateOf(customFunctionSignals.maxOfOrNull { it.id }?.plus(1) ?: 1) }
    var isSignalsExpanded by remember { mutableStateOf(false) }

    fun calculateDFT() {
        if (drawingPoints.size < samplesCount) return
        val coeffs = mutableListOf<Pair<Float, Float>>()

        // Calculate Harmonics up to 50
        for (n in 1..50) {
            var re = 0f
            var im = 0f
            for (i in 0 until samplesCount) {
                val angle = 2 * PI.toFloat() * n * i / samplesCount
                re += drawingPoints[i] * cos(angle)
                im += drawingPoints[i] * sin(angle)
            }
            re /= (samplesCount / 2f)
            im /= (samplesCount / 2f)

            val amp = kotlin.math.sqrt(re * re + im * im)
            val phase = kotlin.math.atan2(re, im)
            coeffs.add(amp to phase)
        }
        customCoefficients = coeffs
    }

    // Initialize DFT if drawing points exist
    LaunchedEffect(Unit) {
        if (drawingPoints.any { it != 0f }) {
            calculateDFT()
        }
    }

    // Save state when it changes
    LaunchedEffect(nTerms) { prefs.fourierNTerms = nTerms }
    LaunchedEffect(drawingPoints.toList()) { prefs.drawingPoints = drawingPoints.toList() }
    LaunchedEffect(customFunctionSignals.toList()) { prefs.saveFourierSignals(customFunctionSignals.toList()) }

    LaunchedEffect(
        running,
        speed,
        nTerms,
        waveType,
        customCoefficients,
        customFunctionSignals.size
    ) {
        if (!running) return@LaunchedEffect
        var lastTime = System.nanoTime()
        while (running) {
            withFrameNanos { frameTime ->
                val dt = (frameTime - lastTime) / 1e9f
                lastTime = frameTime
                val substeps = 4
                val subDt = dt / substeps
                val newPoints = mutableListOf<Offset>()

                repeat(substeps) {
                    // Adjust speed to make f=1 equal 1 rotation per second
                    // Angle is 2 * PI * f * time. At f=1, period is 1s if time increases by 1 per second.
                    time += subDt * speed

                    // Calculate current x, y position for the path
                    var currentX = 0f
                    var currentY = 0f
                    val radiusBase = 100f

                    // WaveType.SINE should only have one term regardless of nTerms
                    val activeTerms = if (waveType == WaveType.SINE) 1 else nTerms

                    if (waveType == WaveType.CUSTOM_FUNCTION) {
                        val limit = activeTerms.coerceAtMost(customFunctionSignals.size)
                        for (i in 0 until limit) {
                            val signal = customFunctionSignals[i]
                            val freq = signal.freq.toFloatOrNull() ?: 0f
                            val amp = signal.amp.toFloatOrNull() ?: 0f
                            val angle = 2 * PI.toFloat() * freq * time
                            currentX += amp * cos(angle)
                            currentY += amp * sin(angle)
                        }
                    } else {
                        for (i in 0 until activeTerms) {
                            if (waveType == WaveType.MY_SIGNAL) {
                                if (i < customCoefficients.size) {
                                    val (amp, phase) = customCoefficients[i]
                                    val n = i + 1
                                    val angle = 2 * PI.toFloat() * n * time + phase
                                    currentX += amp * cos(angle)
                                    currentY += amp * sin(angle)
                                }
                                continue
                            }

                            val n = when (waveType) {
                                WaveType.SINE -> 1
                                WaveType.SQUARE -> i * 2 + 1
                                WaveType.SAWTOOTH -> i + 1
                                WaveType.TRIANGLE -> i * 2 + 1
                                else -> 1
                            }
                            val radius = when (waveType) {
                                WaveType.SINE -> radiusBase
                                WaveType.SQUARE -> radiusBase * (4f / (n * PI.toFloat()))
                                WaveType.SAWTOOTH -> radiusBase * (2f / (n * PI.toFloat()))
                                WaveType.TRIANGLE -> radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat()))
                                else -> 0f
                            }
                            val phase =
                                if (waveType == WaveType.TRIANGLE && i % 2 != 0) PI.toFloat() else 0f
                            val angle = 2 * PI.toFloat() * n * time + phase
                            currentX += radius * cos(angle)
                            currentY += radius * sin(angle)
                        }
                    }

                    if (displayMode == FourierDisplayMode.COMPLEX) {
                        newPoints.add(0, Offset(currentX, currentY))
                    } else {
                        newPoints.add(0, Offset(time, currentY))
                    }
                }

                path.addAll(0, newPoints)

                if (path.size > 800) {
                    val itemsToRemove = path.size - 800
                    repeat(itemsToRemove) { path.removeAt(path.size - 1) }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {
        // Settings Card
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
                        if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = colors.textSecondary
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
                                val selected = waveType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        waveType = type
                                        path.clear()
                                        if (type == WaveType.MY_SIGNAL && drawingPoints.isEmpty()) {
                                            // Initialize empty drawing points
                                            repeat(samplesCount) { drawingPoints.add(0f) }
                                        }
                                    },
                                    label = {
                                        val labelText = when (type) {
                                            WaveType.MY_SIGNAL -> "Draw a Wave"
                                            WaveType.CUSTOM_FUNCTION -> "Custom Function"
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
                                        borderColor = if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION) && selected) Color.Transparent else colors.cardBorder.copy(
                                            alpha = AppDesign.opacityMedium
                                        ),
                                        selectedBorderColor = if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION) && selected) Color.Transparent else colors.accentCyan,
                                        borderWidth = AppDesign.borderThin,
                                        selectedBorderWidth = if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION) && selected) AppDesign.borderStandard else AppDesign.borderThin
                                    ),
                                    modifier =
                                        if ((type == WaveType.MY_SIGNAL || type == WaveType.CUSTOM_FUNCTION) && selected) {
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

                        AnimatedVisibility(visible = waveType == WaveType.MY_SIGNAL) {
                            Column(modifier = Modifier.padding(top = AppDesign.radiusLarge)) {
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
                                        .pointerInput(Unit) {
                                            var lastIndex = -1
                                            var lastY = 0f

                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    running = false
                                                    path.clear()
                                                    time = 0f

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
                                                    calculateDFT()
                                                },
                                                onDragEnd = {
                                                    prefs.drawingPoints = drawingPoints.toList()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
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
                                            drawingPoints.clear()
                                            repeat(samplesCount) { drawingPoints.add(0f) }
                                            customCoefficients = emptyList()
                                            path.clear()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = AppDesign.spacingMedium)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteForever,
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
                                    // Gradient Border Add Button
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
                                                customFunctionSignals.add(SignalInstance(nextSignalId++, color, nextFreq, nextAmp))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Add,
                                                null,
                                                modifier = Modifier.size(AppDesign.iconSmall),
                                                tint = colors.accentCyan
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

                                    // Clear All Button
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
                                                nextSignalId = 0
                                                running = false
                                                hasStarted = false
                                                path.clear()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.DeleteForever,
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
                                                    hasStarted = false
                                                    path.clear()
                                                    prefs.saveFourierSignals(customFunctionSignals.toList())
                                                },
                                                onDel = {
                                                    customFunctionSignals.remove(signal)
                                                    if (customFunctionSignals.isEmpty()) {
                                                        nextSignalId = 0
                                                    }
                                                }
                                            )
                                        }

                                        if (customFunctionSignals.size > 5) {
                                            TextButton(
                                                onClick = { isSignalsExpanded = !isSignalsExpanded },
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
                                                        if (isSignalsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        null,
                                                        tint = colors.accentCyan,
                                                        modifier = Modifier.size(16.dp)
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
                        ) { speed = it }

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
                            ) { windingFrequency = it }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
        ) {
            Button(
                onClick = {
                    if (!hasStarted) hasStarted = true
                    running = !running
                },
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight),
                shape = RoundedCornerShape(AppDesign.radiusMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (running) colors.accentHell else colors.accentCyan
                )
            ) {
                Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = colors.textOnAccent
                )
                Spacer(Modifier.width(AppDesign.spacingSmall))
                Text(
                    if (running) "Pause" else if (hasStarted) "Resume" else "Simulate",
                    fontWeight = FontWeight.Bold,
                    color = colors.textOnAccent,
                )
            }

            AnimatedVisibility(
                visible = hasStarted,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Button(
                    onClick = {
                        running = false
                        hasStarted = false
                        time = 0f
                        path.clear()
                    },
                    modifier = Modifier.height(AppDesign.buttonHeight),
                    shape = RoundedCornerShape(AppDesign.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardSurface),
                    border = BorderStroke(AppDesign.borderStandard, colors.accentCyan)
                ) {
                    Text("Reset", color = colors.accentCyan)
                }
            }
        }

        // Visualization Canvas
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

                if (displayMode == FourierDisplayMode.CIRCULAR || displayMode == FourierDisplayMode.COMPLEX) {
                    val actualCenterX = if (displayMode == FourierDisplayMode.COMPLEX) size.width * 0.5f else centerX
                    translate(actualCenterX, centerY) {
                        var x = 0f
                        var y = 0f

                        // Limit terms shown in circular mode for performance on low-end devices
                        val termsToDraw = if (waveType == WaveType.CUSTOM_FUNCTION) {
                            nTerms.coerceAtMost(customFunctionSignals.size)
                        } else {
                            nTerms.coerceAtMost(25)
                        }

                        for (i in 0 until termsToDraw) {
                            val prevX = x
                            val prevY = y

                            val n = when (waveType) {
                                WaveType.SINE -> 1f
                                WaveType.SQUARE -> (i * 2 + 1).toFloat()
                                WaveType.SAWTOOTH -> (i + 1).toFloat()
                                WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                                WaveType.MY_SIGNAL -> (i + 1).toFloat()
                                WaveType.CUSTOM_FUNCTION -> if (i < customFunctionSignals.size) customFunctionSignals[i].freq.toFloatOrNull() ?: 0f else 0f
                            }

                            val radius = when (waveType) {
                                WaveType.SINE -> if (i == 0) radiusBase else 0f
                                WaveType.SQUARE -> radiusBase * (4f / (n * PI.toFloat()))
                                WaveType.SAWTOOTH -> radiusBase * (2f / (n * PI.toFloat()))
                                WaveType.TRIANGLE -> radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat()))
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first else 0f
                                WaveType.CUSTOM_FUNCTION -> if (i < customFunctionSignals.size) customFunctionSignals[i].amp.toFloatOrNull() ?: 0f else 0f
                            }

                            if (radius < 0.5f && i > 0) continue // Skip tiny circles

                            val phase = when (waveType) {
                                WaveType.TRIANGLE -> if (i % 2 != 0) PI.toFloat() else 0f
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].second else 0f
                                else -> 0f
                            }
                            val angle = 2 * PI.toFloat() * n * time + phase
                            x += radius * cos(angle)
                            y += radius * sin(angle)

                            // Draw Circle
                            drawCircle(
                                color = colors.accentCyan.copy(alpha = AppDesign.opacityLow * 2f),
                                radius = radius,
                                center = Offset(prevX, prevY),
                                style = Stroke(width = AppDesign.strokeThin)
                            )

                            // Draw Radius Line
                            drawLine(
                                color = colors.accentCyan.copy(alpha = AppDesign.opacityMedium),
                                start = Offset(prevX, prevY),
                                end = Offset(x, y),
                                strokeWidth = 1.5f
                            )
                        }

                        // Draw final point
                        drawCircle(colors.accentViolet, AppDesign.spacingExtraSmall.toPx(), Offset(x, y))

                        if (displayMode == FourierDisplayMode.CIRCULAR) {
                            // Draw Line to Wave
                            drawLine(
                                color = colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle),
                                start = Offset(x, y),
                                end = Offset(180f, y),
                                strokeWidth = AppDesign.strokeThin
                            )

                            // Draw Wave Path
                            val wavePath = Path()
                            if (path.isNotEmpty()) {
                                wavePath.moveTo(180f, path[0].y)
                                for (i in 1 until path.size step 2) {
                                    wavePath.lineTo(180f + i * 0.8f, path[i].y)
                                }
                            }

                            drawPath(
                                path = wavePath,
                                color = colors.accentCyan,
                                style = Stroke(width = AppDesign.strokeThick, cap = StrokeCap.Round)
                            )
                        } else {
                            // Draw Complex Trace
                            val tracePath = Path()
                            if (path.isNotEmpty()) {
                                tracePath.moveTo(path[0].x, path[0].y)
                                for (i in 1 until path.size) {
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
                        // Draw Grid lines - simplified
                        val gridColor = colors.accentCyan.copy(alpha = AppDesign.opacityGrid)
                        val step = 60f
                        for (i in -4..4) {
                            drawLine(gridColor, Offset(i * step, -200f), Offset(i * step, 200f), AppDesign.strokeThin)
                            drawLine(gridColor, Offset(-240f, i * step), Offset(240f, i * step), AppDesign.strokeThin)
                        }

                        // Draw Axes
                        drawLine(
                            colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle),
                            Offset(-240f, 0f),
                            Offset(240f, 0f),
                            AppDesign.strokeThin
                        )
                        drawLine(
                            colors.textSecondary.copy(alpha = AppDesign.opacityLow + AppDesign.opacitySubtle),
                            Offset(0f, -200f),
                            Offset(0f, 200f),
                            AppDesign.strokeThin
                        )

                        val wrappedPath = Path()
                        var sumX = 0f
                        var sumY = 0f
                        val baseRadius = 80f

                        if (path.isNotEmpty()) {
                            for (i in path.indices step 4) { // Increased step optimization
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
                            }

                            drawPath(
                                path = wrappedPath,
                                color = colors.accentCyan.copy(alpha = AppDesign.opacityTrace),
                                style = Stroke(width = AppDesign.strokeStandard, cap = StrokeCap.Round)
                            )

                            // Center of Mass (Red Dot)
                            val count = (path.size + 1) / 2
                            val avgX = sumX / count
                            val avgY = sumY / count
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

            // ── Sidebar Navigation ──
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
                    if (displayMode != FourierDisplayMode.CIRCULAR) path.clear()
                    displayMode = FourierDisplayMode.CIRCULAR
                }

                DisplayModeButton(
                    icon = Icons.Default.Adjust,
                    selected = displayMode == FourierDisplayMode.WRAPPING,
                    colors = colors
                ) {
                    if (displayMode != FourierDisplayMode.WRAPPING) path.clear()
                    displayMode = FourierDisplayMode.WRAPPING
                }

                DisplayModeButton(
                    icon = Icons.Default.Hub,
                    selected = displayMode == FourierDisplayMode.COMPLEX,
                    colors = colors
                ) {
                    if (displayMode != FourierDisplayMode.COMPLEX) path.clear()
                    displayMode = FourierDisplayMode.COMPLEX
                }

                Spacer(Modifier.height(AppDesign.spacingSmall + AppDesign.spacingExtraSmall / 2f))

                // Clear Trace Button
                IconButton(
                    onClick = { path.clear() },
                    modifier = Modifier
                        .size(AppDesign.sidebarButtonSize)
                        .background(
                            colors.accentHell.copy(alpha = AppDesign.opacityLow),
                            RoundedCornerShape(AppDesign.radiusSmall)
                        )
                        .border(
                            AppDesign.borderThin,
                            colors.accentHell.copy(alpha = AppDesign.opacityLow * 2f),
                            RoundedCornerShape(AppDesign.radiusSmall)
                        )
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        null,
                        tint = colors.accentHell,
                        modifier = Modifier.size(AppDesign.iconMedium)
                    )
                }
            }

            // ── Terms Handler (Right Sidebar) ──
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
                            Icons.Default.Add,
                            null,
                            tint = colors.accentCyan,
                            modifier = Modifier.size(AppDesign.iconSmallMedium)
                        )

                        Slider(
                            value = nTerms.toFloat(),
                            onValueChange = { nTerms = it.toInt() },
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
                            Icons.Default.Remove,
                            null,
                            tint = colors.accentCyan,
                            modifier = Modifier.size(AppDesign.iconSmallMedium)
                        )

                        Text(
                            "$nTerms",
                            color = colors.textPrimary,
                            fontSize = AppDesign.textCaption,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Harmonic Components or Center of Mass Graph
        when (displayMode) {
            FourierDisplayMode.WRAPPING -> {
                CenterOfMassGraph(
                    path = path,
                    colors = colors,
                    currentWindingFreq = windingFrequency
                )
            }
            FourierDisplayMode.COMPLEX -> {
                ComplexHarmonicComponents(
                    nTerms = nTerms,
                    waveType = waveType,
                    time = time,
                    colors = colors,
                    customCoefficients = customCoefficients,
                    customFunctionSignals = customFunctionSignals
                )
            }
            else -> {
                HarmonicComponents(
                    nTerms = nTerms,
                    waveType = waveType,
                    time = time,
                    colors = colors,
                    customCoefficients = customCoefficients,
                    customFunctionSignals = customFunctionSignals
                )
            }
        }

        // Info Card
        GlassCard(colors = colors) {
            Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
                Text(
                    "How it works",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.radiusSmall))
                Text(
                    "Fourier series allows us to represent any periodic signal as a sum of simple sine and cosine waves. " +
                            "By adding more terms (circles), we can approximate complex shapes like square or sawtooth waves more accurately.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun HarmonicComponents(
    nTerms: Int,
    waveType: WaveType,
    time: Float,
    colors: AppColors,
    customCoefficients: List<Pair<Float, Float>>,
    customFunctionSignals: List<SignalInstance>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxTerms = if (waveType == WaveType.CUSTOM_FUNCTION) {
        nTerms.coerceAtMost(customFunctionSignals.size)
    } else {
        nTerms
    }

    val displayTerms = if (isExpanded) maxTerms else maxTerms.coerceAtMost(6)

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Signal Decomposition",
                fontSize = AppDesign.textHeadline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppDesign.radiusLarge))

            // Result Indicator
            Text(
                "Pure Signal",
                color = colors.accentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.KeyboardDoubleArrowDown,
                null,
                tint = colors.textSecondary.copy(0.5f),
                modifier = Modifier.size(AppDesign.radiusLarge)
            )

            Spacer(Modifier.height(AppDesign.radiusSmall))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0 until displayTerms) {
                    val n = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        WaveType.MY_SIGNAL -> (i + 1).toFloat()
                        WaveType.CUSTOM_FUNCTION -> customFunctionSignals[i].freq.toFloatOrNull() ?: 0f
                    }

                    if (waveType == WaveType.SINE && i > 0) continue

                    val radiusBase = 30f
                    val radius = when (waveType) {
                        WaveType.SINE -> radiusBase
                        WaveType.SQUARE -> radiusBase * (4f / (n * PI.toFloat()))
                        WaveType.SAWTOOTH -> radiusBase * (2f / (n * PI.toFloat()))
                        WaveType.TRIANGLE -> radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat()))
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first * (radiusBase / 100f) else 0f
                        WaveType.CUSTOM_FUNCTION -> (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * (radiusBase / 100f)
                    }

                    if (i > 0) {
                        Text(
                            "+",
                            color = colors.textSecondary.copy(0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width(45.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "f = $n",
                                color = colors.accentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            val centerY = size.height / 2
                            val phase = when (waveType) {
                                WaveType.TRIANGLE -> if (i % 2 != 0) PI.toFloat() else 0f
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].second else 0f
                                else -> 0f
                            }

                            val path = Path()
                            val samples = 150
                            for (s in 0..samples) {
                                val x = (s.toFloat() / samples) * size.width
                                val waveT = time - (1f - s.toFloat() / samples) * 2f
                                val angle = 2 * PI.toFloat() * n * waveT + phase
                                val y = centerY + radius * sin(angle)

                                if (s == 0) path.moveTo(x, y)
                                else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = colors.accentCyan.copy(alpha = 0.6f),
                                style = Stroke(width = 2f, cap = StrokeCap.Round)
                            )

                            // Axes
                            val subAxisColor = colors.textSecondary.copy(alpha = 0.2f)
                            drawLine(
                                color = subAxisColor,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = subAxisColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                if (maxTerms > 6 && waveType != WaveType.SINE) {
                    Spacer(Modifier.height(AppDesign.spacingSmall))
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isExpanded) "Show Less" else "Show All Components",
                                color = colors.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = colors.accentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { signal.isExpanded = !signal.isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(AppDesign.radiusSmall)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(signal.color)
                    )
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(
                        "Signal Component ${signal.id}",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = AppDesign.textBodyLarge
                    )
                }
                Row {
                    if (showDel) IconButton(
                        onClick = onDel,
                        modifier = Modifier.size(AppDesign.iconLarge)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = colors.accentHell,
                            modifier = Modifier.size(AppDesign.iconSmall)
                        )
                    }
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Icon(
                        if (signal.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = colors.textSecondary
                    )
                }
            }
            AnimatedVisibility(
                visible = signal.isExpanded,
                enter = expandVertically(animationSpec = androidx.compose.animation.core.tween(50)) + fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        50
                    )
                ),
                exit = shrinkVertically(animationSpec = androidx.compose.animation.core.tween(50)) + fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(
                        50
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(top = AppDesign.spacingSmall),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        SignalField("Freq (Hz)", signal.freq, colors) {
                            signal.freq = it; onParameterChange()
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        SignalField("Amp", signal.amp, colors) {
                            signal.amp = it; onParameterChange()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalField(
    label: String,
    value: String,
    colors: AppColors,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = AppDesign.textCaption) },
        textStyle = TextStyle(color = colors.textPrimary, fontSize = AppDesign.textBody),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDesign.spacingSmall),
        shape = RoundedCornerShape(AppDesign.radiusSmall),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.fieldFocused,
            focusedLabelColor = colors.accentCyan,
            unfocusedBorderColor = colors.fieldBorder
        )
    )
}

@Composable
private fun CenterOfMassGraph(
    path: List<Offset>,
    colors: AppColors,
    currentWindingFreq: Float
) {
    var isExpanded by remember { mutableStateOf(false) }
    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize()
        ) {
            Text(
                "Frequency Domain (Real-time Center of Mass)",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(AppDesign.radiusLarge))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 250.dp else 180.dp)
            ) {
// ...
                val width = size.width
                val height = size.height
                val centerY = height * 0.7f
                val maxFreq = 5.0f
                val freqScale = width / maxFreq

                // Draw Axes
                val axisColor = colors.textSecondary.copy(0.3f)
                drawLine(axisColor, Offset(0f, centerY), Offset(width, centerY), 2f)
                drawLine(axisColor, Offset(0f, 0f), Offset(0f, height), 2f)

                // Labels
                for (f in 0..5) {
                    val x = f * freqScale
                    drawLine(axisColor, Offset(x, centerY - 5f), Offset(x, centerY + 5f), 1f)
                }

                if (path.isEmpty()) return@Canvas

                // Calculate COM Graph using the ACTUAL data points from the simulation
                val graphPath = Path()
                val freqSteps = 50 // Optimized for mobile

                for (s in 0..freqSteps) {
                    val freq = (s.toFloat() / freqSteps) * maxFreq

                    var sumX = 0f
                    // We integrate f(t) * cos(w*t) over the current path history
                    // Step optimization for the inner loop
                    for (i in path.indices step 6) {
                        val point = path[i]
                        val t = point.x
                        val ft = point.y
                        sumX += ft * cos(2 * PI.toFloat() * freq * t)
                    }

                    val avgX = sumX / ((path.size + 5) / 6)
                    val x = freq * freqScale
                    val y = centerY - avgX * 0.8f

                    if (s == 0) graphPath.moveTo(x, y)
                    else graphPath.lineTo(x, y)
                }

                drawPath(
                    path = graphPath,
                    color = colors.accentHell,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Current Frequency Marker
                val currentX = currentWindingFreq * freqScale

                // Highlight the point on the graph corresponding to the current winding freq
                var currentAvgX = 0f
                for (i in path.indices step 6) {
                    val point = path[i]
                    currentAvgX += point.y * cos(2 * PI.toFloat() * currentWindingFreq * point.x)
                }
                currentAvgX /= ((path.size + 5) / 6)
                val currentYOnGraph = centerY - currentAvgX * 0.8f

                drawLine(
                    colors.accentCyan.copy(0.4f),
                    Offset(currentX, 0f),
                    Offset(currentX, height),
                    1f,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(
                            10f,
                            10f
                        )
                    )
                )

                drawCircle(
                    colors.accentCyan,
                    6f,
                    Offset(currentX, currentYOnGraph)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Peaks indicate the frequencies present in the signal. When the winding frequency matches a component, the center of mass moves away from the origin.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.2f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Technical Insight:",
                        color = colors.accentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "This graph performs a real-time integration of your signal against a rotating phasor. The magnitude shown is the average 'pull' of the signal in the complex plane. Higher peaks correspond to stronger harmonics.",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isExpanded) "Show Less" else "Technical Details",
                        color = colors.accentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplexHarmonicComponents(
    nTerms: Int,
    waveType: WaveType,
    time: Float,
    colors: AppColors,
    customCoefficients: List<Pair<Float, Float>>,
    customFunctionSignals: List<SignalInstance>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxTerms = if (waveType == WaveType.CUSTOM_FUNCTION) {
        nTerms.coerceAtMost(customFunctionSignals.size)
    } else {
        nTerms
    }

    val displayTerms = if (isExpanded) maxTerms else maxTerms.coerceAtMost(6)

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Phasor Decomposition",
                fontSize = AppDesign.textHeadline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppDesign.radiusLarge))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0 until displayTerms) {
                    val n = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        WaveType.MY_SIGNAL -> (i + 1).toFloat()
                        WaveType.CUSTOM_FUNCTION -> customFunctionSignals[i].freq.toFloatOrNull() ?: 0f
                    }

                    if (waveType == WaveType.SINE && i > 0) continue

                    val radiusBase = 40f
                    val radius = when (waveType) {
                        WaveType.SINE -> radiusBase
                        WaveType.SQUARE -> radiusBase * (4f / (n * PI.toFloat()))
                        WaveType.SAWTOOTH -> radiusBase * (2f / (n * PI.toFloat()))
                        WaveType.TRIANGLE -> radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat()))
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first * (radiusBase / 100f) else 0f
                        WaveType.CUSTOM_FUNCTION -> (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * (radiusBase / 100f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(60.dp)) {
                            Text(
                                text = "Harmonic ${i + 1}",
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "f = $n",
                                color = colors.accentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val phase = when (waveType) {
                                WaveType.TRIANGLE -> if (i % 2 != 0) PI.toFloat() else 0f
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].second else 0f
                                else -> 0f
                            }

                            // Draw circle
                            drawCircle(
                                color = colors.accentCyan.copy(alpha = 0.1f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = 1f)
                            )

                            // Draw axes
                            drawLine(
                                colors.textSecondary.copy(alpha = 0.1f),
                                Offset(center.x - radius - 10f, center.y),
                                Offset(center.x + radius + 10f, center.y),
                                1f
                            )
                            drawLine(
                                colors.textSecondary.copy(alpha = 0.1f),
                                Offset(center.x, center.y - radius - 10f),
                                Offset(center.x, center.y + radius + 10f),
                                1f
                            )

                            // Draw rotating vector
                            val angle = 2 * PI.toFloat() * n * time + phase
                            val end = Offset(
                                center.x + radius * cos(angle),
                                center.y + radius * sin(angle)
                            )

                            drawLine(
                                color = colors.accentCyan,
                                start = center,
                                end = end,
                                strokeWidth = 2f,
                                cap = StrokeCap.Round
                            )

                            drawCircle(
                                color = colors.accentCyan,
                                radius = 3f,
                                center = end
                            )
                        }

                        Column(
                            modifier = Modifier.width(60.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Amplitude",
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", radius),
                                color = colors.accentViolet,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (maxTerms > 6 && waveType != WaveType.SINE) {
                    Spacer(Modifier.height(AppDesign.spacingSmall))
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isExpanded) "Show Less" else "Show All Harmonics",
                                color = colors.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = colors.accentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
