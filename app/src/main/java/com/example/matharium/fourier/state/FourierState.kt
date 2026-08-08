package com.example.matharium.fourier.state

import androidx.compose.runtime.*
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
    var intendedNTerms by mutableIntStateOf(prefs.fourierNTerms)
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
    
    // TRAIL OPTIMIZATION: Fixed-size circular buffer for primitive data
    // This eliminates hundreds of object allocations per second.
    val trailSize = 2000
    var trailPointer = 0
    val pathX = FloatArray(trailSize)
    val pathY = FloatArray(trailSize)
    val pathError = FloatArray(trailSize)
    var pathCount by mutableIntStateOf(0)

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

    val drawingPoints2D = mutableStateListOf<FourierLogic.MathPoint>().apply {
        val saved = prefs.drawingPoints2D
        if (saved.isNotEmpty()) {
            addAll(saved.map { FourierLogic.MathPoint(it.x, it.y) })
            scope.launch { calculateDFT2D() }
        }
    }
    var drawing2DVersion by mutableIntStateOf(0)
    val resampledPoints2D = mutableStateListOf<FourierLogic.MathPoint>()
    var customCoefficients2D by mutableStateOf<List<FourierLogic.ComplexCoeff>>(prefs.customCoefficients2D)
    var baseCustomCoefficients2D by mutableStateOf<List<FourierLogic.ComplexCoeff>>(prefs.customCoefficients2D)

    val svgPoints = mutableStateListOf<FourierLogic.MathPoint>().apply {
        addAll(prefs.fourierSvgPoints.map { FourierLogic.MathPoint(it.x, it.y) })
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

    var isAnalyzing by mutableStateOf(false)
    var isSynthesizing by mutableStateOf(false)
    var cachedHarmonics by mutableStateOf<List<FourierLogic.Harmonic>>(emptyList())
    var idealWavetable by mutableStateOf<Array<FourierLogic.MathPoint>>(emptyArray())

    private var dftJob: Job? = null
    private var spectrumJob: Job? = null
    private var cacheJob: Job? = null

    fun clearOverrides() {
        pausedHarmonics.clear()
        removedHarmonics.clear()
        harmonicFrequencies.clear()
        harmonicAmplitudes.clear()
        harmonicPhases.clear()
        harmonicVersion++
        rebuildCache(fullRebuild = true)
    }

    /**
     * Debounced cache rebuild.
     * Caches the "Mathematics" for standard and analyzed wave types.
     */
    fun rebuildCache(fullRebuild: Boolean = false) {
        if (fullRebuild) isAnalyzing = true 
        
        // Immediate Harmonics update for UI responsiveness
        if (!fullRebuild) {
            cachedHarmonics = prepareHarmonicsList()
        }

        cacheJob?.cancel()
        cacheJob = scope.launch(Dispatchers.Default) {
            isSynthesizing = true
            // Debounce for UI sliders if it's a source change
            if (fullRebuild) delay(300)
            
            val harmonics = prepareHarmonicsList()

            // Generate Wavetable for the ideal signal (error calculation)
            // Rebuilt only if source changes.
            val table = if (fullRebuild || idealWavetable.isEmpty()) {
                FourierLogic.generateWavetable(
                    1000, waveType, radiusBasePx,
                    drawingPoints.toList(), drawingPoints2D.toList(), resampledPoints2D.toList(),
                    svgPoints.toList(), formulaString, customFunctionSignals.toList()
                )
            } else idealWavetable

            withContext(Dispatchers.Main) {
                cachedHarmonics = harmonics
                idealWavetable = table
                isSynthesizing = false
                isAnalyzing = false
                updateSpectrum()
            }
        }
    }

    private fun prepareHarmonicsList(): List<FourierLogic.Harmonic> {
        val maxTerms = 250
        val harmonics = mutableListOf<FourierLogic.Harmonic>()
        when (waveType) {
            WaveType.SINE, WaveType.SQUARE, WaveType.SAWTOOTH, WaveType.TRIANGLE -> {
                harmonics.addAll(FourierLogic.calculateStandardHarmonics(waveType, maxTerms))
            }
            WaveType.MY_SIGNAL -> {
                customCoefficients.forEachIndexed { i, c ->
                    harmonics.add(FourierLogic.Harmonic(i.toFloat(), c.first, -c.second + (kotlin.math.PI.toFloat() / 2f)))
                }
            }
            WaveType.FORMULA -> {
                formulaCoefficients.forEachIndexed { i, c ->
                    harmonics.add(FourierLogic.Harmonic(i.toFloat(), c.first, -c.second + (kotlin.math.PI.toFloat() / 2f)))
                }
            }
            WaveType.MY_SIGNAL_2D -> {
                customCoefficients2D.forEach { c -> harmonics.add(FourierLogic.Harmonic(c.freq.toFloat(), c.amp, c.phase)) }
            }
            WaveType.SVG -> {
                svgCoefficients.forEach { c -> harmonics.add(FourierLogic.Harmonic(c.freq.toFloat(), c.amp, c.phase)) }
            }
            WaveType.PURE_SIGNAL -> {
                customFunctionSignals.forEachIndexed { i, s ->
                    harmonics.add(FourierLogic.Harmonic(
                        harmonicFrequencies[i] ?: (s.freq.toFloatOrNull() ?: 0f),
                        harmonicAmplitudes[i] ?: (s.amp.toFloatOrNull() ?: 0f),
                        harmonicPhases[i] ?: s.cachedPhase,
                        s.colorArgb.toArgb()
                    ))
                }
            }
        }
        return harmonics
    }

    fun calculateDFT() {
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            isAnalyzing = true
            val samples = if (waveType == WaveType.FORMULA) {
                val list = mutableListOf<Float>()
                for (i in 0 until samplesCount) {
                    val x = (i.toDouble() / samplesCount) * 2.0 * kotlin.math.PI - kotlin.math.PI
                    val eval = FourierExpressionEvaluator.evaluate(formulaString, x)
                    list.add(if (eval.isFinite()) eval.toFloat() else 0f)
                }
                list
            } else {
                if (drawingPoints.size < samplesCount) { withContext(Dispatchers.Main) { isAnalyzing = false }; return@launch }
                drawingPoints.map { -it / radiusBasePx }
            }
            val coeffs = try { FourierLogic.performDFT(samples, samplesCount) } catch (e: Exception) { emptyList() }
            val symmetry = FourierLogic.detectSymmetry(samples)
            val idealTable = FourierLogic.generateWavetable(1000, waveType, radiusBasePx, if (waveType == WaveType.FORMULA) emptyList() else drawingPoints.toList(), drawingPoints2D.toList(), resampledPoints2D.toList(), svgPoints.toList(), formulaString, customFunctionSignals.toList())

            withContext(Dispatchers.Main) {
                symmetryResult = symmetry
                if (waveType == WaveType.FORMULA) { formulaCoefficients = coeffs; baseFormulaCoefficients = coeffs }
                else { customCoefficients = coeffs; baseCustomCoefficients = coeffs; prefs.customCoefficients = coeffs }
                idealWavetable = idealTable
                rebuildCache(fullRebuild = true)
            }
        }
    }

    fun calculateDFT2D() {
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            isAnalyzing = true
            if (drawingPoints2D.isEmpty()) { withContext(Dispatchers.Main) { isAnalyzing = false }; return@launch }
            val resampled = FourierLogic.resamplePath(drawingPoints2D.toList(), 1000)
            val normalizedPoints = resampled.map { FourierLogic.MathPoint(it.x / radiusBasePx, -it.y / radiusBasePx) }
            val coeffs = try { FourierLogic.performComplexDFT(normalizedPoints) } catch (e: Exception) { emptyList() }
            val idealTable = FourierLogic.generateWavetable(1000, waveType, radiusBasePx, emptyList(), drawingPoints2D.toList(), resampledPoints2D.toList(), svgPoints.toList(), formulaString, customFunctionSignals.toList())

            withContext(Dispatchers.Main) {
                resampledPoints2D.clear(); resampledPoints2D.addAll(resampled)
                customCoefficients2D = coeffs; baseCustomCoefficients2D = coeffs; prefs.customCoefficients2D = coeffs
                idealWavetable = idealTable
                rebuildCache(fullRebuild = true)
            }
        }
    }

    fun calculateSVGDFT() {
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            isAnalyzing = true
            if (svgPoints.isEmpty()) { withContext(Dispatchers.Main) { isAnalyzing = false }; return@launch }
            val coeffs = try { FourierLogic.performComplexDFT(svgPoints.toList()) } catch (e: Exception) { emptyList() }
            val idealTable = FourierLogic.generateWavetable(1000, waveType, radiusBasePx, emptyList(), drawingPoints2D.toList(), resampledPoints2D.toList(), svgPoints.toList(), formulaString, customFunctionSignals.toList())

            withContext(Dispatchers.Main) {
                svgCoefficients = coeffs; baseSvgCoefficients = coeffs; idealWavetable = idealTable
                rebuildCache(fullRebuild = true)
            }
        }
    }

    fun resetSimulation() {
        running = false; hasStarted = false; time = 0f; clearPath()
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
                        var sumX = 0.0; var sumY = 0.0
                        for (i in 0 until limit) {
                            if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue
                            val hFreq: Float; val hAmp: Float; val hPhase: Float
                            when (waveType) {
                                WaveType.SINE -> { hFreq = 1f; hAmp = 1f; hPhase = 0f }
                                WaveType.SQUARE -> { hFreq = (i * 2 + 1).toFloat(); hAmp = 4f / (hFreq * kotlin.math.PI.toFloat()); hPhase = 0f }
                                WaveType.SAWTOOTH -> { hFreq = (i + 1).toFloat(); val sign = if (hFreq.toInt() % 2 == 0) -1f else 1f; hAmp = (2f / (hFreq * kotlin.math.PI.toFloat())) * sign; hPhase = 0f }
                                WaveType.TRIANGLE -> { hFreq = (i * 2 + 1).toFloat(); val sign = if (((hFreq.toInt() - 1) / 2) % 2 != 0) -1f else 1f; hAmp = (8f / (hFreq * hFreq * kotlin.math.PI.toFloat() * kotlin.math.PI.toFloat())) * sign; hPhase = 0f }
                                WaveType.MY_SIGNAL -> { hFreq = i.toFloat(); if (i < customCoefficients.size) { hAmp = customCoefficients[i].first; hPhase = -customCoefficients[i].second + (kotlin.math.PI.toFloat() / 2f) } else { hAmp = 0f; hPhase = 0f } }
                                WaveType.FORMULA -> { hFreq = i.toFloat(); if (i < formulaCoefficients.size) { hAmp = formulaCoefficients[i].first; hPhase = -formulaCoefficients[i].second + (kotlin.math.PI.toFloat() / 2f) } else { hAmp = 0f; hPhase = 0f } }
                                WaveType.MY_SIGNAL_2D -> { if (i < customCoefficients2D.size) { val c = customCoefficients2D[i]; hAmp = c.amp; hPhase = c.phase; hFreq = c.freq.toFloat() } else { hAmp = 0f; hPhase = 0f; hFreq = 0f } }
                                WaveType.SVG -> { if (i < svgCoefficients.size) { val c = svgCoefficients[i]; hAmp = c.amp; hPhase = c.phase; hFreq = c.freq.toFloat() } else { hAmp = 0f; hPhase = 0f; hFreq = 0f } }
                                WaveType.PURE_SIGNAL -> { if (i < customFunctionSignals.size) { val s = customFunctionSignals[i]; if (s.isPaused) { hAmp = 0f; hPhase = 0f; hFreq = 0f } else { hAmp = s.amp.toFloatOrNull() ?: 0f; hPhase = s.cachedPhase; hFreq = s.freq.toFloatOrNull() ?: 0f } } else { hAmp = 0f; hPhase = 0f; hFreq = 0f } }
                                else -> { hAmp = 0f; hPhase = 0f; hFreq = 0f }
                            }
                            val amp = (harmonicAmplitudes[i] ?: hAmp).toDouble()
                            val phase = (harmonicPhases[i] ?: hPhase).toDouble()
                            val n = (harmonicFrequencies[i] ?: hFreq).toDouble()
                            val angle = 2 * kotlin.math.PI * n * t + phase
                            sumX += amp * kotlin.math.cos(angle); sumY += -amp * kotlin.math.sin(angle)
                        }
                        FourierLogic.Complex(sumX, sumY)
                    }
                }
            }
            if (samples.isEmpty()) return@launch
            val maxFreq = 5.0f; val spectrumPoints = 500
            val result = List(spectrumPoints) { i ->
                val f = (i.toFloat() / spectrumPoints) * maxFreq
                var sumRe = 0.0; var sumIm = 0.0
                val totalSteps = (samples.size * 10.0).toInt()
                for (j in 0 until totalSteps) {
                    val sampleIdx = j % samples.size
                    val normalizedT = j.toDouble() / samples.size
                    val angle = 2 * kotlin.math.PI * f * normalizedT
                    val rotRe = kotlin.math.cos(angle); val rotIm = -kotlin.math.sin(angle)
                    val s = samples[sampleIdx]
                    sumRe += s.re * rotRe - s.im * rotIm; sumIm += s.re * rotIm + s.im * rotRe
                }
                FourierLogic.Complex(sumRe / totalSteps, sumIm / totalSteps)
            }
            withContext(Dispatchers.Main) { spectrumData = result }
        }
    }

    suspend fun runSimulation() {
        if (!running) return
        var lastTime = System.nanoTime()
        while (running) {
            withFrameNanos { frameTime -> updatePhysics(frameTime, lastTime); lastTime = frameTime }
        }
    }

    private fun updatePhysics(frameTime: Long, lastTime: Long) {
        val dt = ((frameTime - lastTime) / 1e9f).coerceAtMost(0.05f) 
        val substeps = 2; val subDt = dt / substeps
        val radiusBase = radiusBasePx; val twoPi = 2.0 * kotlin.math.PI
        val harmonics = cachedHarmonics; val wavetable = idealWavetable
        val termsCount = if (waveType == WaveType.PURE_SIGNAL) harmonics.size else nTerms

        repeat(substeps) {
            time += subDt * speed
            val normalizedTime = ((time % 1f) + 1f) % 1f
            
            // OPTIMIZATION: Summation is cheap (O(K)). 
            // This allows nTerms to update instantly without rebuilding a wavetable.
            var approxX = 0f
            var approxY = 0f
            val angleBase = twoPi * time
            for (i in 0 until termsCount) {
                if (i >= harmonics.size) break
                if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue
                val h = harmonics[i]
                val freq = (harmonicFrequencies[i] ?: h.freq).toDouble()
                val amp = (harmonicAmplitudes[i] ?: h.amp) * radiusBase
                val phase = (harmonicPhases[i] ?: h.phase).toDouble()
                val angle = angleBase * freq + phase
                approxX += (amp * kotlin.math.cos(angle)).toFloat()
                approxY += -(amp * kotlin.math.sin(angle)).toFloat()
            }

            val error: Float
            if (displayMode == FourierDisplayMode.COMPLEX) {
                // Linear Interpolation for Ideal Wavetable (Error Calculation)
                val target = if (wavetable.isNotEmpty()) {
                    val fIdx = normalizedTime * 999f
                    val i1 = fIdx.toInt(); val i2 = (i1 + 1) % 1000; val frac = fIdx - i1
                    val p1 = wavetable[i1]; val p2 = wavetable[i2]
                    FourierLogic.MathPoint(p1.x * (1 - frac) + p2.x * frac, p1.y * (1 - frac) + p2.y * frac)
                } else {
                    val res = FourierLogic.getIdealValue(time, waveType, radiusBase, displayMode, drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints, formulaString, customFunctionSignals, harmonicFrequencies, harmonicAmplitudes, harmonicPhases)
                    FourierLogic.MathPoint(res.x, res.y)
                }
                val dx = approxX - target.x; val dy = approxY - target.y
                error = if (showErrorGradient) kotlin.math.sqrt(dx * dx + dy * dy) else 0f
            } else {
                approxX = time
                val targetY = if (wavetable.isNotEmpty()) {
                    val fIdx = normalizedTime * 999f
                    val i1 = fIdx.toInt(); val i2 = (i1 + 1) % 1000; val frac = fIdx - i1
                    wavetable[i1].y * (1 - frac) + wavetable[i2].y * frac
                } else {
                    FourierLogic.getIdealValue(time, waveType, radiusBase, displayMode, drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints, formulaString, customFunctionSignals, harmonicFrequencies, harmonicAmplitudes, harmonicPhases).y
                }
                val dy = approxY - targetY; error = if (showErrorGradient) (if (dy < 0) -dy else dy) else 0f
            }
            
            // OPTIMIZATION: Circular buffer update (zero heap allocations)
            pathX[trailPointer] = approxX; pathY[trailPointer] = approxY; pathError[trailPointer] = error
            trailPointer = (trailPointer + 1) % trailSize
            if (pathCount < trailSize) pathCount++
        }
    }

    fun clearDrawing() {
        clearOverrides(); drawingPoints.clear(); repeat(samplesCount) { drawingPoints.add(0f) }
        prefs.drawingPoints = emptyList(); customCoefficients = emptyList(); baseCustomCoefficients = emptyList()
        resetSimulation()
    }

    fun clearDrawing2D() {
        clearOverrides(); drawingPoints2D.clear(); resampledPoints2D.clear()
        prefs.drawingPoints2D = emptyList(); customCoefficients2D = emptyList(); baseCustomCoefficients2D = emptyList()
        resetSimulation()
    }

    fun clearSVG() {
        clearOverrides(); svgPoints.clear(); prefs.fourierSvgPoints = emptyList()
        svgCoefficients = emptyList(); baseSvgCoefficients = emptyList(); resetSimulation()
    }

    fun clearPath() { trailPointer = 0; pathCount = 0 }
}

@Composable
fun rememberFourierState(prefs: AppPreferences, colors: AppColors, scope: CoroutineScope, samplesCount: Int = 1000, radiusBasePx: Float): FourierState {
    val defaultColorArgb = colors.accentCyan.toArgb()
    return remember { FourierState(prefs, scope, samplesCount, radiusBasePx, defaultColorArgb) }
}
