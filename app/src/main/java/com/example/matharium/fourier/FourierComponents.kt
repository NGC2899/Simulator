package com.example.matharium.fourier

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.app.*
import com.example.matharium.R
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun HarmonicComponents(
    nTerms: Int,
    waveType: WaveType,
    time: Float,
    colors: AppColors,
    customCoefficients: List<Pair<Float, Float>>,
    customCoefficients2D: List<FourierLogic.ComplexCoeff> = emptyList(),
    formulaCoefficients: List<Pair<Float, Float>> = emptyList(),
    svgCoefficients: List<FourierLogic.ComplexCoeff> = emptyList(),
    customFunctionSignals: List<SignalInstance>,
    onRemoveHarmonic: (Int) -> Unit = {},
    onTogglePause: (Int) -> Unit = {},
    isHarmonicPaused: (Int) -> Boolean = { false },
    onFrequencyChange: (Int, Float) -> Unit = { _, _ -> },
    getHarmonicFrequency: (Int, Float) -> Float = { _, default -> default },
    onAmplitudeChange: (Int, Float) -> Unit = { _, _ -> },
    getHarmonicAmplitude: (Int, Float) -> Float = { _, default -> default },
    removedHarmonics: Map<Int, Boolean> = emptyMap(),
    onResetHarmonic: (Int) -> Unit = {},
    onResetHarmonics: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxTerms = when (waveType) {
        WaveType.PURE_SIGNAL -> customFunctionSignals.size
        WaveType.MY_SIGNAL_2D -> customCoefficients2D.size
        WaveType.FORMULA -> formulaCoefficients.size
        WaveType.SVG -> svgCoefficients.size
        else -> nTerms
    }

    val activeTermsIndices = remember(waveType, maxTerms, removedHarmonics.size) {
        (0 until maxTerms).filter { index ->
            if (waveType == WaveType.SINE && index > 0) false
            else removedHarmonics[index] != true
        }
    }

    val displayTermsIndices = if (isExpanded) {
        activeTermsIndices.take(100)
    } else {
        activeTermsIndices.take(6)
    }

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(animationSpec = tween(AppDesign.animDurationStandard)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Signal Decomposition",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold,
                )
                if (activeTermsIndices.isNotEmpty()) {
                    TextButton(onClick = onResetHarmonics) {
                        Text("Reset", color = colors.accentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (activeTermsIndices.isEmpty()) {
                Text(
                    "No active components",
                    color = colors.accentCyan,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppDesign.spacingLarge),
                )
            }

            Spacer(Modifier.height(AppDesign.radiusLarge))

            if (activeTermsIndices.isNotEmpty()) {
                // Result Indicator
                Text(
                    "Signal",
                    color = colors.accentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.radiusSmall))
                Icon(
                    Icons.Default.KeyboardDoubleArrowDown,
                    null,
                    tint = colors.textSecondary.copy(0.5f),
                    modifier = Modifier.size(AppDesign.radiusLarge)
                )

                Spacer(Modifier.height(AppDesign.radiusSmall))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                displayTermsIndices.forEach { i ->
                    val defaultN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        WaveType.MY_SIGNAL -> i.toFloat()
                        WaveType.FORMULA -> i.toFloat()
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].freq.toFloat() else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].freq.toFloat() else 0f
                        WaveType.PURE_SIGNAL -> customFunctionSignals[i].freq.toFloatOrNull() ?: 0f
                    }
                    val n = getHarmonicFrequency(i, defaultN)
                    val isPaused = isHarmonicPaused(i)

                    val radiusBase = 30f

                    val baseN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        else -> 1f
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .graphicsLayer(alpha = if (isPaused) 0.4f else 1f),
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
                            val (defaultAmp, phase) = when (waveType) {
                                WaveType.SINE -> Pair(-radiusBase, PI.toFloat() / 2f)
                                WaveType.SQUARE -> Pair(-radiusBase * (4f / (baseN * PI.toFloat())), PI.toFloat() / 2f)
                                WaveType.SAWTOOTH -> {
                                    val sign = if (baseN.toInt() % 2 == 0) -1f else 1f
                                    Pair(-radiusBase * (2f / (baseN * PI.toFloat())) * sign, PI.toFloat() / 2f)
                                }
                                WaveType.TRIANGLE -> {
                                    val sign = if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                                    Pair(-radiusBase * (8f / (baseN * baseN * PI.toFloat() * PI.toFloat())) * sign, PI.toFloat() / 2f)
                                }
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (customCoefficients[i].first * (radiusBase / 100f) to customCoefficients[i].second) else (0f to 0f)
                                WaveType.FORMULA -> if (i < formulaCoefficients.size) (formulaCoefficients[i].first * (radiusBase / 100f) to formulaCoefficients[i].second) else (0f to 0f)
                                WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) (customCoefficients2D[i].amp * (radiusBase / 100f) to customCoefficients2D[i].phase) else (0f to 0f)
                                WaveType.SVG -> if (i < svgCoefficients.size) (svgCoefficients[i].amp * (radiusBase / 100f) to svgCoefficients[i].phase) else (0f to 0f)
                                WaveType.PURE_SIGNAL -> {
                                    val amp = (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * (radiusBase / 100f) * -1f
                                    val phase = PI.toFloat() / 2f
                                    amp to phase
                                }
                                else -> (0f to 0f)
                            }
                            
                            val amp = getHarmonicAmplitude(i, defaultAmp)

                            val path = Path()
                            val samples = 150
                            for (s in 0..samples) {
                                val x = (s.toFloat() / samples) * size.width
                                val waveT = if (isPaused) 0f else time - (1f - s.toFloat() / samples) * 2f
                                val angle = 2 * PI.toFloat() * n * waveT
                                val y = if (waveType == WaveType.MY_SIGNAL_2D || waveType == WaveType.SVG) {
                                    centerY + amp * sin(angle + phase)
                                } else {
                                    centerY + amp * cos(angle - phase)
                                }

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

                        // Edit Menu
                        Box () {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(AppDesign.iconSmall)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Edit",
                                    tint = colors.textSecondary.copy(AppDesign.opacityMedium),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier
                                    .width(200.dp)
                                    .clip(RoundedCornerShape(AppDesign.radiusCard))
                                    .background(colors.cardSurface.copy(alpha = AppDesign.opacityGlass))
                                    .border(
                                        AppDesign.borderThin,
                                        colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                                        RoundedCornerShape(AppDesign.radiusCard)
                                    )
                                    .padding(8.dp),
                            ) {
                                val isPaused = isHarmonicPaused(i)
                                DropdownMenuItem(
                                    text = { Text(if (isPaused) "Resume" else "Pause") },
                                    onClick = {
                                        onTogglePause(i)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isPaused) painterResource( id = R.drawable.pause_outline) else painterResource( id = R.drawable.caret_forward_outline),
                                            contentDescription = null,
                                            modifier = Modifier.size(AppDesign.iconSmall),
                                            tint = colors.accentCyan
                                        )
                                    }
                                )
                                
                                Spacer(Modifier.height(AppDesign.spacingSmall))
                                Text(
                                    "Frequency",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Slider(
                                    value = getHarmonicFrequency(i, n),
                                    onValueChange = { onFrequencyChange(i, it) },
                                    valueRange = -20f..20f,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.accentCyan,
                                        activeTrackColor = colors.accentCyan
                                    )
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Amplitude",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Slider(
                                    value = getHarmonicAmplitude(i, 0f), // Fallback
                                    onValueChange = { onAmplitudeChange(i, it) },
                                    valueRange = -50f..50f,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.accentCyan,
                                        activeTrackColor = colors.accentCyan
                                    )
                                )

                                DropdownMenuItem(
                                    text = { Text("Reset to Default") },
                                    onClick = {
                                        onResetHarmonic(i)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = colors.accentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        onRemoveHarmonic(i)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource( id = R.drawable.trash_outline),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (activeTermsIndices.size > displayTermsIndices.size && waveType != WaveType.SINE) {
                    Spacer(Modifier.height(AppDesign.spacingSmall))
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isExpanded) "Show Less" else "Show All Components (${activeTermsIndices.size})",
                                color = colors.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                                null,
                                tint = colors.accentCyan,
                                modifier = Modifier.size(AppDesign.iconTiny)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CenterOfMassGraph(
    path: List<Offset>,
    colors: AppColors,
    currentWindingFreq: Float
) {
    var isExpanded by remember { mutableStateOf(false) }
    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(animationSpec = tween(AppDesign.animDurationStandard))
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
                val freqSteps = 120

                for (s in 0..freqSteps) {
                    val freq = (s.toFloat() / freqSteps) * maxFreq

                    var sumX = 0f
                    var sumY = 0f
                    var processedCount = 0
                    // Increased sampling density (step 5) for better accuracy at low speeds
                    for (i in path.indices step 5) {
                        val point = path[i]
                        val t = point.x
                        val ft = point.y
                        sumX += ft * cos(2 * PI.toFloat() * freq * t)
                        sumY += ft * sin(2 * PI.toFloat() * freq * t)
                        processedCount++
                    }

                    val avgX = if (processedCount > 0) sumX / processedCount else 0f
                    val avgY = if (processedCount > 0) sumY / processedCount else 0f
                    val magnitude = sqrt(avgX * avgX + avgY * avgY)
                    
                    val x = freq * freqScale
                    val y = centerY - magnitude * 0.9f

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
                var currentSumX = 0f
                var currentSumY = 0f
                var currentCount = 0
                for (i in path.indices step 5) {
                    val point = path[i]
                    currentSumX += point.y * cos(2 * PI.toFloat() * currentWindingFreq * point.x)
                    currentSumY += point.y * sin(2 * PI.toFloat() * currentWindingFreq * point.x)
                    currentCount++
                }
                val currentAvgX = if (currentCount > 0) currentSumX / currentCount else 0f
                val currentAvgY = if (currentCount > 0) currentSumY / currentCount else 0f
                val currentMagnitude = sqrt(currentAvgX * currentAvgX + currentAvgY * currentAvgY)
                val currentYOnGraph = centerY - currentMagnitude * 0.9f

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

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(AppDesign.animDurationStandard)) + fadeIn(
                    animationSpec = tween(AppDesign.animDurationStandard)
                ),
                exit = shrinkVertically(animationSpec = tween(AppDesign.animDurationStandard)) + fadeOut(
                    animationSpec = tween(AppDesign.animDurationStandard)
                )
            ) {
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
                        if (isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                        null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(AppDesign.iconTiny)
                    )
                }
            }
        }
    }
}

