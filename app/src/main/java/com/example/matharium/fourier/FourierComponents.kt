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

                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val radiusBase = with(density) { 30.dp.toPx() }

                    val baseN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        else -> 1f
                    }

                    val (defaultAmp, phase) = when (waveType) {
                        WaveType.SINE -> Pair(-1.0f, PI.toFloat() / 2f)
                        WaveType.SQUARE -> Pair(-1.0f * (4f / (baseN * PI.toFloat())), PI.toFloat() / 2f)
                        WaveType.SAWTOOTH -> {
                            val sign = if (baseN.toInt() % 2 == 0) -1f else 1f
                            Pair(-1.0f * (2f / (baseN * PI.toFloat())) * sign, PI.toFloat() / 2f)
                        }
                        WaveType.TRIANGLE -> {
                            val sign = if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                            Pair(-1.0f * (8f / (baseN * baseN * PI.toFloat() * PI.toFloat())) * sign, PI.toFloat() / 2f)
                        }
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (customCoefficients[i].first to customCoefficients[i].second) else (0f to 0f)
                        WaveType.FORMULA -> if (i < formulaCoefficients.size) (formulaCoefficients[i].first to formulaCoefficients[i].second) else (0f to 0f)
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) (customCoefficients2D[i].amp to customCoefficients2D[i].phase) else (0f to 0f)
                        WaveType.SVG -> if (i < svgCoefficients.size) (svgCoefficients[i].amp to svgCoefficients[i].phase) else (0f to 0f)
                        WaveType.PURE_SIGNAL -> {
                            val ampValue = (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * -1f
                            val phaseValue = PI.toFloat() / 2f
                            ampValue to phaseValue
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
                            val path = Path()
                            val samples = 150
                            for (s in 0..samples) {
                                val x = (s.toFloat() / samples) * size.width
                                val waveT = if (isPaused) 0f else time - (1f - s.toFloat() / samples) * 2f
                                val angle = 2 * PI.toFloat() * n * waveT
                                val y = if (waveType == WaveType.MY_SIGNAL_2D || waveType == WaveType.SVG) {
                                    centerY + (amp * radiusBase) * sin(angle + phase)
                                } else {
                                    centerY + (amp * radiusBase) * cos(angle - phase)
                                }

                                if (s == 0) path.moveTo(x, y)
                                else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = colors.accentCyan.copy(alpha = 0.6f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            val subAxisColor = colors.textSecondary.copy(alpha = 0.2f)
                            drawLine(
                                color = subAxisColor,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = subAxisColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Edit Menu
                        Box {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Edit",
                                    tint = colors.textSecondary.copy(AppDesign.opacityMedium),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
                                MaterialTheme(
                                    colorScheme = MaterialTheme.colorScheme.copy(
                                        surface = Color.Transparent,
                                        surfaceVariant = Color.Transparent,
                                        onSurface = colors.textPrimary
                                    )
                                ) {
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clip(RoundedCornerShape(AppDesign.radiusCard))
                                            .background(colors.cardSurface.copy(alpha = 0.98f))
                                            .border(
                                                AppDesign.borderThin,
                                                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                                                RoundedCornerShape(AppDesign.radiusCard)
                                            )
                                            .padding(8.dp),
                                    ) {
                                        val isHarmonicPausedVal = isHarmonicPaused(i)
                                        DropdownMenuItem(
                                            text = { Text(if (isHarmonicPausedVal) "Resume" else "Pause", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            onClick = {
                                                onTogglePause(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = if (isHarmonicPausedVal) painterResource(id = R.drawable.caret_forward_outline) else painterResource(id = R.drawable.pause_outline),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = colors.accentCyan
                                                )
                                            }
                                        )

                                        HorizontalDivider(
                                            color = colors.cardBorder.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Frequency", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f Hz", n), fontSize = 11.sp, color = colors.accentCyan, fontWeight = FontWeight.Bold)
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

                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Amplitude", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f", amp), fontSize = 11.sp, color = colors.accentViolet, fontWeight = FontWeight.Bold)
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
                                            text = { Text("Reset to Default", fontSize = 13.sp) },
                                            onClick = {
                                                onResetHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Remove", color = colors.accentHell, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            onClick = {
                                                onRemoveHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.trash_outline),
                                                    contentDescription = null,
                                                    tint = colors.accentHell,
                                                    modifier = Modifier.size(18.dp)
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
    path: List<PathPoint>,
    colors: AppColors,
    currentWindingFreq: Float
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusBasePx = with(density) { 100.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val gridColor = colors.textSecondary.copy(alpha = 0.1f)
        
        drawCircle(
            color = gridColor,
            radius = radiusBasePx,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawLine(
            gridColor,
            Offset(0f, center.y),
            Offset(size.width, center.y),
            1.dp.toPx()
        )
        drawLine(
            gridColor,
            Offset(center.x, 0f),
            Offset(center.x, size.height),
            1.dp.toPx()
        )

        if (path.isNotEmpty()) {
            val wrappedPath = Path()
            var sumX = 0f
            var sumY = 0f
            var processedCount = 0
            
            val baseRadius = radiusBasePx * 0.8f

            for (i in path.indices step 2) {
                val point = path[i].offset
                val t = point.x
                val amplitude = point.y + baseRadius
                val angle = -2 * PI.toFloat() * currentWindingFreq * t

                val wx = amplitude * cos(angle)
                val wy = amplitude * sin(angle)

                if (i == 0) wrappedPath.moveTo(wx + center.x, wy + center.y)
                else wrappedPath.lineTo(wx + center.x, wy + center.y)

                sumX += wx
                sumY += wy
                processedCount++
            }
            
            drawPath(
                path = wrappedPath,
                color = colors.accentCyan.copy(alpha = 0.6f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            if (processedCount > 0) {
                val avgX = sumX / processedCount
                val avgY = sumY / processedCount
                drawCircle(colors.accentHell, 4.dp.toPx(), Offset(avgX + center.x, avgY + center.y))
                
                drawLine(
                    colors.textSecondary.copy(alpha = 0.3f),
                    center,
                    Offset(avgX + center.x, avgY + center.y),
                    1.dp.toPx()
                )
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

                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val radiusBase = with(density) { 40.dp.toPx() }
                    val baseN = when (waveType) {
                        WaveType.SINE -> 1.0f
                        WaveType.SQUARE -> (i * 2 + 1).toFloat()
                        WaveType.SAWTOOTH -> (i + 1).toFloat()
                        WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                        else -> 1f
                    }

                    val defaultRadius = when (waveType) {
                        WaveType.SINE -> -1.0f
                        WaveType.SQUARE -> -1.0f * (4f / (baseN * PI.toFloat()))
                        WaveType.SAWTOOTH -> -1.0f * (2f / (baseN * PI.toFloat()))
                        WaveType.TRIANGLE -> -1.0f * (8f / (baseN * baseN * PI.toFloat() * PI.toFloat()))
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first else 0f
                        WaveType.FORMULA -> if (i < formulaCoefficients.size) formulaCoefficients[i].first else 0f
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].amp else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].amp else 0f
                        WaveType.PURE_SIGNAL -> (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f)
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

                            drawCircle(
                                color = colors.accentCyan.copy(alpha = 0.1f),
                                radius = radius * radiusBase,
                                center = center,
                                style = Stroke(width = 1.dp.toPx())
                            )

                            drawLine(
                                colors.textSecondary.copy(alpha = 0.1f),
                                Offset(center.x - (radius * radiusBase) - 10.dp.toPx(), center.y),
                                Offset(center.x + (radius * radiusBase) + 10.dp.toPx(), center.y),
                                1.dp.toPx()
                            )
                            drawLine(
                                colors.textSecondary.copy(alpha = 0.1f),
                                Offset(center.x, center.y - (radius * radiusBase) - 10.dp.toPx()),
                                Offset(center.x, center.y + (radius * radiusBase) + 10.dp.toPx()),
                                1.dp.toPx()
                            )

                            val angle = if (isPaused) 0f else 2 * PI.toFloat() * n * time + phase
                            val end = Offset(
                                center.x + (radius * radiusBase) * cos(angle),
                                center.y + (radius * radiusBase) * sin(angle)
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
                                        onSurface = colors.textPrimary
                                    )
                                ) {
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clip(RoundedCornerShape(AppDesign.radiusCard))
                                            .background(colors.cardSurface.copy(alpha = 0.98f))
                                            .border(
                                                AppDesign.borderThin,
                                                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                                                RoundedCornerShape(AppDesign.radiusCard)
                                            )
                                            .padding(8.dp),
                                    ) {
                                        val isHarmonicPausedVal = isHarmonicPaused(i)
                                        DropdownMenuItem(
                                            text = { Text(if (isHarmonicPausedVal) "Resume" else "Pause", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            onClick = {
                                                onTogglePause(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = if (isHarmonicPausedVal) painterResource(id = R.drawable.caret_forward_outline) else painterResource(id = R.drawable.pause_outline),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = colors.accentCyan
                                                )
                                            }
                                        )

                                        HorizontalDivider(
                                            color = colors.cardBorder.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Frequency", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f Hz", n), fontSize = 11.sp, color = colors.accentCyan, fontWeight = FontWeight.Bold)
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

                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Amplitude", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                                                Text(String.format(Locale.US, "%.1f", radius), fontSize = 11.sp, color = colors.accentViolet, fontWeight = FontWeight.Bold)
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
                                            text = { Text("Reset to Default", fontSize = 13.sp) },
                                            onClick = {
                                                onResetHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Remove", color = colors.accentHell, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            onClick = {
                                                onRemoveHarmonic(i)
                                                menuExpanded = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.trash_outline),
                                                    contentDescription = null,
                                                    tint = colors.accentHell,
                                                    modifier = Modifier.size(18.dp)
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
