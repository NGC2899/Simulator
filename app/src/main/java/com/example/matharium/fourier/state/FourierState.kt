package com.example.matharium.fourier.state

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.*
import kotlinx.coroutines.*

class FourierState(
    val prefs: AppPreferences,
    val scope: CoroutineScope,
    val samplesCount: Int,
    val radiusBasePx: Float,
    val defaultSignalColorArgb: Int
) {
    var nTerms by mutableIntStateOf(prefs.fourierNTerms)
    var waveType by mutableStateOf(
        try {
            val saved = prefs.fourierWaveType
            if (saved == "CUSTOM_FUNCTION") WaveType.PURE_SIGNAL else WaveType.valueOf(saved)
        } catch (e: Exception) {
            WaveType.SQUARE
        }
    )

    var running by mutableStateOf(false)
    var hasStarted by mutableStateOf(false)
    var speed by mutableFloatStateOf(prefs.fourierSpeed)
    var displayMode by mutableStateOf(
        try {
            FourierDisplayMode.valueOf(prefs.fourierDisplayMode)
        } catch (e: Exception) {
            FourierDisplayMode.CIRCULAR
        }
    )
    var windingFrequency by mutableFloatStateOf(prefs.fourierWindingFrequency)
    var waveStretch by mutableFloatStateOf(prefs.fourierWaveStretch)

    var showErrorGradient by mutableStateOf(prefs.fourierShowErrorGradient)
    var errorSensitivity by mutableFloatStateOf(prefs.fourierErrorSensitivity)

    var formulaString by mutableStateOf(prefs.fourierFormula)
    var formulaCoefficients by mutableStateOf<List<Pair<Float, Float>>>(emptyList())
    var baseFormulaCoefficients by mutableStateOf<List<Pair<Float, Float>>>(emptyList())
    var symmetryResult by mutableStateOf<FourierLogic.SymmetryResult?>(null)

    var time by mutableFloatStateOf(0f)
    
    // TRAIL OPTIMIZATION:
    // Using a list but adding to the END to avoid O(N) shifts.
    // The UI will handle the "newest first" logic during drawing.
    val path = mutableStateListOf<PathPoint>()

    val drawingPoints = mutableStateListOf<Float>().apply {
        val saved = prefs.drawingPoints
        if (saved.isNotEmpty()) {
            addAll(saved)
        } else {
            repeat(samplesCount) { add(0f) }
        }
    }
    var drawingVersion by mutableIntStateOf(0)
    var harmonicVersion by mutableIntStateOf(0)
    var customCoefficients by mutableStateOf<List<Pair<Float, Float>>>(prefs.customCoefficients)
    var baseCustomCoefficients by mutableStateOf<List<Pair<Float, Float>>>(prefs.customCoefficients)

    val drawingPoints2D = mutableStateListOf<Offset>().apply {
        val saved = prefs.drawingPoints2D
        if (saved.isNotEmpty()) {
            addAll(saved)
            scope.launch { calculateDFT2D() }
        }
    }
    var drawing2DVersion by mutableIntStateOf(0)
    val resampledPoints2D = mutableStateListOf<Offset>()
    var customCoefficients2D by mutableStateOf<List<FourierLogic.ComplexCoeff>>(prefs.customCoefficients2D)
    var baseCustomCoefficients2D by mutableStateOf<List<FourierLogic.ComplexCoeff>>(prefs.customCoefficients2D)

    val svgPoints = mutableStateListOf<Offset>().apply {
        addAll(prefs.fourierSvgPoints)
    }
    var svgCoefficients by mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList())
    var baseSvgCoefficients by mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList())

    val customFunctionSignals = mutableStateListOf<SignalInstance>().apply {
        addAll(prefs.loadFourierSignals(defaultSignalColorArgb))
    }
    var nextSignalId by mutableIntStateOf(customFunctionSignals.maxOfOrNull { it.id }?.plus(1) ?: 1)
    var isSignalsExpanded by mutableStateOf(false)

    val pausedHarmonics = mutableStateMapOf<Int, Boolean>()
    val removedHarmonics = mutableStateMapOf<Int, Boolean>()
    val harmonicFrequencies = mutableStateMapOf<Int, Float>()
    val harmonicAmplitudes = mutableStateMapOf<Int, Float>()
    val harmonicPhases = mutableStateMapOf<Int, Float>()

    var spectrumData by mutableStateOf<List<FourierLogic.Complex>>(emptyList())

    private var dftJob: Job? = null
    private var spectrumJob: Job? = null

    fun clearOverrides() {
        pausedHarmonics.clear()
        removedHarmonics.clear()
        harmonicFrequencies.clear()
        harmonicAmplitudes.clear()
        harmonicPhases.clear()
        harmonicVersion++
    }

    fun calculateDFT() {
        clearOverrides()
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            val samples = if (waveType == WaveType.FORMULA) {
                val list = mutableListOf<Float>()
                for (i in 0 until samplesCount) {
                    val x = (i.toDouble() / samplesCount) * 2.0 * kotlin.math.PI - kotlin.math.PI
                    val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
                    val value = if (eval.isFinite()) eval.toFloat() else 0f
                    list.add(value)
                }
                list
            } else {
                if (drawingPoints.size < samplesCount) return@launch
                drawingPoints.map { -it / radiusBasePx }
            }

            val coeffs = try {
                FourierLogic.performDFT(samples, samplesCount)
            } catch (e: Exception) {
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
                    prefs.customCoefficients = coeffs
                }
            }
        }
    }

    fun calculateDFT2D() {
        clearOverrides()
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            if (drawingPoints2D.isEmpty()) return@launch
            val resampled = FourierLogic.resamplePath(drawingPoints2D.toList(), 1000)
            val normalizedPoints = resampled.map { Offset(it.x / radiusBasePx, -it.y / radiusBasePx) }
            val coeffs = try {
                FourierLogic.performComplexDFT(normalizedPoints)
            } catch (e: Exception) {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                resampledPoints2D.clear()
                resampledPoints2D.addAll(resampled)
                customCoefficients2D = coeffs
                baseCustomCoefficients2D = coeffs
                prefs.customCoefficients2D = coeffs
            }
        }
    }

    fun calculateSVGDFT() {
        clearOverrides()
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            if (svgPoints.isEmpty()) return@launch
            val coeffs = try {
                FourierLogic.performComplexDFT(svgPoints.toList())
            } catch (e: Exception) {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                svgCoefficients = coeffs
                baseSvgCoefficients = coeffs
            }
        }
    }

    fun resetSimulation() {
        running = false
        hasStarted = false
        time = 0f
        path.clear()
    }

    fun updateSpectrum() {
        spectrumJob?.cancel()
        spectrumJob = scope.launch(Dispatchers.Default) {
            val samples: List<FourierLogic.Complex> = when (waveType) {
                WaveType.SINE, WaveType.SQUARE, WaveType.SAWTOOTH, WaveType.TRIANGLE,
                WaveType.MY_SIGNAL, WaveType.FORMULA, WaveType.MY_SIGNAL_2D, WaveType.SVG, WaveType.PURE_SIGNAL -> {
                    val limit = if (waveType == WaveType.SINE) 1 else nTerms
                    
                    List(samplesCount) { step ->
                        val t = step.toFloat() / samplesCount
                        var sumX = 0.0
                        var sumY = 0.0
                        
                        for (i in 0 until limit) {
                            if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue
                            
                            val defaultN: Float
                            val defaultAmp: Float
                            val analyzedPhase: Float

                            when (waveType) {
                                WaveType.SINE -> { defaultN = 1f; defaultAmp = 1f; analyzedPhase = 0f }
                                WaveType.SQUARE -> {
                                    defaultN = (i * 2 + 1).toFloat()
                                    defaultAmp = 4f / (defaultN * kotlin.math.PI.toFloat()); analyzedPhase = 0f
                                }
                                WaveType.SAWTOOTH -> {
                                    defaultN = (i + 1).toFloat()
                                    val sign = if (defaultN.toInt() % 2 == 0) -1f else 1f
                                    defaultAmp = (2f / (defaultN * kotlin.math.PI.toFloat())) * sign; analyzedPhase = 0f
                                }
                                WaveType.TRIANGLE -> {
                                    defaultN = (i * 2 + 1).toFloat()
                                    val sign = if (((defaultN.toInt() - 1) / 2) % 2 != 0) -1f else 1f
                                    defaultAmp = (8f / (defaultN * defaultN * kotlin.math.PI.toFloat() * kotlin.math.PI.toFloat())) * sign; analyzedPhase = 0f
                                }
                                WaveType.MY_SIGNAL -> {
                                    defaultN = i.toFloat()
                                    if (i < customCoefficients.size) {
                                        defaultAmp = customCoefficients[i].first
                                        analyzedPhase = -customCoefficients[i].second + (kotlin.math.PI.toFloat() / 2f)
                                    } else { defaultAmp = 0f; analyzedPhase = 0f }
                                }
                                WaveType.FORMULA -> {
                                    defaultN = i.toFloat()
                                    if (i < formulaCoefficients.size) {
                                        defaultAmp = formulaCoefficients[i].first
                                        analyzedPhase = -formulaCoefficients[i].second + (kotlin.math.PI.toFloat() / 2f)
                                    } else { defaultAmp = 0f; analyzedPhase = 0f }
                                }
                                WaveType.MY_SIGNAL_2D -> {
                                    if (i < customCoefficients2D.size) {
                                        val c = customCoefficients2D[i]
                                        defaultAmp = c.amp; analyzedPhase = c.phase; defaultN = c.freq.toFloat()
                                    } else { defaultAmp = 0f; analyzedPhase = 0f; defaultN = 0f }
                                }
                                WaveType.SVG -> {
                                    if (i < svgCoefficients.size) {
                                        val c = svgCoefficients[i]
                                        defaultAmp = c.amp; analyzedPhase = c.phase; defaultN = c.freq.toFloat()
                                    } else { defaultAmp = 0f; analyzedPhase = 0f; defaultN = 0f }
                                }
                                WaveType.PURE_SIGNAL -> {
                                    if (i < customFunctionSignals.size) {
                                        val s = customFunctionSignals[i]
                                        if (s.isPaused) { defaultAmp = 0f; analyzedPhase = 0f; defaultN = 0f }
                                        else { defaultAmp = s.amp.toFloatOrNull() ?: 0f; analyzedPhase = s.cachedPhase; defaultN = s.freq.toFloatOrNull() ?: 0f }
                                    } else { defaultAmp = 0f; analyzedPhase = 0f; defaultN = 0f }
                                }
                                else -> { defaultAmp = 0f; analyzedPhase = 0f; defaultN = 0f }
                            }
                            
                            val amp = (harmonicAmplitudes[i] ?: defaultAmp).toDouble()
                            val phase = (harmonicPhases[i] ?: analyzedPhase).toDouble()
                            val n = (harmonicFrequencies[i] ?: defaultN).toDouble()
                            
                            val angle = 2 * kotlin.math.PI * n * t + phase
                            sumX += amp * kotlin.math.cos(angle)
                            sumY += -amp * kotlin.math.sin(angle)
                        }
                        FourierLogic.Complex(sumX, sumY)
                    }
                }
            }

            if (samples.isEmpty()) return@launch

            val maxFreq = 5.0f
            val spectrumPoints = 500
            val result = List(spectrumPoints) { i ->
                val f = (i.toFloat() / spectrumPoints) * maxFreq
                var sumRe = 0.0
                var sumIm = 0.0
                val analyzeWindow = 10.0
                val totalSteps = (samples.size * analyzeWindow).toInt()
                for (j in 0 until totalSteps) {
                    val sampleIdx = j % samples.size
                    val normalizedT = j.toDouble() / samples.size
                    val angle = 2 * kotlin.math.PI * f * normalizedT
                    val rotRe = kotlin.math.cos(angle)
                    val rotIm = -kotlin.math.sin(angle)
                    
                    val s = samples[sampleIdx]
                    sumRe += s.re * rotRe - s.im * rotIm
                    sumIm += s.re * rotIm + s.im * rotRe
                }
                FourierLogic.Complex(sumRe / totalSteps, sumIm / totalSteps)
            }
            withContext(Dispatchers.Main) {
                spectrumData = result
            }
        }
    }

    /**
     * Simulation Heartbeat.
     * Runs the physics loop using the frame clock.
     */
    suspend fun runSimulation() {
        if (!running) return
        var lastTime = System.nanoTime()
        while (running) {
            withFrameNanos { frameTime ->
                updatePhysics(frameTime, lastTime)
                lastTime = frameTime
            }
        }
    }

    private fun updatePhysics(frameTime: Long, lastTime: Long) {
        val elapsedSeconds = (frameTime - lastTime) / 1e9f
        val substeps = 2
        val subDt = elapsedSeconds / substeps

        repeat(substeps) {
            time += subDt * speed

            var currentX = 0f
            var currentY = 0f
            val radiusBase = radiusBasePx
            val twoPi = 2.0 * kotlin.math.PI

            if (waveType == WaveType.PURE_SIGNAL) {
                val limit = nTerms.coerceAtMost(customFunctionSignals.size)
                for (i in 0 until limit) {
                    if (removedHarmonics[i] == true) continue
                    val signal = customFunctionSignals[i]
                    if (signal.isPaused) continue
                    val freq = (harmonicFrequencies[i] ?: signal.cachedFreq).toDouble()
                    val ampValue = (harmonicAmplitudes[i] ?: signal.cachedAmp) * radiusBase
                    val phase = (harmonicPhases[i] ?: signal.cachedPhase).toDouble()
                    val angle = twoPi * freq * time + phase
                    currentX += (ampValue * kotlin.math.cos(angle)).toFloat()
                    currentY += -(ampValue * kotlin.math.sin(angle)).toFloat()
                }
            } else {
                for (i in 0 until nTerms) {
                    if (waveType == WaveType.SINE && i > 0) continue
                    if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue

                    if (waveType == WaveType.MY_SIGNAL || waveType == WaveType.FORMULA) {
                        val coeffs = if (waveType == WaveType.FORMULA) formulaCoefficients else customCoefficients
                        if (i < coeffs.size) {
                            val c = coeffs[i]
                            val amp = harmonicAmplitudes[i] ?: c.first
                            val phase = (harmonicPhases[i] ?: c.second).toDouble()
                            val n = (harmonicFrequencies[i] ?: i.toFloat()).toDouble()
                            val totalAngle = twoPi * n * time - phase + (kotlin.math.PI / 2.0)
                            currentX += (amp * radiusBase * kotlin.math.cos(totalAngle)).toFloat()
                            currentY += -(amp * radiusBase * kotlin.math.sin(totalAngle)).toFloat()
                        }
                        continue
                    }

                    if (waveType == WaveType.MY_SIGNAL_2D || waveType == WaveType.SVG) {
                        val coeffs = if (waveType == WaveType.SVG) svgCoefficients else customCoefficients2D
                        if (i < coeffs.size) {
                            val coeff = coeffs[i]
                            val n = (harmonicFrequencies[i] ?: coeff.freq.toFloat()).toDouble()
                            val amp = harmonicAmplitudes[i] ?: coeff.amp
                            val phase = (harmonicPhases[i] ?: coeff.phase).toDouble()
                            val totalAngle = twoPi * n * time + phase
                            currentX += (amp * radiusBase * kotlin.math.cos(totalAngle)).toFloat()
                            currentY += -(amp * radiusBase * kotlin.math.sin(totalAngle)).toFloat()
                        }
                        continue
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
                    val n = (harmonicFrequencies[i] ?: baseN).toDouble()
                    val amp = harmonicAmplitudes[i] ?: defaultAmp
                    val phase = (harmonicPhases[i] ?: 0f).toDouble()
                    val angle = twoPi * n * time + phase
                    currentX += (amp * radiusBase * kotlin.math.cos(angle)).toFloat()
                    currentY += -(amp * radiusBase * kotlin.math.sin(angle)).toFloat()
                }
            }

            val approxX: Float
            val approxY: Float
            val error: Float
            if (displayMode == FourierDisplayMode.COMPLEX) {
                approxX = currentX
                approxY = currentY
                val target = FourierLogic.getIdealValue(
                    time, waveType, radiusBase, displayMode,
                    drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                    formulaString, customFunctionSignals,
                    harmonicFrequencies, harmonicAmplitudes, harmonicPhases
                )
                error = if (showErrorGradient) {
                    val dx = approxX - target.x
                    val dy = approxY - target.y
                    kotlin.math.sqrt(dx * dx + dy * dy)
                } else 0f
                path.add(PathPoint(Offset(approxX, approxY), error))
            } else {
                approxX = time
                approxY = currentY
                val targetY = FourierLogic.getIdealValue(
                    time, waveType, radiusBase, displayMode,
                    drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                    formulaString, customFunctionSignals,
                    harmonicFrequencies, harmonicAmplitudes, harmonicPhases
                ).y
                error = if (showErrorGradient) {
                    val dy = approxY - targetY
                    if (dy < 0) -dy else dy
                } else 0f
                path.add(PathPoint(Offset(approxX, approxY), error))
            }
        }

        // Keep trail size limited
        if (path.size > 2000) {
            val toRemove = path.size - 2000
            repeat(toRemove) { path.removeAt(0) }
        }
    }

    fun clearDrawing() {
        clearOverrides()
        drawingPoints.clear()
        repeat(samplesCount) { drawingPoints.add(0f) }
        prefs.drawingPoints = emptyList()
        customCoefficients = emptyList()
        baseCustomCoefficients = emptyList()
        resetSimulation()
    }

    fun clearDrawing2D() {
        clearOverrides()
        drawingPoints2D.clear()
        resampledPoints2D.clear()
        prefs.drawingPoints2D = emptyList()
        customCoefficients2D = emptyList()
        baseCustomCoefficients2D = emptyList()
        resetSimulation()
    }

    fun clearSVG() {
        clearOverrides()
        svgPoints.clear()
        prefs.fourierSvgPoints = emptyList()
        svgCoefficients = emptyList()
        baseSvgCoefficients = emptyList()
        resetSimulation()
    }
}

@Composable
fun rememberFourierState(
    prefs: AppPreferences,
    colors: AppColors,
    scope: CoroutineScope,
    samplesCount: Int = 1000,
    radiusBasePx: Float
): FourierState {
    val defaultColorArgb = colors.accentCyan.toArgb()
    return remember {
        FourierState(prefs, scope, samplesCount, radiusBasePx, defaultColorArgb)
    }
}
