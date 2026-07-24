package com.example.matharium.fourier

import android.util.Log
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
import androidx.compose.ui.unit.dp
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
    var waveType by remember {
        mutableStateOf(
            try {
                val saved = prefs.fourierWaveType
                if (saved == "CUSTOM_FUNCTION") WaveType.PURE_SIGNAL else WaveType.valueOf(saved)
            } catch (e: Exception) {
                WaveType.SQUARE
            }
        )
    }

    // Clamp nTerms when switching wave types
    LaunchedEffect(waveType) {
        val maxForCurrent = 250
        if (nTerms > maxForCurrent) {
            nTerms = maxForCurrent
        }
        prefs.fourierWaveType = waveType.name
    }
    var running by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(prefs.fourierSpeed) }
    var displayMode by remember {
        mutableStateOf(
            try {
                FourierDisplayMode.valueOf(prefs.fourierDisplayMode)
            } catch (e: Exception) {
                FourierDisplayMode.CIRCULAR
            }
        )
    }
    var windingFrequency by remember { mutableFloatStateOf(prefs.fourierWindingFrequency) }
    var waveStretch by remember { mutableFloatStateOf(prefs.fourierWaveStretch) }

    var showErrorGradient by remember { mutableStateOf(prefs.fourierShowErrorGradient) }
    var errorSensitivity by remember { mutableFloatStateOf(prefs.fourierErrorSensitivity) }

    var formulaString by remember { mutableStateOf(prefs.fourierFormula) }
    var formulaCoefficients by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var baseFormulaCoefficients by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var symmetryResult by remember { mutableStateOf<FourierLogic.SymmetryResult?>(null) }

    var time by remember { mutableFloatStateOf(0f) }
    val path = remember { mutableStateListOf<PathPoint>() }

    // --- Draw a Wave State ---
    val samplesCount = 1000
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
    var baseCustomCoefficients by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }

    // --- Draw 2D State ---
    val drawingPoints2D = remember {
        val saved = prefs.drawingPoints2D
        val list = mutableStateListOf<Offset>()
        list.addAll(saved)
        list
    }
    var customCoefficients2D by remember { mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList()) }
    var baseCustomCoefficients2D by remember { mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList()) }

    // --- SVG State ---
    val svgPoints = remember { 
        val saved = prefs.fourierSvgPoints
        val list = mutableStateListOf<Offset>()
        list.addAll(saved)
        list
    }
    var svgCoefficients by remember { mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList()) }
    var baseSvgCoefficients by remember { mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList()) }

    // --- Custom Function State ---
    val customFunctionSignals = remember {
        val list = mutableStateListOf<SignalInstance>()
        list.addAll(prefs.loadFourierSignals(colors.accentCyan))
        list
    }
    var nextSignalId by remember { mutableIntStateOf(customFunctionSignals.maxOfOrNull { it.id }?.plus(1) ?: 1) }
    var isSignalsExpanded by remember { mutableStateOf(false) }

    // --- Dynamic Tuning State ---
    val pausedHarmonics = remember { mutableStateMapOf<Int, Boolean>() }
    val removedHarmonics = remember { mutableStateMapOf<Int, Boolean>() }
    val harmonicFrequencies = remember { mutableStateMapOf<Int, Float>() }
    val harmonicAmplitudes = remember { mutableStateMapOf<Int, Float>() }

    // Pre-calculated spectrum data (0..5 Hz) for real-time visualization
    var spectrumData by remember { mutableStateOf<List<FourierLogic.Complex>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    var dftJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusBasePx = with(density) { 100.dp.toPx() }

    fun calculateDFT() {
        dftJob?.cancel()
        dftJob = coroutineScope.launch(Dispatchers.Default) {
            val samples = if (waveType == WaveType.FORMULA) {
                val list = mutableListOf<Float>()
                for (i in 0 until samplesCount) {
                    val x = (i.toDouble() / samplesCount) * 2.0 * kotlin.math.PI - kotlin.math.PI
                    
                    // Formula gives Math-Y (Up is positive).
                    val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
                    val value = if (eval.isFinite()) eval.toFloat() else 0f
                    list.add(value)
                }
                list
            } else {
                if (drawingPoints.size < samplesCount) return@launch
                // Negate drawing points to convert from Android-Y (Down is positive) to Math-Y (Up is positive)
                drawingPoints.map { -it / radiusBasePx }
            }
            
            val coeffs = try {
                FourierLogic.performDFT(samples, samplesCount)
            } catch (e: Exception) {
                Log.e("FourierSeries", "DFT Calculation error", e)
                emptyList()
            }
            
            val symmetry = FourierLogic.detectSymmetry(samples)

            withContext(Dispatchers.Main) {
                symmetryResult = symmetry
                if (waveType == WaveType.FORMULA) {
                    formulaCoefficients = coeffs
                    baseFormulaCoefficients = coeffs
                } else {
                    customCoefficients = coeffs
                    baseCustomCoefficients = coeffs
                }
            }
        }
    }

    fun calculateDFT2D() {
        dftJob?.cancel()
        dftJob = coroutineScope.launch(Dispatchers.Default) {
            if (drawingPoints2D.isEmpty()) return@launch
            // Flip Y for mathematical consistency (CCW rotation)
            val normalizedPoints = drawingPoints2D.map { Offset(it.x / radiusBasePx, -it.y / radiusBasePx) }
            val coeffs = try {
                FourierLogic.performComplexDFT(normalizedPoints)
            } catch (e: Exception) {
                Log.e("FourierSeries", "Complex DFT Calculation error", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                customCoefficients2D = coeffs
                baseCustomCoefficients2D = coeffs
            }
        }
    }

    fun calculateSVGDFT() {
        dftJob?.cancel()
        dftJob = coroutineScope.launch(Dispatchers.Default) {
            if (svgPoints.isEmpty()) return@launch
            val coeffs = try {
                FourierLogic.performComplexDFT(svgPoints.toList())
            } catch (e: Exception) {
                Log.e("FourierSeries", "SVG DFT Calculation error", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                svgCoefficients = coeffs
                baseSvgCoefficients = coeffs
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
                        // Mirror Y for CCW rotation
                        val mirroredPoints = points.map { Offset(it.x, -it.y) }
                        withContext(Dispatchers.Main) {
                            svgPoints.clear()
                            svgPoints.addAll(mirroredPoints)
                            prefs.fourierSvgPoints = mirroredPoints
                            calculateSVGDFT()
                            path.clear()
                            time = 0f
                            running = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FourierSeries", "Error loading SVG", e)
                    withContext(Dispatchers.Main) {
                        waveType = WaveType.SQUARE
                        android.widget.Toast.makeText(context, "Invalid SVG: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Initialize DFT if drawing points exist
    DisposableEffect(Unit) {
        if (drawingPoints.any { it != 0f } || formulaString.isNotEmpty()) {
            calculateDFT()
        }
        if (drawingPoints2D.isNotEmpty()) {
            calculateDFT2D()
        }
        if (svgPoints.isNotEmpty()) {
            calculateSVGDFT()
        }
        onDispose {
            dftJob?.cancel()
        }
    }

    // Save state when it changes
    LaunchedEffect(nTerms) { prefs.fourierNTerms = nTerms }
    LaunchedEffect(speed) { prefs.fourierSpeed = speed }
    LaunchedEffect(windingFrequency) { prefs.fourierWindingFrequency = windingFrequency }
    LaunchedEffect(waveStretch) { prefs.fourierWaveStretch = waveStretch }
    LaunchedEffect(showErrorGradient) { prefs.fourierShowErrorGradient = showErrorGradient }
    LaunchedEffect(errorSensitivity) { prefs.fourierErrorSensitivity = errorSensitivity }
    LaunchedEffect(displayMode) { prefs.fourierDisplayMode = displayMode.name }
    LaunchedEffect(formulaString, waveType) { 
        prefs.fourierFormula = formulaString
        if (waveType == WaveType.FORMULA) {
            calculateDFT()
        }
    }
    LaunchedEffect(drawingPoints.toList()) { prefs.drawingPoints = drawingPoints.toList() }
    LaunchedEffect(customFunctionSignals.toList()) { prefs.saveFourierSignals(customFunctionSignals.toList()) }

    // Asynchronously calculate spectrum data blueprint
    LaunchedEffect(waveType, drawingPoints.toList(), formulaString, customFunctionSignals.toList()) {
        launch(Dispatchers.Default) {
            val samplesCount = 1000
            val samples = when (waveType) {
                WaveType.MY_SIGNAL -> {
                    if (drawingPoints.size == samplesCount) drawingPoints.toList() 
                    else List(samplesCount) { i -> drawingPoints.getOrElse((i.toFloat() / samplesCount * drawingPoints.size).toInt()) { 0f } }
                }
                WaveType.FORMULA -> {
                    List(samplesCount) { i ->
                        val x = (i.toDouble() / samplesCount) * 2.0 * kotlin.math.PI - kotlin.math.PI
                        val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
                        if (eval.isFinite()) eval.toFloat() else 0f
                    }
                }
                WaveType.PURE_SIGNAL -> {
                    List(samplesCount) { i ->
                        val t = i.toFloat() / samplesCount
                        var sum = 0f
                        for (sig in customFunctionSignals) {
                            if (sig.isPaused) continue
                            sum += -sig.cachedAmp * kotlin.math.sin(2 * kotlin.math.PI.toFloat() * sig.cachedFreq * t)
                        }
                        sum
                    }
                }
                WaveType.SINE -> List(samplesCount) { i -> -kotlin.math.sin(2 * kotlin.math.PI.toFloat() * (i.toFloat() / samplesCount)) }
                WaveType.SQUARE -> List(samplesCount) { i -> if ((i.toFloat() / samplesCount) < 0.5f) -1.0f else 1.0f }
                WaveType.SAWTOOTH -> List(samplesCount) { i -> - (2f * ((i.toFloat() / samplesCount + 0.5f) % 1f) - 1f) }
                WaveType.TRIANGLE -> List(samplesCount) { i -> 
                    val f = (i.toFloat() / samplesCount + 0.75f) % 1f
                    if (f < 0.5f) (4f * f - 1f) else (3f - 4f * f)
                }
                else -> emptyList()
            }

            if (samples.isEmpty()) return@launch

            val maxFreq = 5.0f
            val spectrumPoints = 250
            val result = List(spectrumPoints) { i ->
                val f = (i.toFloat() / spectrumPoints) * maxFreq
                var re = 0.0
                var im = 0.0
                // For sharper peaks, we effectively repeat the signal by integrating 
                // over multiple periods, but we must ensure frequency scaling is correct.
                val analyzeWindow = 10.0 // Integration window for sharpness
                for (j in 0 until (samples.size * analyzeWindow).toInt()) {
                    val sampleIdx = j % samples.size
                    val normalizedT = j.toDouble() / samples.size
                    val angle = 2 * kotlin.math.PI * f * normalizedT
                    re += samples[sampleIdx] * kotlin.math.cos(angle)
                    im += samples[sampleIdx] * kotlin.math.sin(angle)
                }
                FourierLogic.Complex(re / (samples.size * analyzeWindow), im / (samples.size * analyzeWindow))
            }
            withContext(Dispatchers.Main) {
                spectrumData = result
            }
        }
    }

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
                val newPoints = mutableListOf<PathPoint>()

                repeat(substeps) {
                    time += subDt * speed

                    var currentX = 0f
                    var currentY = 0f
                    val radiusBase = radiusBasePx

                    val animationTerms = nTerms

                    if (waveType == WaveType.PURE_SIGNAL) {
                        val limit = animationTerms.coerceAtMost(customFunctionSignals.size)
                        for (i in 0 until limit) {
                            val signal = customFunctionSignals[i]
                            if (signal.isPaused) continue
                            val freq = harmonicFrequencies[i] ?: signal.cachedFreq
                            val ampValue = (harmonicAmplitudes[i] ?: signal.cachedAmp) * radiusBase
                            
                            // For custom signals, we use the pure phasor rotation.
                            // We start them all in phase (sine-like) for simplicity
                            val angle = 2 * kotlin.math.PI.toFloat() * freq * time
                            currentX += ampValue * kotlin.math.cos(angle.toDouble()).toFloat()
                            currentY += -ampValue * kotlin.math.sin(angle.toDouble()).toFloat()
                        }
                    } else {
                        for (i in 0 until animationTerms) {
                            if (waveType == WaveType.SINE && i > 0) continue
                            if (removedHarmonics[i] == true) continue
                            if (pausedHarmonics[i] == true) continue

                            if (waveType == WaveType.MY_SIGNAL || waveType == WaveType.FORMULA) {
                                val coeffs = if (waveType == WaveType.FORMULA) formulaCoefficients else customCoefficients
                                if (i < coeffs.size) {
                                    val (amp, phase) = coeffs[i]
                                    val n = harmonicFrequencies[i] ?: i.toFloat()
                                    // Unified CCW Phasor: X = cos, Y = -sin
                                    // DFT gives phase phi such that signal = amp * cos(wt - phi)
                                    // To make Y = -signal, we use angle = wt - phi + pi/2
                                    val totalAngle = 2 * kotlin.math.PI.toFloat() * n * time - phase + (kotlin.math.PI.toFloat() / 2f)
                                    currentX += (amp * radiusBase) * kotlin.math.cos(totalAngle.toDouble()).toFloat()
                                    currentY += -(amp * radiusBase) * kotlin.math.sin(totalAngle.toDouble()).toFloat()
                                }
                                continue
                            }

                            if (waveType == WaveType.MY_SIGNAL_2D) {
                                if (i < customCoefficients2D.size) {
                                    val coeff = customCoefficients2D[i]
                                    val n = harmonicFrequencies[i] ?: coeff.freq.toFloat()
                                    val amp = harmonicAmplitudes[i] ?: coeff.amp
                                    // 2D signals use their complex phase directly
                                    val totalAngle = 2 * kotlin.math.PI.toFloat() * n * time + coeff.phase
                                    currentX += (amp * radiusBase) * kotlin.math.cos(totalAngle.toDouble()).toFloat()
                                    currentY += -(amp * radiusBase) * kotlin.math.sin(totalAngle.toDouble()).toFloat()
                                }
                                continue
                            }

                            if (waveType == WaveType.SVG) {
                                if (i < svgCoefficients.size) {
                                    val coeff = svgCoefficients[i]
                                    val n = harmonicFrequencies[i] ?: coeff.freq.toFloat()
                                    val amp = harmonicAmplitudes[i] ?: coeff.amp
                                    val totalAngle = 2 * kotlin.math.PI.toFloat() * n * time + coeff.phase
                                    currentX += (amp * radiusBase) * kotlin.math.cos(totalAngle.toDouble()).toFloat()
                                    currentY += -(amp * radiusBase) * kotlin.math.sin(totalAngle.toDouble()).toFloat()
                                }
                                continue
                            }

                            val n = harmonicFrequencies[i] ?: when (waveType) {
                                WaveType.SINE -> 1f
                                WaveType.SQUARE -> (i * 2 + 1).toFloat()
                                WaveType.SAWTOOTH -> (i + 1).toFloat()
                                WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                                else -> 1f
                            }
                            
                            val baseN = when (waveType) {
                                WaveType.SINE -> 1f
                                WaveType.SQUARE -> (i * 2 + 1).toFloat()
                                WaveType.SAWTOOTH -> (i + 1).toFloat()
                                WaveType.TRIANGLE -> (i * 2 + 1).toFloat()
                                else -> 1f
                            }

                            val defaultAmp = when (waveType) {
                                WaveType.SINE -> 1.0f
                                WaveType.SQUARE -> 4f / (baseN * kotlin.math.PI.toFloat())
                                WaveType.SAWTOOTH -> (2f / (baseN * kotlin.math.PI.toFloat())) * (if (baseN.toInt() % 2 == 0) -1f else 1f)
                                WaveType.TRIANGLE -> (8f / (baseN * baseN * kotlin.math.PI.toFloat() * kotlin.math.PI.toFloat())) * (if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f)
                                else -> 0f
                            }
                            
                            val amp = harmonicAmplitudes[i] ?: defaultAmp
                            val angle = 2 * kotlin.math.PI.toFloat() * n * time
                            
                            // Analytical waves are built from sines. 
                            // Sin(wt) corresponds to X = cos(wt - pi/2 + pi/2) = cos(wt), Y = -sin(wt)
                            currentX += (amp * radiusBase) * kotlin.math.cos(angle.toDouble()).toFloat()
                            currentY += -(amp * radiusBase) * kotlin.math.sin(angle.toDouble()).toFloat()
                        }
                    }

                    if (displayMode == FourierDisplayMode.COMPLEX) {
                        val approx = Offset(currentX, currentY)
                        val target = getIdealValue(time, waveType, radiusBase, displayMode, drawingPoints, drawingPoints2D, svgPoints, formulaString, customFunctionSignals)
                        
                        val error = if (showErrorGradient) {
                            // For standard 1D signals in complex mode, we only care about approximating the Y component.
                            // The 2D path traced by a one-sided Fourier series doesn't naturally converge to a 1D line.
                            val isExplicitly2D = waveType == WaveType.MY_SIGNAL_2D || 
                                                 waveType == WaveType.SVG || 
                                                 waveType == WaveType.SINE || 
                                                 waveType == WaveType.PURE_SIGNAL
                            
                            if (isExplicitly2D) {
                                (approx - target).getDistance()
                            } else {
                                kotlin.math.abs(approx.y - target.y)
                            }
                        } else 0f
                        
                        newPoints.add(0, PathPoint(approx, error))
                    } else {
                        val approxY = currentY
                        val targetY = getIdealValue(time, waveType, radiusBase, displayMode, drawingPoints, drawingPoints2D, svgPoints, formulaString, customFunctionSignals).y
                        val error = if (showErrorGradient) kotlin.math.abs(approxY - targetY) else 0f
                        newPoints.add(0, PathPoint(Offset(time, approxY), error))
                    }
                }

                path.addAll(0, newPoints)

                if (path.size > 2000) {
                    val itemsToRemove = path.size - 2000
                    repeat(itemsToRemove) { path.removeAt(path.size - 1) }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {

        val isSimulationEnabled = when (waveType) {
            WaveType.MY_SIGNAL -> drawingPoints.any { it != 0f }
            WaveType.MY_SIGNAL_2D -> drawingPoints2D.isNotEmpty()
            WaveType.PURE_SIGNAL -> customFunctionSignals.isNotEmpty()
            WaveType.FORMULA -> formulaCoefficients.isNotEmpty()
            WaveType.SVG -> svgCoefficients.isNotEmpty()
            else -> true
        }

        // --- FIXED TOP SECTION ---
        FourierVisualizerBox(
            displayMode = displayMode,
            onDisplayModeChange = { displayMode = it },
            waveType = waveType,
            nTerms = nTerms,
            onNTermsChange = { nTerms = it },
            time = time,
            path = path,
            showErrorGradient = showErrorGradient,
            errorSensitivity = errorSensitivity,
            waveStretch = waveStretch,
            onClearPath = { path.clear() },
            windingFrequency = windingFrequency,
            customCoefficients = customCoefficients,
            customCoefficients2D = customCoefficients2D,
            formulaCoefficients = formulaCoefficients,
            svgCoefficients = svgCoefficients,
            customFunctionSignals = customFunctionSignals,
            colors = colors,
            pausedHarmonics = pausedHarmonics,
            removedHarmonics = removedHarmonics,
            harmonicFrequencies = harmonicFrequencies,
            harmonicAmplitudes = harmonicAmplitudes
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
            colors = colors,
            enabled = isSimulationEnabled
        )

        // --- SCROLLABLE BOTTOM SECTION ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
        ) {
            FourierSettingsCard(
                waveType = waveType,
                onWaveTypeChange = { waveType = it },
                onRunningChange = { running = it },
                speed = speed,
                onSpeedChange = { speed = it },
                showErrorGradient = showErrorGradient,
                onShowErrorGradientChange = { showErrorGradient = it },
                errorSensitivity = errorSensitivity,
                onErrorSensitivityChange = { errorSensitivity = it },
                windingFrequency = windingFrequency,
                onWindingFrequencyChange = { windingFrequency = it },
                waveStretch = waveStretch,
                onWaveStretchChange = { waveStretch = it },
                displayMode = displayMode,
                drawingPoints = drawingPoints,
                drawingPoints2D = drawingPoints2D,
                svgPoints = svgPoints,
                customFunctionSignals = customFunctionSignals,
                formulaString = formulaString,
                onFormulaChange = { formulaString = it },
                onCalculateDFT = { calculateDFT() },
                onCalculateDFT2D = { calculateDFT2D() },
                onClearCustomCoefficients = {
                    customCoefficients = emptyList()
                    symmetryResult = null
                    running = false
                    hasStarted = false
                },
                onClearCustomCoefficients2D = {
                    customCoefficients2D = emptyList()
                    symmetryResult = null
                    running = false
                    hasStarted = false
                },
                onClearSVGCoefficients = {
                    svgCoefficients = emptyList()
                    symmetryResult = null
                    running = false
                    hasStarted = false
                },
                onClearPath = { path.clear() },
                onResetTime = { time = 0f },
                onResetHasStarted = { hasStarted = false },
                svgPickerLauncher = svgPickerLauncher,
                nextSignalId = nextSignalId,
                onNextSignalIdChange = { nextSignalId = it },
                isSignalsExpanded = isSignalsExpanded,
                onSignalsExpandedChange = { isSignalsExpanded = it },
                symmetryResult = symmetryResult,
                colors = colors,
                prefs = prefs,
                samplesCount = samplesCount
            )

            val handleRemoveHarmonic: (Int) -> Unit = { index ->
                if (waveType == WaveType.PURE_SIGNAL) {
                    if (index < customFunctionSignals.size) {
                        customFunctionSignals.removeAt(index)
                    }
                } else {
                    removedHarmonics[index] = true
                }
                path.clear()
                time = 0f
            }

            val handleResetHarmonic: (Int) -> Unit = { index ->
                pausedHarmonics.remove(index)
                removedHarmonics.remove(index)
                harmonicFrequencies.remove(index)
                harmonicAmplitudes.remove(index)
                if (waveType == WaveType.PURE_SIGNAL && index < customFunctionSignals.size) {
                    customFunctionSignals[index].freq = "1.0"
                    customFunctionSignals[index].amp = "0.5"
                    customFunctionSignals[index].isPaused = false
                    customFunctionSignals[index].updateCache()
                }
                path.clear()
                time = 0f
            }

            val handleResetHarmonics: () -> Unit = {
                pausedHarmonics.clear()
                removedHarmonics.clear()
                harmonicFrequencies.clear()
                harmonicAmplitudes.clear()
                
                when (waveType) {
                    WaveType.MY_SIGNAL -> {
                        customCoefficients = baseCustomCoefficients
                    }
                    WaveType.MY_SIGNAL_2D -> {
                        customCoefficients2D = baseCustomCoefficients2D
                    }
                    WaveType.SVG -> {
                        svgCoefficients = baseSvgCoefficients
                    }
                    WaveType.FORMULA -> {
                        formulaCoefficients = baseFormulaCoefficients
                    }
                    WaveType.PURE_SIGNAL -> {
                        customFunctionSignals.clear()
                        customFunctionSignals.addAll(prefs.loadFourierSignals(colors.accentCyan))
                    }
                    else -> {}
                }

                path.clear()
                time = 0f
            }

            when (displayMode) {
                FourierDisplayMode.WRAPPING -> {
                    FrequencyDomainGraph(
                        spectrumData = spectrumData,
                        colors = colors,
                        currentWindingFreq = windingFrequency,
                        time = time
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
                        customFunctionSignals = customFunctionSignals,
                        onRemoveHarmonic = handleRemoveHarmonic,
                        onTogglePause = { index ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].isPaused = !customFunctionSignals[index].isPaused
                                }
                            } else {
                                pausedHarmonics[index] = !(pausedHarmonics[index] ?: false)
                            }
                        },
                        isHarmonicPaused = { index ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) customFunctionSignals[index].isPaused else false
                            } else {
                                pausedHarmonics[index] ?: false
                            }
                        },
                        onFrequencyChange = { index, newFreq ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].freq = String.format(java.util.Locale.US, "%.2f", newFreq)
                                    customFunctionSignals[index].updateCache()
                                }
                            } else {
                                harmonicFrequencies[index] = newFreq
                            }
                            path.clear()
                        },
                        getHarmonicFrequency = { index, default ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].freq.toFloatOrNull() ?: default
                                } else default
                            } else {
                                harmonicFrequencies[index] ?: default
                            }
                        },
                        onAmplitudeChange = { index, newAmp ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].amp = String.format(java.util.Locale.US, "%.2f", newAmp)
                                    customFunctionSignals[index].updateCache()
                                }
                            } else {
                                harmonicAmplitudes[index] = newAmp
                            }
                            path.clear()
                        },
                        getHarmonicAmplitude = { index, default ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].amp.toFloatOrNull() ?: default
                                } else default
                            } else {
                                harmonicAmplitudes[index] ?: default
                            }
                        },
                        removedHarmonics = removedHarmonics,
                        onResetHarmonic = handleResetHarmonic,
                        onResetHarmonics = handleResetHarmonics
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
                        customFunctionSignals = customFunctionSignals,
                        onRemoveHarmonic = handleRemoveHarmonic,
                        onTogglePause = { index ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].isPaused = !customFunctionSignals[index].isPaused
                                }
                            } else {
                                pausedHarmonics[index] = !(pausedHarmonics[index] ?: false)
                            }
                        },
                        isHarmonicPaused = { index ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) customFunctionSignals[index].isPaused else false
                            } else {
                                pausedHarmonics[index] ?: false
                            }
                        },
                        onFrequencyChange = { index, newFreq ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].freq = String.format(java.util.Locale.US, "%.2f", newFreq)
                                    customFunctionSignals[index].updateCache()
                                }
                            } else {
                                harmonicFrequencies[index] = newFreq
                            }
                            path.clear()
                        },
                        getHarmonicFrequency = { index, default ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].freq.toFloatOrNull() ?: default
                                } else default
                            } else {
                                harmonicFrequencies[index] ?: default
                            }
                        },
                        onAmplitudeChange = { index, newAmp ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].amp = String.format(java.util.Locale.US, "%.2f", newAmp)
                                    customFunctionSignals[index].updateCache()
                                }
                            } else {
                                harmonicAmplitudes[index] = newAmp
                            }
                            path.clear()
                        },
                        getHarmonicAmplitude = { index, default ->
                            if (waveType == WaveType.PURE_SIGNAL) {
                                if (index < customFunctionSignals.size) {
                                    customFunctionSignals[index].amp.toFloatOrNull() ?: default
                                } else default
                            } else {
                                harmonicAmplitudes[index] ?: default
                            }
                        },
                        removedHarmonics = removedHarmonics,
                        onResetHarmonic = handleResetHarmonic,
                        onResetHarmonics = handleResetHarmonics
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
                        fontSize = AppDesign.textBodyLarge,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

