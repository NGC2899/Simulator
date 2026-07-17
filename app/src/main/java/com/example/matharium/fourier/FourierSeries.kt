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
        val maxForCurrent = 50
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

    // --- Dynamic Tuning State ---
    val pausedHarmonics = remember { mutableStateMapOf<Int, Boolean>() }
    val harmonicFrequencies = remember { mutableStateMapOf<Int, Float>() }
    val harmonicAmplitudes = remember { mutableStateMapOf<Int, Float>() }

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
            
            val coeffs = try {
                FourierLogic.performDFT(samples, samplesCount)
            } catch (e: Exception) {
                Log.e("FourierSeries", "DFT Calculation error", e)
                emptyList()
            }
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
            val coeffs = try {
                FourierLogic.performComplexDFT(drawingPoints2D.toList())
            } catch (e: Exception) {
                Log.e("FourierSeries", "Complex DFT Calculation error", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                customCoefficients2D = coeffs
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
    DisposableEffect(Unit) {
        if (drawingPoints.any { it != 0f } || formulaString.isNotEmpty()) {
            calculateDFT()
        }
        if (drawingPoints2D.isNotEmpty()) {
            calculateDFT2D()
        }
        onDispose {
            dftJob?.cancel()
        }
    }

    // Save state when it changes
    LaunchedEffect(nTerms) { prefs.fourierNTerms = nTerms }
    LaunchedEffect(speed) { prefs.fourierSpeed = speed }
    LaunchedEffect(displayMode) { prefs.fourierDisplayMode = displayMode.name }
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

                    if (waveType == WaveType.PURE_SIGNAL) {
                        val limit = animationTerms.coerceAtMost(customFunctionSignals.size)
                        for (i in 0 until limit) {
                            val signal = customFunctionSignals[i]
                            if (signal.isPaused) continue
                            val freq = harmonicFrequencies[i] ?: signal.freq.toFloatOrNull() ?: 0f
                            val amp = (harmonicAmplitudes[i] ?: signal.amp.toFloatOrNull() ?: 0f) * -1f
                            val phase = kotlin.math.PI.toFloat() / 2f
                            val angle = 2 * kotlin.math.PI.toFloat() * freq * time
                            currentY += amp * kotlin.math.cos((angle - phase).toDouble()).toFloat()
                            currentX += amp * kotlin.math.sin((angle - phase).toDouble()).toFloat()
                        }
                    } else {
                        for (i in 0 until animationTerms) {
                            if (waveType == WaveType.SINE && i > 0) continue
                            if (pausedHarmonics[i] == true) continue

                            if (waveType == WaveType.MY_SIGNAL || waveType == WaveType.FORMULA) {
                                val coeffs = if (waveType == WaveType.FORMULA) formulaCoefficients else customCoefficients
                                if (i < coeffs.size) {
                                    val (origAmp, phase) = coeffs[i]
                                    val n = harmonicFrequencies[i] ?: i.toFloat()
                                    val amp = harmonicAmplitudes[i] ?: origAmp
                                    val angle = 2 * kotlin.math.PI.toFloat() * n * time
                                    currentY += amp * kotlin.math.cos((angle - phase).toDouble()).toFloat()
                                    currentX += amp * kotlin.math.sin((angle - phase).toDouble()).toFloat()
                                }
                                continue
                            }

                            if (waveType == WaveType.MY_SIGNAL_2D) {
                                if (i < customCoefficients2D.size) {
                                    val coeff = customCoefficients2D[i]
                                    val n = harmonicFrequencies[i] ?: coeff.freq.toFloat()
                                    val amp = harmonicAmplitudes[i] ?: coeff.amp
                                    val angle = 2 * kotlin.math.PI.toFloat() * n * time + coeff.phase
                                    currentX += amp * kotlin.math.cos(angle.toDouble()).toFloat()
                                    currentY += amp * kotlin.math.sin(angle.toDouble()).toFloat()
                                }
                                continue
                            }

                            if (waveType == WaveType.SVG) {
                                if (i < svgCoefficients.size) {
                                    val coeff = svgCoefficients[i]
                                    val n = harmonicFrequencies[i] ?: coeff.freq.toFloat()
                                    val amp = harmonicAmplitudes[i] ?: coeff.amp
                                    val angle = 2 * kotlin.math.PI.toFloat() * n * time + coeff.phase
                                    currentX += amp * kotlin.math.cos(angle.toDouble()).toFloat()
                                    currentY += amp * kotlin.math.sin(angle.toDouble()).toFloat()
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

                            val phase = kotlin.math.PI.toFloat() / 2f
                            val defaultAmp = when (waveType) {
                                WaveType.SINE -> -radiusBase
                                WaveType.SQUARE -> -radiusBase * (4f / (baseN * kotlin.math.PI.toFloat()))
                                WaveType.SAWTOOTH -> {
                                    val sign = if (baseN.toInt() % 2 == 0) -1f else 1f
                                    -radiusBase * (2f / (baseN * kotlin.math.PI.toFloat())) * sign
                                }
                                WaveType.TRIANGLE -> {
                                    val sign = if (((baseN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                                    -radiusBase * (8f / (baseN * baseN * kotlin.math.PI.toFloat() * kotlin.math.PI.toFloat())) * sign
                                }
                                else -> 0f
                            }
                            
                            val amp = harmonicAmplitudes[i] ?: defaultAmp
                            val angle = 2 * kotlin.math.PI.toFloat() * n * time
                            currentY += amp * kotlin.math.cos((angle - phase).toDouble()).toFloat()
                            currentX += amp * kotlin.math.sin((angle - phase).toDouble()).toFloat()
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
            onClearCustomCoefficients = { 
                customCoefficients = emptyList()
                running = false
                hasStarted = false
            },
            onClearCustomCoefficients2D = { 
                customCoefficients2D = emptyList()
                running = false
                hasStarted = false
            },
            onClearSVGCoefficients = { 
                svgCoefficients = emptyList()
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
            colors = colors,
            prefs = prefs,
            samplesCount = samplesCount
        )

        val isSimulationEnabled = when (waveType) {
            WaveType.MY_SIGNAL -> drawingPoints.any { it != 0f }
            WaveType.MY_SIGNAL_2D -> drawingPoints2D.isNotEmpty()
            WaveType.PURE_SIGNAL -> customFunctionSignals.isNotEmpty()
            WaveType.FORMULA -> formulaCoefficients.isNotEmpty()
            WaveType.SVG -> svgCoefficients.isNotEmpty()
            else -> true
        }

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
            colors = colors,
            pausedHarmonics = pausedHarmonics,
            harmonicFrequencies = harmonicFrequencies,
            harmonicAmplitudes = harmonicAmplitudes
        )

        val handleRemoveHarmonic: (Int) -> Unit = { index ->
            when (waveType) {
                WaveType.MY_SIGNAL -> {
                    val newList = customCoefficients.toMutableList()
                    if (index < newList.size) {
                        newList[index] = 0f to 0f
                        customCoefficients = newList
                    }
                }
                WaveType.FORMULA -> {
                    val newList = formulaCoefficients.toMutableList()
                    if (index < newList.size) {
                        newList[index] = 0f to 0f
                        formulaCoefficients = newList
                    }
                }
                WaveType.MY_SIGNAL_2D -> {
                    val newList = customCoefficients2D.toMutableList()
                    if (index < newList.size) {
                        newList.removeAt(index)
                        customCoefficients2D = newList
                    }
                }
                WaveType.SVG -> {
                    val newList = svgCoefficients.toMutableList()
                    if (index < newList.size) {
                        newList.removeAt(index)
                        svgCoefficients = newList
                    }
                }
                WaveType.PURE_SIGNAL -> {
                    if (index < customFunctionSignals.size) {
                        customFunctionSignals.removeAt(index)
                    }
                }
                else -> {
                    pausedHarmonics[index] = true
                }
            }
            path.clear()
            time = 0f
        }

        val handleResetHarmonics: () -> Unit = {
            pausedHarmonics.clear()
            harmonicFrequencies.clear()
            customFunctionSignals.forEach { it.isPaused = false }
            path.clear()
            time = 0f
        }

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
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