@Composable
fun ComplexHarmonicComponents(
    nTerms: Int,
    waveType: WaveType,
    time: Float,
    colors: AppColors,
    customCoefficients: List<Pair<Float, Float>>,
    customCoefficients2D: List<FourierLogic.ComplexCoeff> = emptyList(),
    formulaCoefficients: List<Pair<Float, Float>> = emptyList(),
    svgCoefficients: List<FourierLogic.ComplexCoeff> = emptyList(),
    customFunctionSignals: List<SignalInstance>,
    onRemoveHarmonic: (Int) -> Unit = {},
    onTogglePause: (Int) -> Unit = {},
    isHarmonicPaused: (Int) -> Boolean = { false },
    onFrequencyChange: (Int, Float) -> Unit = { _, _ -> },
    getHarmonicFrequency: (Int, Float) -> Float = { _, default -> default },
    onAmplitudeChange: (Int, Float) -> Unit = { _, _ -> },
    getHarmonicAmplitude: (Int, Float) -> Float = { _, default -> default },
    removedHarmonics: Map<Int, Boolean> = emptyMap(),
    onResetHarmonic: (Int) -> Unit = {},
    onResetHarmonics: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxTerms = when (waveType) {
        WaveType.PURE_SIGNAL -> customFunctionSignals.size
        WaveType.MY_SIGNAL_2D -> customCoefficients2D.size
        WaveType.FORMULA -> formulaCoefficients.size
        WaveType.SVG -> svgCoefficients.size
        else -> nTerms
    }

    val activeTermsIndices = remember(waveType, maxTerms, removedHarmonics.size) {
        (0 until maxTerms).filter { index ->
            if (waveType == WaveType.SINE && index > 0) false
            else removedHarmonics[index] != true
        }
    }

    val displayTermsIndices = if (isExpanded) {
        activeTermsIndices.take(100)
    } else {
        activeTermsIndices.take(6)
    }

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(animationSpec = tween(AppDesign.animDurationStandard)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Phasor Decomposition",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold,
                )
                if (activeTermsIndices.isNotEmpty()) {
                    TextButton(onClick = onResetHarmonics) {
                        Text("Reset", color = colors.accentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (activeTermsIndices.isEmpty()) {
                Text(
                    "No active components",
                    color = colors.accentCyan,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppDesign.spacingLarge),
                )
            }

            Spacer(Modifier.height(AppDesign.radiusLarge))

            if (activeTermsIndices.isNotEmpty()) {
                // Result Indicator
                Text(
                    "Signal",
                    color = colors.accentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.radiusSmall))
                Icon(
                    Icons.Default.KeyboardDoubleArrowDown,
                    null,
                    tint = colors.textSecondary.copy(0.5f),
                    modifier = Modifier.size(AppDesign.radiusLarge)
                )

                Spacer(Modifier.height(AppDesign.radiusSmall))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                displayTermsIndices.forEach { i ->
                    val defaultN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        WaveType.MY_SIGNAL -> i.toFloat()
                        WaveType.FORMULA -> i.toFloat()
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].freq.toFloat() else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].freq.toFloat() else 0f
                        WaveType.PURE_SIGNAL -> customFunctionSignals[i].freq.toFloatOrNull() ?: 0f
                    }
                    val n = getHarmonicFrequency(i, defaultN)
                    val isPaused = isHarmonicPaused(i)

                    val radiusBase = 40f
                    val baseN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        else -> 1f
                    }

                    val defaultRadius = when (waveType) {
                        WaveType.SINE -> -radiusBase
                        WaveType.SQUARE -> -radiusBase * (4f / (baseN * PI.toFloat()))
                        WaveType.SAWTOOTH -> -radiusBase * (2f / (baseN * PI.toFloat()))
                        WaveType.TRIANGLE -> -radiusBase * (8f / (baseN * baseN * PI.toFloat() * PI.toFloat()))
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first * (radiusBase / 100f) else 0f
                        WaveType.FORMULA -> if (i < formulaCoefficients.size) formulaCoefficients[i].first * (radiusBase / 100f) else 0f
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].amp * (radiusBase / 100f) else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].amp * (radiusBase / 100f) else 0f
                        WaveType.PURE_SIGNAL -> (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * (radiusBase / 100f)
                        else -> 0f
                    }
                    
                    val radius = getHarmonicAmplitude(i, defaultRadius)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .graphicsLayer(alpha = if (isPaused) 0.4f else 1f),
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
                                WaveType.FORMULA -> if (i < formulaCoefficients.size) formulaCoefficients[i].second else 0f
                                WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].phase else 0f
                                WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].phase else 0f
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
                            val angle = if (isPaused) 0f else 2 * PI.toFloat() * n * time + phase
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

                        // Edit Menu
                        Box {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Edit",
                                    tint = colors.textSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier
                                    .width(200.dp)
                                    .background(colors.cardSurface)
                                    .padding(8.dp)
                            ) {
                                val isPaused = isHarmonicPaused(i)
                                DropdownMenuItem(
                                    text = { Text(if (isPaused) "Resume" else "Pause") },
                                    onClick = {
                                        onTogglePause(i)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = null,
                                            tint = colors.accentCyan
                                        )
                                    }
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Frequency",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Slider(
                                    value = getHarmonicFrequency(i, n),
                                    onValueChange = { onFrequencyChange(i, it) },
                                    valueRange = -20f..20f,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.accentCyan,
                                        activeTrackColor = colors.accentCyan
                                    )
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Amplitude",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Slider(
                                    value = getHarmonicAmplitude(i, radius),
                                    onValueChange = { onAmplitudeChange(i, it) },
                                    valueRange = -200f..200f,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.accentViolet,
                                        activeTrackColor = colors.accentViolet
                                    )
                                )

                                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(
                                    text = { Text("Reset to Default") },
                                    onClick = {
                                        onResetHarmonic(i)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = colors.accentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )

                                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(
                                    text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        onRemoveHarmonic(i)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (activeTermsIndices.size > displayTermsIndices.size && waveType != WaveType.SINE) {
                    Spacer(Modifier.height(AppDesign.spacingSmall))
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isExpanded) "Show Less" else "Show All Harmonics (${activeTermsIndices.size})",
                                color = colors.accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isExpanded) painterResource(id = R.drawable.chevron_up_outline) else painterResource(id = R.drawable.chevron_down_outline),
                                null,
                                tint = colors.accentCyan,
                                modifier = Modifier.size(AppDesign.iconTiny)
                            )
                        }
                    }
                }
            }
        }
    }
}
