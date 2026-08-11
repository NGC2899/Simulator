package com.example.matharium.fourier.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.*
import com.example.matharium.fourier.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourierSeries() {
    val colors = LocalAppColors.current
    val prefs = LocalAppPrefs.current
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radiusBasePx = with(density) { AppDesign.unitCircleRadius.toPx() }
    
    val state = rememberFourierState(prefs, colors, scope, radiusBasePx = radiusBasePx)

    // CONSOLIDATED Persistence & Simple Settings
    LaunchedEffect(state.nTerms) {
        state.prefs.fourierNTerms = state.nTerms
        state.rebuildCache(fullRebuild = false)
    }
    LaunchedEffect(state.speed) { state.prefs.fourierSpeed = state.speed }
    LaunchedEffect(state.windingFrequency) { state.prefs.fourierWindingFrequency = state.windingFrequency }
    LaunchedEffect(state.waveStretch) { state.prefs.fourierWaveStretch = state.waveStretch }
    LaunchedEffect(state.showErrorGradient) { state.prefs.fourierShowErrorGradient = state.showErrorGradient }
    LaunchedEffect(state.errorSensitivity) { state.prefs.fourierErrorSensitivity = state.errorSensitivity }
    LaunchedEffect(state.displayMode) { state.prefs.fourierDisplayMode = state.displayMode.name }

    // CONSOLIDATED Source Analysis Logic
    // This flow ensures that analysis happens in the correct order and results 
    // are cached only once per source change.
    LaunchedEffect(state.waveType, state.formulaString, state.drawingVersion, state.drawing2DVersion) {
        state.running = false
        state.hasStarted = false
        
        when (state.waveType) {
            WaveType.MY_SIGNAL -> {
                kotlinx.coroutines.delay(150) // Debounce for drawing
                state.calculateDFT()
            }
            WaveType.MY_SIGNAL_2D -> {
                kotlinx.coroutines.delay(150)
                state.calculateDFT2D()
            }
            WaveType.FORMULA -> {
                state.calculateDFT()
            }
            WaveType.SVG -> {
                if (state.svgCoefficients.isEmpty()) state.calculateSVGDFT()
            }
            else -> {
                state.rebuildCache(fullRebuild = true)
            }
        }
        
        // Update persistent points
        state.prefs.drawingPoints = state.drawingPoints.toList()
        state.prefs.drawingPoints2D = state.drawingPoints2D.map { Offset(it.x, it.y) }
        state.prefs.fourierWaveType = state.waveType.name
    }

    // Handle internal tweaks (harmonic overrides)
    LaunchedEffect(state.harmonicVersion) {
        val isFull = state.waveType == WaveType.PURE_SIGNAL
        state.rebuildCache(fullRebuild = isFull)
        state.prefs.saveFourierSignals(state.customFunctionSignals.toList()) 
    }

    LaunchedEffect(state.running) {
        state.runSimulation()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val svgPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                    if (content != null) {
                        val points = FourierLogic.extractPointsFromSVG(content)
                        withContext(Dispatchers.Main) {
                            state.svgPoints.clear()
                            state.svgPoints.addAll(points)
                            state.prefs.fourierSvgPoints = points.map { Offset(it.x, it.y) }
                            state.calculateSVGDFT()
                            state.resetSimulation()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        state.waveType = WaveType.SQUARE
                        android.widget.Toast.makeText(context, "Invalid SVG: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val isSimulationEnabled by remember {
        derivedStateOf {
            if (state.isAnalyzing) return@derivedStateOf false
            when (state.waveType) {
                WaveType.MY_SIGNAL -> state.drawingPoints.any { it != 0f }
                WaveType.MY_SIGNAL_2D -> state.drawingPoints2D.isNotEmpty()
                WaveType.PURE_SIGNAL -> state.customFunctionSignals.isNotEmpty()
                WaveType.FORMULA -> state.formulaCoefficients.isNotEmpty()
                WaveType.SVG -> state.svgCoefficients.isNotEmpty()
                else -> true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)) {
        FourierVisualizerBox(
            displayMode = state.displayMode, onDisplayModeChange = { state.displayMode = it },
            waveType = state.waveType, 
            nTermsProvider = { state.nTerms }, 
            intendedNTermsProvider = { state.intendedNTerms },
            onIntendedNTermsChange = { state.intendedNTerms = it },
            onActiveNTermsChange = { state.nTerms = it },
            timeProvider = { state.time }, 
            pathX = state.pathX, pathY = state.pathY, pathError = state.pathError,
            pathCountProvider = { state.pathCount },
            trailPointerProvider = { state.trailPointer },
            showErrorGradient = state.showErrorGradient,
            errorSensitivity = state.errorSensitivity, waveStretch = state.waveStretch,
            onClearPath = { state.clearPath() }, windingFrequency = state.windingFrequency,
            customCoefficients = state.customCoefficients, customCoefficients2D = state.customCoefficients2D,
            formulaCoefficients = state.formulaCoefficients, svgCoefficients = state.svgCoefficients,
            customFunctionSignals = state.customFunctionSignals, colors = colors,
            pausedHarmonics = state.pausedHarmonics, removedHarmonics = state.removedHarmonics,
            harmonicFrequencies = state.harmonicFrequencies, harmonicAmplitudes = state.harmonicAmplitudes,
            harmonicPhases = state.harmonicPhases,
            isAnalyzing = state.isAnalyzing, isSynthesizing = state.isSynthesizing,
            cachedHarmonics = state.cachedHarmonics
        )

        FourierActionControls(
            running = state.running, onRunningChange = { state.running = it },
            hasStarted = state.hasStarted, onHasStartedChange = { state.hasStarted = it },
            onReset = { state.resetSimulation() },
            colors = colors, enabled = isSimulationEnabled
        )

        Column(modifier = Modifier.clip(RoundedCornerShape(AppDesign.radiusCard)).weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)) {
            FourierSettingsCard(state, svgPickerLauncher)
            SimulatorEnvironmentSettings(state)

            if (state.displayMode == FourierDisplayMode.WRAPPING) {
                FrequencyDomainGraph(spectrumData = state.spectrumData, colors = colors, currentWindingFreq = state.windingFrequency, timeProvider = { state.time })
            } else if (state.displayMode == FourierDisplayMode.COMPLEX) {
                ComplexHarmonicComponents(
                    nTermsProvider = { state.nTerms }, waveType = state.waveType, timeProvider = { state.time }, colors = colors,
                    customCoefficients = state.customCoefficients, customCoefficients2D = state.customCoefficients2D,
                    formulaCoefficients = state.formulaCoefficients, svgCoefficients = state.svgCoefficients,
                    customFunctionSignals = state.customFunctionSignals,
                    onRemoveHarmonic = { i -> state.removedHarmonics[i] = true; state.harmonicVersion++; state.clearPath(); state.time = 0f },
                    onTogglePause = { i -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].isPaused = !state.customFunctionSignals[i].isPaused else state.pausedHarmonics[i] = !(state.pausedHarmonics[i] ?: false); state.harmonicVersion++; state.clearPath() },
                    isHarmonicPaused = { i -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].isPaused else state.pausedHarmonics[i] ?: false },
                    onFrequencyChange = { i, f -> if (state.waveType == WaveType.PURE_SIGNAL) { state.customFunctionSignals[i].freq = String.format(java.util.Locale.US, "%.2f", f); state.customFunctionSignals[i].updateCache() } else state.harmonicFrequencies[i] = f; state.harmonicVersion++; state.clearPath() },
                    getHarmonicFrequency = { i, d -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].freq.toFloatOrNull() ?: d else state.harmonicFrequencies[i] ?: d },
                    onAmplitudeChange = { i, a -> if (state.waveType == WaveType.PURE_SIGNAL) { state.customFunctionSignals[i].amp = String.format(java.util.Locale.US, "%.2f", a); state.customFunctionSignals[i].updateCache() } else state.harmonicAmplitudes[i] = a; state.harmonicVersion++; state.clearPath() },
                    getHarmonicAmplitude = { i, d -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].amp.toFloatOrNull() ?: d else state.harmonicAmplitudes[i] ?: d },
                    onPhaseChange = { i, p -> if (state.waveType == WaveType.PURE_SIGNAL) { state.customFunctionSignals[i].phase = String.format(java.util.Locale.US, "%.2f", p * 180f / Math.PI.toFloat()); state.customFunctionSignals[i].updateCache() } else state.harmonicPhases[i] = p; state.harmonicVersion++; state.clearPath() },
                    getHarmonicPhase = { i, d -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].cachedPhase else state.harmonicPhases[i] ?: d },
                    removedHarmonics = state.removedHarmonics,
                    onResetHarmonic = { i -> state.pausedHarmonics.remove(i); state.removedHarmonics.remove(i); state.harmonicFrequencies.remove(i); state.harmonicAmplitudes.remove(i); state.harmonicPhases.remove(i); if (state.waveType == WaveType.PURE_SIGNAL) { val s = state.customFunctionSignals[i]; s.freq = s.initialFreq; s.amp = s.initialAmp; s.phase = s.initialPhase; s.isPaused = false; s.updateCache() }; state.harmonicVersion++; state.clearPath(); state.time = 0f },
                    onResetHarmonics = { state.pausedHarmonics.clear(); state.removedHarmonics.clear(); state.harmonicFrequencies.clear(); state.harmonicAmplitudes.clear(); state.harmonicPhases.clear(); when (state.waveType) { WaveType.MY_SIGNAL -> state.customCoefficients = state.baseCustomCoefficients; WaveType.MY_SIGNAL_2D -> state.customCoefficients2D = state.baseCustomCoefficients2D; WaveType.SVG -> state.svgCoefficients = state.baseSvgCoefficients; WaveType.FORMULA -> state.formulaCoefficients = state.baseFormulaCoefficients; WaveType.PURE_SIGNAL -> { state.customFunctionSignals.forEach { s -> s.freq = s.initialFreq; s.amp = s.initialAmp; s.phase = s.initialPhase; s.isPaused = false; s.updateCache() } } else -> {} }; state.harmonicVersion++; state.clearPath(); state.time = 0f }
                )
            } else {
                HarmonicComponents(
                    nTermsProvider = { state.nTerms }, waveType = state.waveType, timeProvider = { state.time }, colors = colors,
                    customCoefficients = state.customCoefficients, customCoefficients2D = state.customCoefficients2D,
                    formulaCoefficients = state.formulaCoefficients, svgCoefficients = state.svgCoefficients,
                    customFunctionSignals = state.customFunctionSignals,
                    onRemoveHarmonic = { i -> state.removedHarmonics[i] = true; state.harmonicVersion++; state.clearPath(); state.time = 0f },
                    onTogglePause = { i -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].isPaused = !state.customFunctionSignals[i].isPaused else state.pausedHarmonics[i] = !(state.pausedHarmonics[i] ?: false); state.harmonicVersion++; state.clearPath() },
                    isHarmonicPaused = { i -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].isPaused else state.pausedHarmonics[i] ?: false },
                    onFrequencyChange = { i, f -> if (state.waveType == WaveType.PURE_SIGNAL) { state.customFunctionSignals[i].freq = String.format(java.util.Locale.US, "%.2f", f); state.customFunctionSignals[i].updateCache() } else state.harmonicFrequencies[i] = f; state.harmonicVersion++; state.clearPath() },
                    getHarmonicFrequency = { i, d -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].freq.toFloatOrNull() ?: d else state.harmonicFrequencies[i] ?: d },
                    onAmplitudeChange = { i, a -> if (state.waveType == WaveType.PURE_SIGNAL) { state.customFunctionSignals[i].amp = String.format(java.util.Locale.US, "%.2f", a); state.customFunctionSignals[i].updateCache() } else state.harmonicAmplitudes[i] = a; state.harmonicVersion++; state.clearPath() },
                    getHarmonicAmplitude = { i, d -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].amp.toFloatOrNull() ?: d else state.harmonicAmplitudes[i] ?: d },
                    onPhaseChange = { i, p -> if (state.waveType == WaveType.PURE_SIGNAL) { state.customFunctionSignals[i].phase = String.format(java.util.Locale.US, "%.2f", p * 180f / Math.PI.toFloat()); state.customFunctionSignals[i].updateCache() } else state.harmonicPhases[i] = p; state.harmonicVersion++; state.clearPath() },
                    getHarmonicPhase = { i, d -> if (state.waveType == WaveType.PURE_SIGNAL) state.customFunctionSignals[i].cachedPhase else state.harmonicPhases[i] ?: d },
                    removedHarmonics = state.removedHarmonics,
                    onResetHarmonic = { i -> state.pausedHarmonics.remove(i); state.removedHarmonics.remove(i); state.harmonicFrequencies.remove(i); state.harmonicAmplitudes.remove(i); state.harmonicPhases.remove(i); if (state.waveType == WaveType.PURE_SIGNAL) { val s = state.customFunctionSignals[i]; s.freq = s.initialFreq; s.amp = s.initialAmp; s.phase = s.initialPhase; s.isPaused = false; s.updateCache() }; state.harmonicVersion++; state.clearPath(); state.time = 0f },
                    onResetHarmonics = { state.pausedHarmonics.clear(); state.removedHarmonics.clear(); state.harmonicFrequencies.clear(); state.harmonicAmplitudes.clear(); state.harmonicPhases.clear(); when (state.waveType) { WaveType.MY_SIGNAL -> state.customCoefficients = state.baseCustomCoefficients; WaveType.MY_SIGNAL_2D -> state.customCoefficients2D = state.baseCustomCoefficients2D; WaveType.SVG -> state.svgCoefficients = state.baseSvgCoefficients; WaveType.FORMULA -> state.formulaCoefficients = state.baseFormulaCoefficients; WaveType.PURE_SIGNAL -> { state.customFunctionSignals.forEach { s -> s.freq = s.initialFreq; s.amp = s.initialAmp; s.phase = s.initialPhase; s.isPaused = false; s.updateCache() } } else -> {} }; state.harmonicVersion++; state.clearPath(); state.time = 0f }
                )
            }

            GlassCard(colors = colors) {
                Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
                    Text("How it works", fontSize = AppDesign.textHeadline, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(AppDesign.radiusSmall))
                    Text("Fourier series allows us to represent any periodic signal as a sum of simple sine and cosine waves. By adding more terms (circles), we can approximate complex shapes like square or sawtooth waves more accurately.", color = colors.textSecondary, fontSize = AppDesign.textBodyLarge, lineHeight = 20.sp)
                }
            }
        }
    }
}
