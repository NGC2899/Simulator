package com.example.matharium.fourier

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        WaveType.MY_SIGNAL_2D -> nTerms.coerceAtMost(customCoefficients2D.size)
        WaveType.FORMULA -> nTerms.coerceAtMost(formulaCoefficients.size)
        WaveType.SVG -> nTerms.coerceAtMost(svgCoefficients.size)
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
                        Text("Reset", color = colors.accentCyan, fontWeight = FontWeight.Bold, fontSize = AppDesign.textSmall)
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
                Text(
                    "Signal",
                    color = colors.accentCyan,
                    fontSize = AppDesign.textOverline,
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
                verticalArrangement = Arrangement.spacedBy(AppDesign.spacingExtraSmall),
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

                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val radiusBase = with(density) { 12.dp.toPx() } 
                    val pixelsPerTimeUnit = with(density) { 60.dp.toPx() }

                    val baseN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        else -> 1f
                    }

                    val (defaultAmp, phase) = when (waveType) {
                        WaveType.SINE -> Pair(1.0f, 0f)
                        WaveType.SQUARE -> Pair(4f / (baseN * PI.toFloat()), 0f)
                        WaveType.SAWTOOTH -> {
                            val sign = if (baseN.toInt() % 2 == 0) -1f else 1f
                            Pair((2f / (baseN * PI.toFloat())) * sign, 0f)
                        }
                        WaveType.TRIANGLE -> {
                            val sign = if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                            Pair((8f / (baseN * baseN * PI.toFloat() * PI.toFloat())) * sign, 0f)
                        }
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (customCoefficients[i].first to (PI.toFloat() / 2f - customCoefficients[i].second)) else (0f to 0f)
                        WaveType.FORMULA -> if (i < formulaCoefficients.size) (formulaCoefficients[i].first to (PI.toFloat() / 2f - formulaCoefficients[i].second)) else (0f to 0f)
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) (customCoefficients2D[i].amp to customCoefficients2D[i].phase) else (0f to 0f)
                        WaveType.SVG -> if (i < svgCoefficients.size) (svgCoefficients[i].amp to svgCoefficients[i].phase) else (0f to 0f)
                        WaveType.PURE_SIGNAL -> {
                            val ampValue = (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f)
                            ampValue to 0f
                        }
                        else -> (0f to 0f)
                    }
                    val amp = getHarmonicAmplitude(i, defaultAmp)

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
                                fontSize = AppDesign.textSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            val centerY = size.height / 2
                            val path = Path()
                            val samples = 150
                            val timeRange = size.width / pixelsPerTimeUnit
                            for (s in 0..samples) {
                                val x = (s.toFloat() / samples) * size.width
                                val waveT = if (isPaused) 0f else time - (s.toFloat() / samples) * timeRange
                                val totalAngle = 2 * PI.toFloat() * n * waveT + phase
                                val y = centerY - (amp * radiusBase) * sin(totalAngle)
                                // Standard 1D preview is just a time-series of the Y component
                                if (s == 0) path.moveTo(x, y)
                                else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = colors.accentCyan.copy(alpha = 0.6f),
                                style = Stroke(width = AppDesign.strokeStandard.toPx(), cap = StrokeCap.Round)
                            )

                            val subAxisColor = colors.textSecondary.copy(alpha = 0.2f)
                            drawLine(
                                color = subAxisColor,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY),
                                strokeWidth = AppDesign.strokeThin.toPx()
                            )
                            drawLine(
                                color = subAxisColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = AppDesign.strokeThin.toPx()
                            )
                        }

                        // Edit Menu
                        Box {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(AppDesign.iconLarge)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Edit",
                                    tint = colors.textSecondary.copy(AppDesign.opacityMedium),
                                    modifier = Modifier.size(AppDesign.iconSmall)
                                )
                            }
                            CompositionLocalProvider(LocalAbsoluteTonalElevation provides AppDesign.borderNone) {
                                MaterialTheme(
                                    colorScheme = MaterialTheme.colorScheme.copy(
                                        surface = Color.Transparent,
                                        surfaceVariant = Color.Transparent,
                                        onSurface = colors.textPrimary,
                                        primary = colors.accentCyan
                                    )
                                ) {
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        containerColor = Color.Transparent,
                                        tonalElevation = AppDesign.borderNone,
                                        shadowElevation = AppDesign.spacingSmall,
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clip(RoundedCornerShape(AppDesign.radiusCard))
                                            .background(colors.cardSurface.copy(alpha = 0.98f))
                                            .border(
                                                AppDesign.borderThin,
                                                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                                                RoundedCornerShape(AppDesign.radiusCard)
                                            )
                                            .padding(AppDesign.spacingSmall),
                                    ) {
                                        val isHarmonicPausedVal = isHarmonicPaused(i)
                                        DropdownMenuItem(
                                            text = { Text(if (isHarmonicPausedVal) "Resume" else "Pause", fontWeight = FontWeight.Bold, fontSize = AppDesign.textBodyLarge) },
                                            onClick = {
                                                onTogglePause(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = if (isHarmonicPausedVal) painterResource(id = R.drawable.caret_forward_outline) else painterResource(id = R.drawable.pause_outline),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(AppDesign.iconSmallMedium),
                                                    tint = colors.accentCyan
                                                )
                                            }
                                        )

                                        HorizontalDivider(
                                            color = colors.cardBorder.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        Column(modifier = Modifier.padding(horizontal = AppDesign.spacingMedium, vertical = AppDesign.spacingExtraSmall)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Frequency", fontSize = AppDesign.textSmall, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f Hz", n), fontSize = AppDesign.textSmall, color = colors.accentCyan, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = n,
                                                onValueChange = { onFrequencyChange(i, it) },
                                                valueRange = -20f..20f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = colors.accentCyan,
                                                    activeTrackColor = colors.accentCyan,
                                                    inactiveTrackColor = colors.fieldBorder.copy(0.2f)
                                                )
                                            )
                                        }

                                        Column(modifier = Modifier.padding(horizontal = AppDesign.spacingMedium, vertical = AppDesign.spacingExtraSmall)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Amplitude", fontSize = AppDesign.textSmall, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f", amp), fontSize = AppDesign.textSmall, color = colors.accentViolet, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = amp,
                                                onValueChange = { onAmplitudeChange(i, it) },
                                                valueRange = -2.0f..2.0f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = colors.accentViolet,
                                                    activeTrackColor = colors.accentViolet,
                                                    inactiveTrackColor = colors.fieldBorder.copy(0.2f)
                                                )
                                            )
                                        }

                                        HorizontalDivider(
                                            color = colors.cardBorder.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Reset to Default", fontSize = AppDesign.textBodyLarge) },
                                            onClick = {
                                                onResetHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(AppDesign.iconSmallMedium)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Remove", color = colors.accentHell, fontWeight = FontWeight.Bold, fontSize = AppDesign.textBodyLarge) },
                                            onClick = {
                                                onRemoveHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.trash_outline),
                                                    contentDescription = null,
                                                    tint = colors.accentHell,
                                                    modifier = Modifier.size(AppDesign.iconSmallMedium)
                                                )
                                            }
                                        )
                                    }
                                }
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
                                fontSize = AppDesign.textBody
                            )
                            Spacer(Modifier.width(AppDesign.spacingExtraSmall))
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
fun FrequencyDomainGraph(
    spectrumData: List<FourierLogic.Complex>,
    colors: AppColors,
    currentWindingFreq: Float,
    time: Float
) {
    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
            Text(
                "Frequency Domain (Real-time Center of Mass)",
                fontSize = AppDesign.textHeadline,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(AppDesign.spacingSmall))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(colors.cardSurface.copy(alpha = 0.1f), RoundedCornerShape(AppDesign.radiusSmall))
                    .padding(AppDesign.spacingSmall)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (spectrumData.isEmpty()) return@Canvas
                    
                    val w = size.width
                    val h = size.height
                    
                    // Frequency range 0 to 5 Hz
                    val maxFreq = 5.0f
                    val spectrumPoints = spectrumData.size
                    val projectedValues = FloatArray(spectrumPoints)
                    
                    // Calculate real-time wiggle using pre-calculated complex coefficients
                    for (i in 0 until spectrumPoints) {
                        val f = (i.toFloat() / spectrumPoints) * maxFreq
                        val coeff = spectrumData[i]
                        val angle = 2 * PI.toFloat() * f * time
                        // Real part of (re + i*im) * e^(-i * angle) 
                        // = re * cos(angle) + im * sin(angle)
                        projectedValues[i] = (coeff.re * cos(angle) + coeff.im * sin(angle)).toFloat()
                    }
                    
                    val maxVal = projectedValues.map { kotlin.math.abs(it) }.maxOrNull()?.coerceAtLeast(0.1f) ?: 1f
                    
                    // Draw Axis
                    val axisColor = colors.textSecondary.copy(alpha = 0.2f)
                    val centerY = h / 2f
                    drawLine(axisColor, Offset(0f, centerY), Offset(w, centerY), 1.dp.toPx()) // Zero-line
                    drawLine(axisColor, Offset(0f, 0f), Offset(0f, h), 1.dp.toPx())

                    // Draw Spectrum Path
                    val path = Path()
                    for (i in projectedValues.indices) {
                        val x = (i.toFloat() / (spectrumPoints - 1)) * w
                        val y = centerY - (projectedValues[i] / maxVal) * (h / 2f) * 0.9f
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    
                    drawPath(
                        path = path,
                        color = colors.accentCyan,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Draw Current Frequency Indicator
                    val indicatorX = (currentWindingFreq / maxFreq) * w
                    if (indicatorX in 0f..w) {
                        drawLine(
                            color = colors.accentHell,
                            start = Offset(indicatorX, 0f),
                            end = Offset(indicatorX, h),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(AppDesign.spacingSmall))
            Text(
                "The peaks show the frequencies that make up your signal. The dashed line tracks your winding frequency.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
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
        WaveType.MY_SIGNAL_2D -> nTerms.coerceAtMost(customCoefficients2D.size)
        WaveType.FORMULA -> nTerms.coerceAtMost(formulaCoefficients.size)
        WaveType.SVG -> nTerms.coerceAtMost(svgCoefficients.size)
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
                        Text("Reset", color = colors.accentCyan, fontWeight = FontWeight.Bold, fontSize = AppDesign.textSmall)
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
                Text(
                    "Signal",
                    color = colors.accentCyan,
                    fontSize = AppDesign.textOverline,
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
                verticalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium),
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

                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val radiusBase = with(density) { AppDesign.phasorRadiusBase.toPx() }
                    val baseN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        else -> 1f
                    }

                    val (defaultRadius, phase) = when (waveType) {
                        WaveType.SINE -> Pair(1.0f, 0f)
                        WaveType.SQUARE -> Pair(4f / (baseN * PI.toFloat()), 0f)
                        WaveType.SAWTOOTH -> {
                            val sign = if (baseN.toInt() % 2 == 0) -1f else 1f
                            Pair((2f / (baseN * PI.toFloat())) * sign, 0f)
                        }
                        WaveType.TRIANGLE -> {
                            val sign = if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                            Pair((8f / (baseN * baseN * PI.toFloat() * PI.toFloat())) * sign, 0f)
                        }
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (customCoefficients[i].first to (PI.toFloat() / 2f - customCoefficients[i].second)) else (0f to 0f)
                        WaveType.FORMULA -> if (i < formulaCoefficients.size) (formulaCoefficients[i].first to (PI.toFloat() / 2f - formulaCoefficients[i].second)) else (0f to 0f)
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) (customCoefficients2D[i].amp to customCoefficients2D[i].phase) else (0f to 0f)
                        WaveType.SVG -> if (i < svgCoefficients.size) (svgCoefficients[i].amp to svgCoefficients[i].phase) else (0f to 0f)
                        WaveType.PURE_SIGNAL -> (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) to 0f
                        else -> 0f to 0f
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
                                fontSize = AppDesign.textBody,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val phasorPhase = when (waveType) {
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (PI.toFloat() / 2f - customCoefficients[i].second) else 0f
                                WaveType.FORMULA -> if (i < formulaCoefficients.size) (PI.toFloat() / 2f - formulaCoefficients[i].second) else 0f
                                WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].phase else 0f
                                WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].phase else 0f
                                else -> 0f
                            }

                            drawCircle(
                                color = colors.accentCyan.copy(alpha = 0.1f),
                                radius = kotlin.math.abs(radius * radiusBase),
                                center = center,
                                style = Stroke(width = 1.dp.toPx())
                            )

                            drawLine(
                                colors.textSecondary.copy(alpha = 0.1f),
                                Offset(center.x - (kotlin.math.abs(radius * radiusBase)) - 10.dp.toPx(), center.y),
                                Offset(center.x + (kotlin.math.abs(radius * radiusBase)) + 10.dp.toPx(), center.y),
                                1.dp.toPx()
                            )
                            drawLine(
                                colors.textSecondary.copy(alpha = 0.1f),
                                Offset(center.x, center.y - (kotlin.math.abs(radius * radiusBase)) - 10.dp.toPx()),
                                Offset(center.x, center.y + (kotlin.math.abs(radius * radiusBase)) + 10.dp.toPx()),
                                1.dp.toPx()
                            )

                            // Draw rotating vector
                            val totalAngle = if (isPaused) 0f else 2 * PI.toFloat() * n * time + phasorPhase
                            val end = Offset(
                                center.x + (radius * radiusBase) * cos(totalAngle),
                                center.y - (radius * radiusBase) * sin(totalAngle)
                            )

                            drawLine(
                                color = colors.accentCyan,
                                start = center,
                                end = end,
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            drawCircle(
                                color = colors.accentCyan,
                                radius = 3.dp.toPx(),
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
                            CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
                                MaterialTheme(
                                    colorScheme = MaterialTheme.colorScheme.copy(
                                        surface = Color.Transparent,
                                        surfaceVariant = Color.Transparent,
                                        onSurface = colors.textPrimary,
                                        primary = colors.accentCyan
                                    )
                                ) {
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        containerColor = Color.Transparent,
                                        tonalElevation = AppDesign.borderNone,
                                        shadowElevation = AppDesign.spacingSmall,
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clip(RoundedCornerShape(AppDesign.radiusCard))
                                            .background(colors.cardSurface.copy(alpha = 0.98f))
                                            .border(
                                                AppDesign.borderThin,
                                                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                                                RoundedCornerShape(AppDesign.radiusCard)
                                            )
                                            .padding(AppDesign.spacingSmall),
                                    ) {
                                        val isHarmonicPausedVal = isHarmonicPaused(i)
                                        DropdownMenuItem(
                                            text = { Text(if (isHarmonicPausedVal) "Resume" else "Pause", fontWeight = FontWeight.Bold, fontSize = AppDesign.textBodyLarge) },
                                            onClick = {
                                                onTogglePause(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = if (isHarmonicPausedVal) painterResource(id = R.drawable.caret_forward_outline) else painterResource(id = R.drawable.pause_outline),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(AppDesign.iconSmallMedium),
                                                    tint = colors.accentCyan
                                                )
                                            }
                                        )

                                        HorizontalDivider(
                                            color = colors.cardBorder.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        Column(modifier = Modifier.padding(horizontal = AppDesign.spacingMedium, vertical = AppDesign.spacingExtraSmall)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Frequency", fontSize = AppDesign.textSmall, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f Hz", n), fontSize = AppDesign.textSmall, color = colors.accentCyan, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = n,
                                                onValueChange = { onFrequencyChange(i, it) },
                                                valueRange = -20f..20f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = colors.accentCyan,
                                                    activeTrackColor = colors.accentCyan,
                                                    inactiveTrackColor = colors.fieldBorder.copy(0.2f)
                                                )
                                            )
                                        }

                                        Column(modifier = Modifier.padding(horizontal = AppDesign.spacingMedium, vertical = AppDesign.spacingExtraSmall)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Amplitude", fontSize = AppDesign.textSmall, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f", radius), fontSize = AppDesign.textSmall, color = colors.accentViolet, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = radius,
                                                onValueChange = { onAmplitudeChange(i, it) },
                                                valueRange = -2.0f..2.0f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = colors.accentViolet,
                                                    activeTrackColor = colors.accentViolet,
                                                    inactiveTrackColor = colors.fieldBorder.copy(0.2f)
                                                )
                                            )
                                        }

                                        HorizontalDivider(
                                            color = colors.cardBorder.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Reset to Default", fontSize = AppDesign.textBodyLarge) },
                                            onClick = {
                                                onResetHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(AppDesign.iconSmallMedium)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Remove", color = colors.accentHell, fontWeight = FontWeight.Bold, fontSize = AppDesign.textBodyLarge) },
                                            onClick = {
                                                onRemoveHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.trash_outline),
                                                    contentDescription = null,
                                                    tint = colors.accentHell,
                                                    modifier = Modifier.size(AppDesign.iconSmallMedium)
                                                )
                                            }
                                        )
                                    }
                                }
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
                                fontSize = AppDesign.textBody
                            )
                            Spacer(Modifier.width(AppDesign.spacingExtraSmall))
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