private fun getIdealValue(
    time: Float,
    waveType: WaveType,
    radiusBase: Float,
    displayMode: FourierDisplayMode,
    drawingPoints: List<Float>,
    drawingPoints2D: List<Offset>,
    svgPoints: List<Offset>,
    formulaString: String,
    customFunctionSignals: List<SignalInstance>
): Offset {
    val t = (time % 1f + 1f) % 1f
    val angle = 2 * kotlin.math.PI.toFloat() * t
    return when (waveType) {
        WaveType.SINE -> {
            val y = -radiusBase * kotlin.math.sin(angle.toDouble()).toFloat()
            if (displayMode == FourierDisplayMode.COMPLEX) {
                Offset(radiusBase * kotlin.math.cos(angle.toDouble()).toFloat(), y)
            } else Offset(time, y)
        }
        WaveType.SQUARE -> {
            val y = if (t % 1f < 0.5f) -radiusBase else radiusBase
            if (displayMode == FourierDisplayMode.COMPLEX) Offset(0f, y) else Offset(time, y)
        }
        WaveType.SAWTOOTH -> {
            // Fourier series: 2/pi * sum((-1)^(n+1) * sin(nwt)/n)
            // Starts at 0, goes up to 1, then jumps to -1.
            // Normalized t goes from 0 to 1.
            val fraction = (t + 0.5f) % 1f
            val y = -radiusBase * (2f * fraction - 1f)
            if (displayMode == FourierDisplayMode.COMPLEX) Offset(0f, y) else Offset(time, y)
        }
        WaveType.TRIANGLE -> {
            // Fourier series: sum(sin terms). Peaks at t=0.25 and t=0.75
            val fraction = (t + 0.75f) % 1f
            val y = radiusBase * (if (fraction < 0.5f) (4f * fraction - 1f) else (3f - 4f * fraction))
            if (displayMode == FourierDisplayMode.COMPLEX) Offset(0f, y) else Offset(time, y)
        }
        WaveType.PURE_SIGNAL -> {
            var sumX = 0f
            var sumY = 0f
            for (signal in customFunctionSignals) {
                if (signal.isPaused) continue
                val freq = signal.freq.toFloatOrNull() ?: 0f
                val amp = (signal.amp.toFloatOrNull() ?: 0f) * radiusBase
                val angleVal = 2 * kotlin.math.PI.toFloat() * freq * time
                sumY += -amp * kotlin.math.sin(angleVal.toDouble()).toFloat()
                sumX += amp * kotlin.math.cos(angleVal.toDouble()).toFloat()
            }
            if (displayMode == FourierDisplayMode.COMPLEX) Offset(sumX, sumY) else Offset(time, sumY)
        }
        WaveType.MY_SIGNAL -> {
            val y = if (drawingPoints.isNotEmpty()) {
                val idx = (t * (drawingPoints.size - 1)).toInt()
                drawingPoints[idx]
            } else 0f
            if (displayMode == FourierDisplayMode.COMPLEX) Offset(0f, y) else Offset(time, y)
        }
        WaveType.FORMULA -> {
            val x = (t.toDouble() * 2.0 * kotlin.math.PI - kotlin.math.PI)
            // We treat 1.0 in math as 1 unit.
            val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
            val y = if (eval.isFinite()) -eval.toFloat() * radiusBase else 0f
            if (displayMode == FourierDisplayMode.COMPLEX) Offset(0f, y) else Offset(time, y)
        }
        WaveType.MY_SIGNAL_2D -> {
            if (drawingPoints2D.isNotEmpty()) {
                val idx = (t * (drawingPoints2D.size - 1)).toInt()
                // drawingPoints2D are stored in screen units, but simulation was updated to use units.
                // However, they were already being used directly. 
                // Let's ensure they are consistent.
                drawingPoints2D[idx]
            } else Offset(0f, 0f)
        }
        WaveType.SVG -> {
            if (svgPoints.isNotEmpty()) {
                val idx = (t * (svgPoints.size - 1)).toInt()
                val pt = svgPoints[idx]
                Offset(pt.x * radiusBase, pt.y * radiusBase)
            } else Offset(0f, 0f)
        }
        else -> Offset(0f, 0f)
    }
}
