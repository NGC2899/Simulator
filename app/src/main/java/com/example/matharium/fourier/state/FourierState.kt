package com.example.matharium.fourier.state

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.*
import androidx.compose.ui.graphics.toArgb
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

    var isAnalyzing by mutableStateOf(false)
    var isSynthesizing by mutableStateOf(false)
    var cachedHarmonics by mutableStateOf<List<FourierLogic.Harmonic>>(emptyList())
    var idealWavetable by mutableStateOf<Array<Offset>>(emptyArray())
    var reconstructionWavetable by mutableStateOf<Array<Offset>>(emptyArray())

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
     * This calculates the "Mathematics" once so the rendering loop is cheap.
     * [fullRebuild] should be true if the source signal or wave type changed.
     */
    fun rebuildCache(fullRebuild: Boolean = false) {
        // Optimization: Invalidate reconstruction table immediately so physics uses manual fallback
        // during the debounce period. This ensures instant feedback on nTerms change.
        reconstructionWavetable = emptyArray()
        
        // Immediate Harmonics Calculation for non-full rebuilds (e.g. nTerms change)
        // This ensures the manual fallback uses the latest coefficients without delay.
        if (!fullRebuild) {
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
                    customCoefficients2D.forEach { c ->
                        harmonics.add(FourierLogic.Harmonic(c.freq.toFloat(), c.amp, c.phase))
                    }
                }
                WaveType.SVG -> {
                    svgCoefficients.forEach { c ->
                        harmonics.add(FourierLogic.Harmonic(c.freq.toFloat(), c.amp, c.phase))
                    }
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
            cachedHarmonics = harmonics
        }

        cacheJob?.cancel()
        cacheJob = scope.launch(Dispatchers.Default) {
            isSynthesizing = true
            // Debounce for UI sliders
            delay(if (fullRebuild) 300 else 100)
            
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
                    customCoefficients2D.forEach { c ->
                        harmonics.add(FourierLogic.Harmonic(c.freq.toFloat(), c.amp, c.phase))
                    }
                }
                WaveType.SVG -> {
                    svgCoefficients.forEach { c ->
                        harmonics.add(FourierLogic.Harmonic(c.freq.toFloat(), c.amp, c.phase))
                    }
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

            // Generate Wavetable for the ideal signal (error calculation)
            // Only rebuild if the source signal actually changed.
            val table = if (fullRebuild || idealWavetable.isEmpty()) {
                FourierLogic.generateWavetable(
                    1000, waveType, radiusBasePx,
                    drawingPoints.toList(), drawingPoints2D.toList(), resampledPoints2D.toList(),
                    svgPoints.toList(), formulaString, customFunctionSignals.toList()
                )
            } else idealWavetable

            // Generate Wavetable for the reconstructed signal (performance)
            // This is the "Full-Cycle Cache" mentioned in the requirements.
            val currentN = nTerms
            val reconTable = Array(1000) { step ->
                val t = step.toFloat() / 1000
                var sumX = 0f
                var sumY = 0f
                val twoPi = 2.0 * kotlin.math.PI
                for (i in 0 until currentN) {
                    if (i >= harmonics.size) break
                    if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue
                    val h = harmonics[i]
                    val freq = (harmonicFrequencies[i] ?: h.freq).toDouble()
                    val amp = (harmonicAmplitudes[i] ?: h.amp) * radiusBasePx
                    val phase = (harmonicPhases[i] ?: h.phase).toDouble()
                    val angle = twoPi * freq * t + phase
                    sumX += (amp * kotlin.math.cos(angle)).toFloat()
                    sumY += -(amp * kotlin.math.sin(angle)).toFloat()
                }
                Offset(sumX, sumY)
            }

            withContext(Dispatchers.Main) {
                cachedHarmonics = harmonics
                idealWavetable = table
                reconstructionWavetable = reconTable
                isSynthesizing = false
                updateSpectrum()
            }
        }
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
                    val value = if (eval.isFinite()) eval.toFloat() else 0f
                    list.add(value)
                }
                list
            } else {
                if (drawingPoints.size < samplesCount) {
                    withContext(Dispatchers.Main) { isAnalyzing = false }
                    return@launch
                }
                drawingPoints.map { -it / radiusBasePx }
            }

            val coeffs = try {
                FourierLogic.performDFT(samples, samplesCount)
            } catch (e: Exception) {
                emptyList()
            }

            val symmetry = FourierLogic.detectSymmetry(samples)

            // Pre-calculate ideal wavetable for immediate use in error calculation
            val idealTable = FourierLogic.generateWavetable(
                1000, waveType, radiusBasePx,
                if (waveType == WaveType.FORMULA) emptyList() else drawingPoints.toList(),
                drawingPoints2D.toList(), resampledPoints2D.toList(),
                svgPoints.toList(), formulaString, customFunctionSignals.toList()
            )

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
                idealWavetable = idealTable
                isAnalyzing = false
                rebuildCache(fullRebuild = true)
            }
        }
    }

    fun calculateDFT2D() {
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            isAnalyzing = true
            if (drawingPoints2D.isEmpty()) {
                withContext(Dispatchers.Main) { isAnalyzing = false }
                return@launch
            }
            val resampled = FourierLogic.resamplePath(drawingPoints2D.toList(), 1000)
            val normalizedPoints = resampled.map { Offset(it.x / radiusBasePx, -it.y / radiusBasePx) }
            val coeffs = try {
                FourierLogic.performComplexDFT(normalizedPoints)
            } catch (e: Exception) {
                emptyList()
            }

            // Pre-calculate ideal wavetable for immediate use
            val idealTable = FourierLogic.generateWavetable(
                1000, waveType, radiusBasePx,
                emptyList(), drawingPoints2D.toList(), resampledPoints2D.toList(),
                svgPoints.toList(), formulaString, customFunctionSignals.toList()
            )

            withContext(Dispatchers.Main) {
                resampledPoints2D.clear()
                resampledPoints2D.addAll(resampled)
                customCoefficients2D = coeffs
                baseCustomCoefficients2D = coeffs
                prefs.customCoefficients2D = coeffs
                idealWavetable = idealTable
                isAnalyzing = false
                rebuildCache(fullRebuild = true)
            }
        }
    }

    fun calculateSVGDFT() {
        dftJob?.cancel()
        dftJob = scope.launch(Dispatchers.Default) {
            isAnalyzing = true
            if (svgPoints.isEmpty()) {
                withContext(Dispatchers.Main) { isAnalyzing = false }
                return@launch
            }
            val coeffs = try {
                FourierLogic.performComplexDFT(svgPoints.toList())
            } catch (e: Exception) {
                emptyList()
            }

            // Pre-calculate ideal wavetable for immediate use
            val idealTable = FourierLogic.generateWavetable(
                1000, waveType, radiusBasePx,
                emptyList(), drawingPoints2D.toList(), resampledPoints2D.toList(),
                svgPoints.toList(), formulaString, customFunctionSignals.toList()
            )

            withContext(Dispatchers.Main) {
                svgCoefficients = coeffs
                baseSvgCoefficients = coeffs
                idealWavetable = idealTable
                isAnalyzing = false
                rebuildCache(fullRebuild = true)
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
        // Limit DT to avoid jagged trails during lag spikes (e.g. keyboard hiding)
        val dt = elapsedSeconds.coerceAtMost(0.05f) 
        val substeps = 2
        val subDt = dt / substeps

        val radiusBase = radiusBasePx
        val twoPi = 2.0 * kotlin.math.PI
        
        // Use a local copy of cached harmonics and wavetables
        val harmonics = cachedHarmonics
        val wavetable = idealWavetable
        val reconTable = reconstructionWavetable
        val termsCount = if (waveType == WaveType.PURE_SIGNAL) harmonics.size else nTerms

        repeat(substeps) {
            time += subDt * speed
            val normalizedTime = ((time % 1f) + 1f) % 1f
            
            var approxX: Float
            var approxY: Float
            
            // OPTIMIZATION: Linear Interpolation for Wavetable (Full-Cycle Cache)
            if (reconTable.isNotEmpty()) {
                val floatIdx = normalizedTime * 999f
                val i1 = floatIdx.toInt()
                val i2 = (i1 + 1) % 1000
                val frac = floatIdx - i1
                val p1 = reconTable[i1]
                val p2 = reconTable[i2]
                approxX = p1.x * (1 - frac) + p2.x * frac
                approxY = p1.y * (1 - frac) + p2.y * frac
            } else {
                // Fallback to manual summation
                var sumX = 0f
                var sumY = 0f
                val angleFactor = twoPi * time
                for (i in 0 until termsCount) {
                    if (i >= harmonics.size) break
                    if (removedHarmonics[i] == true || pausedHarmonics[i] == true) continue
                    val h = harmonics[i]
                    val freq = (harmonicFrequencies[i] ?: h.freq).toDouble()
                    val amp = (harmonicAmplitudes[i] ?: h.amp) * radiusBase
                    val phase = (harmonicPhases[i] ?: h.phase).toDouble()
                    val angle = angleFactor * freq + phase
                    sumX += (amp * kotlin.math.cos(angle)).toFloat()
                    sumY += -(amp * kotlin.math.sin(angle)).toFloat()
                }
                approxX = sumX
                approxY = sumY
            }

            val error: Float
            if (displayMode == FourierDisplayMode.COMPLEX) {
                // Linear Interpolation for Ideal Wavetable (Error Calculation)
                val target = if (wavetable.isNotEmpty()) {
                    val floatIdx = normalizedTime * 999f
                    val i1 = floatIdx.toInt()
                    val i2 = (i1 + 1) % 1000
                    val frac = floatIdx - i1
                    val p1 = wavetable[i1]
                    val p2 = wavetable[i2]
                    Offset(p1.x * (1 - frac) + p2.x * frac, p1.y * (1 - frac) + p2.y * frac)
                } else {
                    FourierLogic.getIdealValue(
                        time, waveType, radiusBase, displayMode,
                        drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                        formulaString, customFunctionSignals,
                        harmonicFrequencies, harmonicAmplitudes, harmonicPhases
                    )
                }
                
                error = if (showErrorGradient) {
                    val dx = approxX - target.x
                    val dy = approxY - target.y
                    kotlin.math.sqrt(dx * dx + dy * dy)
                } else 0f
                path.add(PathPoint(Offset(approxX, approxY), error))
            } else {
                approxX = time
                
                val targetY = if (wavetable.isNotEmpty()) {
                    val floatIdx = normalizedTime * 999f
                    val i1 = floatIdx.toInt()
                    val i2 = (i1 + 1) % 1000
                    val frac = floatIdx - i1
                    val p1 = wavetable[i1].y
                    val p2 = wavetable[i2].y
                    p1 * (1 - frac) + p2 * frac
                } else {
                    FourierLogic.getIdealValue(
                        time, waveType, radiusBase, displayMode,
                        drawingPoints, drawingPoints2D, resampledPoints2D, svgPoints,
                        formulaString, customFunctionSignals,
                        harmonicFrequencies, harmonicAmplitudes, harmonicPhases
                    ).y
                }
                
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
