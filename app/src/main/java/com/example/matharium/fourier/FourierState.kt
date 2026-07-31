package com.example.matharium.fourier

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.example.matharium.app.*
import kotlinx.coroutines.*

class FourierState(
    val prefs: AppPreferences,
    val colors: AppColors,
    val scope: CoroutineScope,
    val samplesCount: Int,
    val radiusBasePx: Float
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
    val path = mutableStateListOf<PathPoint>()

    val drawingPoints = mutableStateListOf<Float>().apply {
        val saved = prefs.drawingPoints
        if (saved.isNotEmpty()) {
            addAll(saved)
        } else {
            repeat(samplesCount) { add(0f) }
        }
    }
    var customCoefficients by mutableStateOf<List<Pair<Float, Float>>>(prefs.customCoefficients)
    var baseCustomCoefficients by mutableStateOf<List<Pair<Float, Float>>>(prefs.customCoefficients)

    val drawingPoints2D = mutableStateListOf<Offset>().apply {
        val saved = prefs.drawingPoints2D
        if (saved.isNotEmpty()) {
            addAll(saved)
            // Trigger resampling for initial load
            scope.launch { calculateDFT2D() }
        }
    }
    val resampledPoints2D = mutableStateListOf<Offset>()
    var customCoefficients2D by mutableStateOf<List<FourierLogic.ComplexCoeff>>(prefs.customCoefficients2D)
    var baseCustomCoefficients2D by mutableStateOf<List<FourierLogic.ComplexCoeff>>(prefs.customCoefficients2D)

    val svgPoints = mutableStateListOf<Offset>().apply {
        addAll(prefs.fourierSvgPoints)
    }
    var svgCoefficients by mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList())
    var baseSvgCoefficients by mutableStateOf<List<FourierLogic.ComplexCoeff>>(emptyList())

    val customFunctionSignals = mutableStateListOf<SignalInstance>().apply {
        addAll(prefs.loadFourierSignals(colors.accentCyan))
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

    fun calculateDFT() {
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
        scope.launch(Dispatchers.Default) {
            val samples: List<FourierLogic.Complex> = when (waveType) {
                WaveType.MY_SIGNAL -> {
                    val pts = if (drawingPoints.size == samplesCount) drawingPoints.toList()
                    else List(samplesCount) { i -> drawingPoints.getOrElse((i.toFloat() / samplesCount * drawingPoints.size).toInt()) { 0f } }
                    pts.map { FourierLogic.Complex(it.toDouble() / radiusBasePx, 0.0) }
                }
                WaveType.FORMULA -> {
                    List(samplesCount) { i ->
                        val x = (i.toDouble() / samplesCount) * 2.0 * kotlin.math.PI - kotlin.math.PI
                        val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
                        val value = if (eval.isFinite()) eval.toDouble() else 0.0
                        FourierLogic.Complex(value, 0.0)
                    }
                }
                WaveType.PURE_SIGNAL -> {
                    val limit = nTerms.coerceAtMost(customFunctionSignals.size)
                    List(samplesCount) { i ->
                        val t = i.toFloat() / samplesCount
                        var sumX = 0.0
                        var sumY = 0.0
                        for (j in 0 until limit) {
                            if (removedHarmonics[j] == true) continue
                            val sig = customFunctionSignals[j]
                            if (sig.isPaused) continue
                            
                            val freq = (harmonicFrequencies[j] ?: sig.cachedFreq).toDouble()
                            val amp = (harmonicAmplitudes[j] ?: sig.cachedAmp).toDouble()
                            val phase = (harmonicPhases[j] ?: sig.cachedPhase).toDouble()
                            
                            val angle = 2 * kotlin.math.PI * freq * t + phase
                            sumX += amp * kotlin.math.cos(angle)
                            sumY += -amp * kotlin.math.sin(angle)
                        }
                        FourierLogic.Complex(sumX, sumY)
                    }
                }
                WaveType.SINE -> List(samplesCount) { i -> FourierLogic.Complex(-kotlin.math.sin(2 * kotlin.math.PI * (i.toDouble() / samplesCount)), 0.0) }
                WaveType.SQUARE -> List(samplesCount) { i -> FourierLogic.Complex(if ((i.toFloat() / samplesCount) < 0.5f) -1.0 else 1.0, 0.0) }
                WaveType.SAWTOOTH -> List(samplesCount) { i -> FourierLogic.Complex(-(2.0 * ((i.toDouble() / samplesCount + 0.5) % 1.0) - 1.0), 0.0) }
                WaveType.TRIANGLE -> List(samplesCount) { i -> 
                    val f = (i.toDouble() / samplesCount + 0.75) % 1.0
                    val v = if (f < 0.5) (4.0 * f - 1.0) else (3.0 - 4.0 * f)
                    FourierLogic.Complex(v, 0.0)
                }
                WaveType.MY_SIGNAL_2D -> {
                    val raw = if (resampledPoints2D.isNotEmpty()) resampledPoints2D.toList() else drawingPoints2D.toList()
                    if (raw.isEmpty()) emptyList()
                    else {
                        val pts = if (raw.size == samplesCount) raw
                        else List(samplesCount) { i -> raw[(i.toFloat() / samplesCount * raw.size).toInt()] }
                        pts.map { FourierLogic.Complex(it.x.toDouble() / radiusBasePx, -it.y.toDouble() / radiusBasePx) }
                    }
                }
                WaveType.SVG -> {
                    if (svgPoints.isEmpty()) emptyList()
                    else {
                        val pts = if (svgPoints.size == samplesCount) svgPoints.toList()
                        else List(samplesCount) { i -> svgPoints[(i.toFloat() / samplesCount * svgPoints.size).toInt()] }
                        // svgPoints are already normalized/negated to math space
                        pts.map { FourierLogic.Complex(it.x.toDouble(), it.y.toDouble()) }
                    }
                }
            }

            if (samples.isEmpty()) return@launch

            val maxFreq = 5.0f
            val spectrumPoints = 500
            val result = List(spectrumPoints) { i ->
                val f = (i.toFloat() / spectrumPoints) * maxFreq
                var sum = FourierLogic.Complex(0.0, 0.0)
                val analyzeWindow = 10.0
                val totalSteps = (samples.size * analyzeWindow).toInt()
                for (j in 0 until totalSteps) {
                    val sampleIdx = j % samples.size
                    val normalizedT = j.toDouble() / samples.size
                    val angle = 2 * kotlin.math.PI * f * normalizedT
                    // Center of mass formula: integral z(t) * e^(-i * 2pi * f * t)
                    // We calculate sum [ samples[t] * (cos(angle) - i sin(angle)) ]
                    val rot = FourierLogic.Complex(kotlin.math.cos(angle), -kotlin.math.sin(angle))
                    sum += samples[sampleIdx] * rot
                }
                FourierLogic.Complex(sum.re / totalSteps, sum.im / totalSteps)
            }
            withContext(Dispatchers.Main) {
                spectrumData = result
            }
        }
    }

    fun updatePhysics(frameTime: Long, lastTime: Long) {
        val elapsedSeconds = (frameTime - lastTime) / 1e9f
        val substeps = 2
        val subDt = elapsedSeconds / substeps
        val newPoints = mutableListOf<PathPoint>()

        repeat(substeps) {
            time += subDt * speed

            var currentX = 0f
            var currentY = 0f
            val radiusBase = radiusBasePx

            if (waveType == WaveType.PURE_SIGNAL) {
                val limit = nTerms.coerceAtMost(customFunctionSignals.size)
                for (i in 0 until limit) {
                    if (removedHarmonics[i] == true) continue
                    val signal = customFunctionSignals[i]
                    if (signal.isPaused) continue
                    val freq = harmonicFrequencies[i] ?: signal.cachedFreq
                    val ampValue = (harmonicAmplitudes[i] ?: signal.cachedAmp) * radiusBase
                    val phase = harmonicPhases[i] ?: signal.cachedPhase
                    val angle = 2 * kotlin.math.PI.toFloat() * freq * time + phase
                    currentX += ampValue * kotlin.math.cos(angle.toDouble()).toFloat()
                    currentY += -ampValue * kotlin.math.sin(angle.toDouble()).toFloat()
                }
            } else {
                for (i in 0 until nTerms) {
                    if (waveType == WaveType.SINE && i > 0) continue
                    if (removedHarmonics[i] == true) continue
                    if (pausedHarmonics[i] == true) continue

                    if (waveType == WaveType.MY_SIGNAL || waveType == WaveType.FORMULA) {
                        val coeffs = if (waveType == WaveType.FORMULA) formulaCoefficients else customCoefficients
                        if (i < coeffs.size) {
                            val (analyzedAmp, analyzedPhase) = coeffs[i]
                            val amp = harmonicAmplitudes[i] ?: analyzedAmp
                            val phase = harmonicPhases[i] ?: analyzedPhase
                            val n = harmonicFrequencies[i] ?: i.toFloat()
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
                            val phase = harmonicPhases[i] ?: coeff.phase
                            val totalAngle = 2 * kotlin.math.PI.toFloat() * n * time + phase
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
                            val phase = harmonicPhases[i] ?: coeff.phase
                            val totalAngle = 2 * kotlin.math.PI.toFloat() * n * time + phase
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
                    val phase = harmonicPhases[i] ?: 0f
                    val angle = 2 * kotlin.math.PI.toFloat() * n * time + phase
                    currentX += (amp * radiusBase) * kotlin.math.cos(angle.toDouble()).toFloat()
                    currentY += -(amp * radiusBase) * kotlin.math.sin(angle.toDouble()).toFloat()
                }
            }

            if (displayMode == FourierDisplayMode.COMPLEX) {
                val approx = Offset(currentX, currentY)
                val target = FourierLogic.getIdealValue(
                    time, waveType, radiusBase, displayMode,
                    drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                    formulaString, customFunctionSignals,
                    nTerms, removedHarmonics,
                    harmonicFrequencies, harmonicAmplitudes, harmonicPhases
                )
                val error = if (showErrorGradient) {
                    val isExplicitly2D = waveType == WaveType.MY_SIGNAL_2D || waveType == WaveType.SVG || waveType == WaveType.SINE || waveType == WaveType.PURE_SIGNAL
                    if (isExplicitly2D) (approx - target).getDistance() else kotlin.math.abs(approx.y - target.y)
                } else 0f
                newPoints.add(0, PathPoint(approx, error))
            } else {
                val approxY = currentY
                val targetY = FourierLogic.getIdealValue(
                    time, waveType, radiusBase, displayMode,
                    drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                    formulaString, customFunctionSignals,
                    nTerms, removedHarmonics,
                    harmonicFrequencies, harmonicAmplitudes, harmonicPhases
                ).y
                val error = if (showErrorGradient) kotlin.math.abs(approxY - targetY) else 0f
                newPoints.add(0, PathPoint(Offset(time, approxY), error))
            }
        }

        path.addAll(0, newPoints)
        if (path.size > 2000) {
            val toRemove = path.size - 2000
            for (i in 0 until toRemove) { path.removeAt(path.size - 1) }
        }
    }

    fun clearDrawing() {
        drawingPoints.clear()
        repeat(samplesCount) { drawingPoints.add(0f) }
        prefs.drawingPoints = emptyList()
        customCoefficients = emptyList()
        baseCustomCoefficients = emptyList()
        resetSimulation()
    }

    fun clearDrawing2D() {
        drawingPoints2D.clear()
        resampledPoints2D.clear()
        prefs.drawingPoints2D = emptyList()
        customCoefficients2D = emptyList()
        baseCustomCoefficients2D = emptyList()
        resetSimulation()
    }

    fun clearSVG() {
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
    return remember {
        FourierState(prefs, colors, scope, samplesCount, radiusBasePx)
    }
}
