package com.example.matharium.fourier

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.matharium.app.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourierSeries() {
    val colors = LocalAppColors.current
    val prefs = LocalAppPrefs.current

    var nTerms by remember { mutableIntStateOf(prefs.fourierNTerms) }
    var waveType by remember { mutableStateOf(WaveType.SQUARE) }

    // Clamp nTerms when switching wave types
    LaunchedEffect(waveType) {
        val maxForCurrent = 50
        if (nTerms > maxForCurrent) {
            nTerms = maxForCurrent
        }
    }
    var running by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var displayMode by remember { mutableStateOf(FourierDisplayMode.CIRCULAR) }
    var windingFrequency by remember { mutableFloatStateOf(1.0f) }

    var formulaString by remember { mutableStateOf(prefs.fourierFormula) }
    var formulaCoefficients by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }

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

    // --- Draw 2D State ---
    val drawingPoints2D = remember {
        val saved = prefs.drawingPoints2D
        val list = mutableStateListOf<Offset>()
        list.addAll(saved)
        list
    }
    var customCoefficients2D by remember { mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList()) }

    // --- SVG State ---
    val svgPoints = remember { mutableStateListOf<Offset>() }
    var svgCoefficients by remember { mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList()) }

    // --- Custom Function State ---
    val customFunctionSignals = remember {
        val list = mutableStateListOf<SignalInstance>()
        list.addAll(prefs.loadFourierSignals(colors.accentCyan))
        list
    }
    var nextSignalId by remember { mutableIntStateOf(customFunctionSignals.maxOfOrNull { it.id }?.plus(1) ?: 1) }
    var isSignalsExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var dftJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun calculateDFT() {
        dftJob?.cancel()
        dftJob = coroutineScope.launch(Dispatchers.Default) {
            val samples = if (waveType == WaveType.FORMULA) {
                val list = mutableListOf<Float>()
                for (i in 0 until samplesCount) {
                    val x = (i.toDouble() / samplesCount) * 2 * kotlin.math.PI
                    // Formula gives value around 0, let's scale it slightly for better default visualization
                    val value = -FourierExpressionEvaluator.evaluate(formulaString, x).toFloat()
                    list.add(value * 80f)
                }
                list
            } else {
                if (drawingPoints.size < samplesCount) return@launch
                drawingPoints.toList()
            }
            
            val coeffs = FourierLogic.performDFT(samples, samplesCount)
            withContext(Dispatchers.Main) {
                if (waveType == WaveType.FORMULA) {
                    formulaCoefficients = coeffs
                } else {
                    customCoefficients = coeffs
                }
            }
        }
    }

    fun calculateDFT2D() {
        dftJob?.cancel()
        dftJob = coroutineScope.launch(Dispatchers.Default) {
            if (drawingPoints2D.isEmpty()) return@launch
            val coeffs = FourierLogic.performComplexDFT(drawingPoints2D.toList())
            withContext(Dispatchers.Main) {
                customCoefficients2D = coeffs
            }
        }
    }

    fun calculateSVGDFT() {
        dftJob?.cancel()
        dftJob = coroutineScope.launch(Dispatchers.Default) {
            if (svgPoints.isEmpty()) return@launch
            val coeffs = FourierLogic.performComplexDFT(svgPoints.toList())
            withContext(Dispatchers.Main) {
                svgCoefficients = coeffs
            }
        }
    }

    val svgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                    if (content != null) {
                        val points = FourierLogic.extractPointsFromSVG(content)
                        withContext(Dispatchers.Main) {
                            svgPoints.clear()
                            svgPoints.addAll(points)
                            calculateSVGDFT()
                            path.clear()
                            time = 0f
                            running = true
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FourierSeries", "Error loading SVG", e)
                    withContext(Dispatchers.Main) {
                        waveType = WaveType.SQUARE
                        android.widget.Toast.makeText(context, "Invalid SVG: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Initialize DFT if drawing points exist
    LaunchedEffect(Unit) {
        if (drawingPoints.any { it != 0f } || formulaString.isNotEmpty()) {
            calculateDFT()
        }
        if (drawingPoints2D.isNotEmpty()) {
            calculateDFT2D()
        }
    }

    // Save state when it changes
    LaunchedEffect(nTerms) { prefs.fourierNTerms = nTerms }
    LaunchedEffect(formulaString, waveType) { 
        prefs.fourierFormula = formulaString
        if (waveType == WaveType.FORMULA) {
            calculateDFT()
        }
    }
    LaunchedEffect(drawingPoints.toList()) { prefs.drawingPoints = drawingPoints.toList() }
    LaunchedEffect(customFunctionSignals.toList()) { prefs.saveFourierSignals(customFunctionSignals.toList()) }

    LaunchedEffect(
        running,
        speed,
        nTerms,
        waveType,
        customCoefficients,
        formulaCoefficients,
        customFunctionSignals.size
    ) {
        if (!running) return@LaunchedEffect
        var lastTime = System.nanoTime()
        while (running) {
            withFrameNanos { frameTime ->
                val elapsedSeconds = (frameTime - lastTime) / 1e9f
                lastTime = frameTime
                val substeps = 2
                val subDt = elapsedSeconds / substeps
                val newPoints = mutableListOf<Offset>()

                repeat(substeps) {
                    time += subDt * speed

                    var currentX = 0f
                    var currentY = 0f
                    val radiusBase = 100f

                    val animationTerms = nTerms

                    if (waveType == WaveType.CUSTOM_FUNCTION) {
                        val limit = animationTerms.coerceAtMost(customFunctionSignals.size)
                        for (i in 0 until limit) {
                            val signal = customFunctionSignals[i]
                            val freq = signal.freq.toFloatOrNull() ?: 0f
                            val amp = signal.amp.toFloatOrNull() ?: 0f
                            val angle = 2 * kotlin.math.PI.toFloat() * freq * time
                            currentX += amp * kotlin.math.cos(angle)
                            currentY += amp * kotlin.math.sin(angle)
                        }
                    } else {
                        for (i in 0 until animationTerms) {
                            if (waveType == WaveType.MY_SIGNAL || waveType == WaveType.FORMULA) {
                                val coeffs = if (waveType == WaveType.FORMULA) formulaCoefficients else customCoefficients
                                if (i < coeffs.size) {
                                    val (amp, phase) = coeffs[i]
                                    val n = i // Using i instead of i+1 because coeffs[0] is now n=0
                                    val angle = 2 * kotlin.math.PI.toFloat() * n * time + phase
                                    currentX += amp * kotlin.math.cos(angle)
                                    currentY += amp * kotlin.math.sin(angle)
                                }
                                continue
                            }

                            if (waveType == WaveType.MY_SIGNAL_2D) {
                                if (i < customCoefficients2D.size) {
                                    val coeff = customCoefficients2D[i]
                                    val angle = 2 * kotlin.math.PI.toFloat() * coeff.freq * time + coeff.phase
                                    currentX += coeff.amp * kotlin.math.cos(angle)
                                    currentY += coeff.amp * kotlin.math.sin(angle)
                                }
                                continue
                            }

                            if (waveType == WaveType.SVG) {
                                if (i < svgCoefficients.size) {
                                    val coeff = svgCoefficients[i]
                                    val angle = 2 * kotlin.math.PI.toFloat() * coeff.freq * time + coeff.phase
                                    currentX += coeff.amp * kotlin.math.cos(angle)
                                    currentY += coeff.amp * kotlin.math.sin(angle)
                                }
                                continue
                            }

                            val n = when (waveType) {
                                WaveType.SINE -> 1f
                                WaveType.SQUARE -> (i * 2 + 1).toFloat()
                                WaveType.SAWTOOTH -> (i + 1).toFloat()
                                WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                                else -> 1f
                            }
                            val radius = when (waveType) {
                                WaveType.SINE -> if (i == 0) -radiusBase else 0f
                                WaveType.SQUARE -> -radiusBase * (4f / (n * kotlin.math.PI.toFloat()))
                                WaveType.SAWTOOTH -> -radiusBase * (2f / (n * kotlin.math.PI.toFloat()))
                                WaveType.TRIANGLE -> -radiusBase * (8f / (n * n * kotlin.math.PI.toFloat() * kotlin.math.PI.toFloat()))
                                else -> 0f
                            }
                            val phase =
                                if (waveType == WaveType.TRIANGLE && i % 2 != 0) kotlin.math.PI.toFloat() else 0f
                            val angle = 2 * kotlin.math.PI.toFloat() * n * time + phase
                            currentX += radius * kotlin.math.cos(angle)
                            currentY += radius * kotlin.math.sin(angle)
                        }
                    }

                    if (displayMode == FourierDisplayMode.COMPLEX) {
                        newPoints.add(0, Offset(currentX, currentY))
                    } else {
                        newPoints.add(0, Offset(time, currentY))
                    }
                }

                path.addAll(0, newPoints)

                if (path.size > 500) {
                    val itemsToRemove = path.size - 500
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
        FourierSettingsCard(
            waveType = waveType,
            onWaveTypeChange = { waveType = it },
            onRunningChange = { running = it },
            speed = speed,
            onSpeedChange = { speed = it },
            windingFrequency = windingFrequency,
            onWindingFrequencyChange = { windingFrequency = it },
            displayMode = displayMode,
            drawingPoints = drawingPoints,
            drawingPoints2D = drawingPoints2D,
            svgPoints = svgPoints,
            customFunctionSignals = customFunctionSignals,
            formulaString = formulaString,
            onFormulaChange = { formulaString = it },
            onCalculateDFT = { calculateDFT() },
            onCalculateDFT2D = { calculateDFT2D() },
            onCalculateSVGDFT = { calculateSVGDFT() },
            onClearPath = { path.clear() },
            onResetTime = { time = 0f },
            onResetHasStarted = { hasStarted = false },
            svgPickerLauncher = svgPickerLauncher,
            nextSignalId = nextSignalId,
            onNextSignalIdChange = { nextSignalId = it },
            isSignalsExpanded = isSignalsExpanded,
            onSignalsExpandedChange = { isSignalsExpanded = it },
            colors = colors,
            prefs = prefs,
            samplesCount = samplesCount
        )

        FourierActionControls(
            running = running,
            onRunningChange = { running = it },
            hasStarted = hasStarted,
            onHasStartedChange = { hasStarted = it },
            onReset = {
                running = false
                hasStarted = false
                time = 0f
                path.clear()
            },
            colors = colors
        )

        FourierVisualizerBox(
            displayMode = displayMode,
            onDisplayModeChange = { displayMode = it },
            waveType = waveType,
            nTerms = nTerms,
            onNTermsChange = { nTerms = it },
            time = time,
            path = path,
            onClearPath = { path.clear() },
            windingFrequency = windingFrequency,
            customCoefficients = customCoefficients,
            customCoefficients2D = customCoefficients2D,
            formulaCoefficients = formulaCoefficients,
            svgCoefficients = svgCoefficients,
            customFunctionSignals = customFunctionSignals,
            colors = colors
        )

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
                    customCoefficients2D = customCoefficients2D,
                    formulaCoefficients = formulaCoefficients,
                    svgCoefficients = svgCoefficients,
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
                    customCoefficients2D = customCoefficients2D,
                    formulaCoefficients = formulaCoefficients,
                    svgCoefficients = svgCoefficients,
                    customFunctionSignals = customFunctionSignals
                )
            }
        }

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
