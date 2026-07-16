package com.example.matharium.fourier

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.app.*
import com.example.matharium.R
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
    customFunctionSignals: List<SignalInstance>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxTerms = when (waveType) {
        WaveType.PURE_SIGNAL -> nTerms.coerceAtMost(customFunctionSignals.size)
        WaveType.MY_SIGNAL_2D -> nTerms.coerceAtMost(customCoefficients2D.size)
        WaveType.FORMULA -> nTerms.coerceAtMost(formulaCoefficients.size)
        WaveType.SVG -> nTerms.coerceAtMost(svgCoefficients.size)
        else -> nTerms
    }

    val displayTerms = if (isExpanded) maxTerms.coerceAtMost(100) else maxTerms.coerceAtMost(6)

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(animationSpec = tween(AppDesign.animDurationStandard)),
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
            Spacer(Modifier.height(AppDesign.radiusSmall))
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
                        WaveType.MY_SIGNAL -> i.toFloat()
                        WaveType.FORMULA -> i.toFloat()
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].freq.toFloat() else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].freq.toFloat() else 0f
                        WaveType.PURE_SIGNAL -> customFunctionSignals[i].freq.toFloatOrNull() ?: 0f
                    }

                    if (waveType == WaveType.SINE && i > 0) continue

                    val radiusBase = 30f

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
                            val (amp, phase) = when (waveType) {
                                WaveType.SINE -> Pair(-radiusBase, PI.toFloat() / 2f)
                                WaveType.SQUARE -> Pair(-radiusBase * (4f / (n * PI.toFloat())), PI.toFloat() / 2f)
                                WaveType.SAWTOOTH -> {
                                    val sign = if (n.toInt() % 2 == 0) -1f else 1f
                                    Pair(-radiusBase * (2f / (n * PI.toFloat())) * sign, PI.toFloat() / 2f)
                                }
                                WaveType.TRIANGLE -> {
                                    val sign = if (((n.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                                    Pair(-radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat())) * sign, PI.toFloat() / 2f)
                                }
                                WaveType.MY_SIGNAL -> if (i < customCoefficients.size) (customCoefficients[i].first * (radiusBase / 100f) to customCoefficients[i].second) else (0f to 0f)
                                WaveType.FORMULA -> if (i < formulaCoefficients.size) (formulaCoefficients[i].first * (radiusBase / 100f) to formulaCoefficients[i].second) else (0f to 0f)
                                WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) (customCoefficients2D[i].amp * (radiusBase / 100f) to customCoefficients2D[i].phase) else (0f to 0f)
                                WaveType.SVG -> if (i < svgCoefficients.size) (svgCoefficients[i].amp * (radiusBase / 100f) to svgCoefficients[i].phase) else (0f to 0f)
                                WaveType.PURE_SIGNAL -> ((customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * (radiusBase / 100f) to 0f)
                                else -> (0f to 0f)
                            }

                            val path = Path()
                            val samples = 150
                            for (s in 0..samples) {
                                val x = (s.toFloat() / samples) * size.width
                                val waveT = time - (1f - s.toFloat() / samples) * 2f
                                val angle = 2 * PI.toFloat() * n * waveT
                                val y = if (waveType == WaveType.MY_SIGNAL_2D || waveType == WaveType.SVG || waveType == WaveType.PURE_SIGNAL) {
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
                enter = expandVertically(animationSpec = tween(AppDesign.animDurationStandard)) + fadeIn(
                    animationSpec = tween(
                        AppDesign.animDurationStandard
                    )
                ),
                exit = shrinkVertically(animationSpec = tween(AppDesign.animDurationStandard)) + fadeOut(
                    animationSpec = tween(
                        AppDesign.animDurationStandard
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
fun SignalField(
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
                val freqSteps = 50 // Optimized for mobile

                for (s in 0..freqSteps) {
                    val freq = (s.toFloat() / freqSteps) * maxFreq

                    var sumX = 0f
                    var processedCount = 0
                    // Match physics substeps (step 2) for stability and consistent sampling
                    for (i in path.indices step 2) {
                        val point = path[i]
                        val t = point.x
                        val ft = point.y
                        sumX += ft * cos(2 * PI.toFloat() * freq * t)
                        processedCount++
                    }

                    val avgX = if (processedCount > 0) sumX / processedCount else 0f
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
    customFunctionSignals: List<SignalInstance>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxTerms = when (waveType) {
        WaveType.PURE_SIGNAL -> nTerms.coerceAtMost(customFunctionSignals.size)
        WaveType.MY_SIGNAL_2D -> nTerms.coerceAtMost(customCoefficients2D.size)
        WaveType.FORMULA -> nTerms.coerceAtMost(formulaCoefficients.size)
        WaveType.SVG -> nTerms.coerceAtMost(svgCoefficients.size)
        else -> nTerms
    }

    val displayTerms = if (isExpanded) maxTerms.coerceAtMost(100) else maxTerms.coerceAtMost(6)

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.radiusLarge)
                .animateContentSize(animationSpec = tween(AppDesign.animDurationStandard)),
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
                        WaveType.MY_SIGNAL -> i.toFloat()
                        WaveType.FORMULA -> i.toFloat()
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].freq.toFloat() else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].freq.toFloat() else 0f
                        WaveType.PURE_SIGNAL -> customFunctionSignals[i].freq.toFloatOrNull() ?: 0f
                    }

                    if (waveType == WaveType.SINE && i > 0) continue

                    val radiusBase = 40f
                    val radius = when (waveType) {
                        WaveType.SINE -> -radiusBase
                        WaveType.SQUARE -> -radiusBase * (4f / (n * PI.toFloat()))
                        WaveType.SAWTOOTH -> -radiusBase * (2f / (n * PI.toFloat()))
                        WaveType.TRIANGLE -> -radiusBase * (8f / (n * n * PI.toFloat() * PI.toFloat()))
                        WaveType.MY_SIGNAL -> if (i < customCoefficients.size) customCoefficients[i].first * (radiusBase / 100f) else 0f
                        WaveType.FORMULA -> if (i < formulaCoefficients.size) formulaCoefficients[i].first * (radiusBase / 100f) else 0f
                        WaveType.MY_SIGNAL_2D -> if (i < customCoefficients2D.size) customCoefficients2D[i].amp * (radiusBase / 100f) else 0f
                        WaveType.SVG -> if (i < svgCoefficients.size) svgCoefficients[i].amp * (radiusBase / 100f) else 0f
                        WaveType.PURE_SIGNAL -> (customFunctionSignals[i].amp.toFloatOrNull() ?: 0f) * (radiusBase / 100f)
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
