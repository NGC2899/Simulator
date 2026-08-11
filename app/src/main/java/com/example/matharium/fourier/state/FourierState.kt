package com.example.matharium.fourier.state

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.*
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

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

    // SIMULATION CACHE: Optimized primitive arrays for the hot loop.
    // Mirrored from the harmonics list and maps to avoid Map lookups every frame.
    private var simFreqs = FloatArray(250)
    private var simAmps = FloatArray(250)
    private var simPhases = FloatArray(250)
    private var simColors = IntArray(250)
    private var simActive = BooleanArray(250)
    private var simTermsCount = 0

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
            if (fullRebuild) delay(300.milliseconds)
            
            val harmonics = prepareHarmonicsList()

            // Generate Wavetable for the ideal signal (error calculation)
            // Rebuilt only if source changes.
            val table = if (fullRebuild || idealWavetable.isEmpty()) {
                FourierLogic.generateWavetable(
                    1024, waveType, radiusBasePx,
                    drawingPoints.toList(), drawingPoints2D.toList(), resampledPoints2D.toList(),
                    svgPoints.toList(), formulaString, customFunctionSignals.toList()
                )
            } else idealWavetable

            withContext(Dispatchers.Main) {
                cachedHarmonics = harmonics
                updateSimulationArrays(harmonics) // Update fast-access arrays
                idealWavetable = table
                isSynthesizing = false
                isAnalyzing = false
                updateSpectrum()
            }
        }
    }

    private fun updateSimulationArrays(harmonics: List<FourierLogic.Harmonic>) {
        simTermsCount = harmonics.size.coerceAtMost(250)
        for (i in 0 until simTermsCount) {
            val h = harmonics[i]
            simFreqs[i] = harmonicFrequencies[i] ?: h.freq
            simAmps[i] = harmonicAmplitudes[i] ?: h.amp
            simPhases[i] = harmonicPhases[i] ?: h.phase
            simColors[i] = h.colorArgb
            val isPaused = if (waveType == WaveType.PURE_SIGNAL && i < customFunctionSignals.size) {
                customFunctionSignals[i].isPaused || pausedHarmonics[i] == true
            } else {
                pausedHarmonics[i] == true
            }
            simActive[i] = removedHarmonics[i] != true && !isPaused
        }
    }

    private fun prepareHarmonicsList(): List<FourierLogic.Harmonic> {
        val maxTerms = if (waveType == WaveType.PURE_SIGNAL) 250 else nTerms
        val harmonics = mutableListOf<FourierLogic.Harmonic>()
        when (waveType) {
            WaveType.SINE, WaveType.SQUARE, WaveType.SAWTOOTH, WaveType.TRIANGLE -> {
                harmonics.addAll(FourierLogic.calculateStandardHarmonics(waveType, maxTerms))
            }
            WaveType.MY_SIGNAL -> {
                customCoefficients.take(maxTerms).forEachIndexed { i, c ->
                    harmonics.add(FourierLogic.Harmonic(i.toFloat(), c.first, c.second))
                }
            }
            WaveType.FORMULA -> {
                formulaCoefficients.take(maxTerms).forEachIndexed { i, c ->
                    harmonics.add(FourierLogic.Harmonic(i.toFloat(), c.first, c.second))
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
                val compiled = FourierExpressionEvaluator.compile(formulaString)
                for (i in 0 until samplesCount) {
                    val x = (i.toDouble() / samplesCount) * 2.0 * kotlin.math.PI - kotlin.math.PI
                    val eval = compiled?.eval(x) ?: Double.NaN
                    list.add(if (eval.isFinite()) eval.toFloat() else 0f)
                }
                list
            } else {
                if (drawingPoints.size < samplesCount) { withContext(Dispatchers.Main) { isAnalyzing = false }; return@launch }
                drawingPoints.map { -it / radiusBasePx }
            }
            val coeffs = try { FourierLogic.performDFT(samples, samplesCount) } catch (e: Exception) { emptyList() }
            val symmetry = FourierLogic.detectSymmetry(samples)
            val idealTable = FourierLogic.generateWavetable(1024, waveType, radiusBasePx, if (waveType == WaveType.FORMULA) emptyList() else drawingPoints.toList(), drawingPoints2D.toList(), resampledPoints2D.toList(), svgPoints.toList(), formulaString, customFunctionSignals.toList())

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
            val resampled = FourierLogic.resamplePath(drawingPoints2D.toList(), 1024)
            val normalizedPoints = resampled.map { FourierLogic.MathPoint(it.x / radiusBasePx, -it.y / radiusBasePx) }
            val coeffs = try { FourierLogic.performComplexDFT(normalizedPoints) } catch (e: Exception) { emptyList() }
            val idealTable = FourierLogic.generateWavetable(1024, waveType, radiusBasePx, emptyList(), drawingPoints2D.toList(), resampledPoints2D.toList(), svgPoints.toList(), formulaString, customFunctionSignals.toList())

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
            val idealTable = FourierLogic.generateWavetable(1024, waveType, radiusBasePx, emptyList(), drawingPoints2D.toList(), resampledPoints2D.toList(), svgPoints.toList(), formulaString, customFunctionSignals.toList())

            withContext(Dispatchers.Main) {
                svgCoefficients = coeffs; baseSvgCoefficients = coeffs; idealWavetable = idealTable
                rebuildCache(fullRebuild = true)
            }
        }
    }

    fun resetSimulation() {
        running = false; hasStarted = false; time = 0f; clearPath()
    }

    /**
     * OPTIMIZED Spectrum Update.
     * Directly maps Fourier coefficients to the spectrum graph.
     * Eliminates millions of trigonometric and reconstruction operations.
     */
    fun updateSpectrum() {
        spectrumJob?.cancel()
        spectrumJob = scope.launch(Dispatchers.Default) {
            val harmonics = prepareHarmonicsList()
            val maxFreq = 5.0f
            val spectrumPoints = 500
            
            // MATHARIUM CANONICAL SPECTRUM
            // This represents the Amplitude Spectrum (A_n) of the Fourier Series.
            // We use a Gaussian kernel for smooth visualization without sinc leakage artifacts.
            val sigma = 0.04f
            val twoSigmaSq = 2f * sigma * sigma

            val result = List(spectrumPoints) { i ->
                val f = (i.toFloat() / spectrumPoints) * maxFreq
                var magnitudeSum = 0.0
                
                for (hIdx in harmonics.indices) {
                    if (removedHarmonics[hIdx] == true || pausedHarmonics[hIdx] == true) continue
                    val h = harmonics[hIdx]
                    val freq = harmonicFrequencies[hIdx] ?: h.freq
                    val amp = harmonicAmplitudes[hIdx] ?: h.amp
                    
                    // Gaussian Kernel: K(f) = A * exp(-(f-f0)^2 / (2*sigma^2))
                    val df = f - freq
                    val weight = kotlin.math.exp(-(df * df) / twoSigmaSq)
                    magnitudeSum += (kotlin.math.abs(amp) * weight).toDouble()
                }
                // Store magnitude in Re, 0 in Im for stable visualization
                FourierLogic.Complex(magnitudeSum, 0.0)
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
        val wavetable = idealWavetable
        val termsToProcess = if (waveType == WaveType.PURE_SIGNAL) simTermsCount else nTerms

        repeat(substeps) {
            time += subDt * speed
            val normalizedTime = ((time % 1f) + 1f) % 1f
            
            var approxX = 0f
            var approxY = 0f
            val angleBase = -twoPi * time
            for (i in 0 until termsToProcess) {
                if (i >= simTermsCount) break
                if (!simActive[i]) continue
                
                val freq = simFreqs[i].toDouble()
                val amp = simAmps[i] * radiusBase
                val phase = simPhases[i].toDouble()
                val angle = angleBase * freq + phase
                approxX += (amp * kotlin.math.cos(angle)).toFloat()
                approxY += -(amp * kotlin.math.sin(angle)).toFloat()
            }

            val error: Float
            if (displayMode == FourierDisplayMode.COMPLEX) {
                // Linear Interpolation for Ideal Wavetable (Error Calculation)
                val target = if (wavetable.isNotEmpty()) {
                    val fIdx = normalizedTime * 1023f
                    val i1 = fIdx.toInt(); val i2 = (i1 + 1) % 1024; val frac = fIdx - i1
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
                    val fIdx = normalizedTime * 1023f
                    val i1 = fIdx.toInt(); val i2 = (i1 + 1) % 1024; val frac = fIdx - i1
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
fun rememberFourierState(prefs: AppPreferences, colors: AppColors, scope: CoroutineScope, samplesCount: Int = 1024, radiusBasePx: Float): FourierState {
    val defaultColorArgb = colors.accentCyan.toArgb()
    return remember { FourierState(prefs, scope, samplesCount, radiusBasePx, defaultColorArgb) }
}
