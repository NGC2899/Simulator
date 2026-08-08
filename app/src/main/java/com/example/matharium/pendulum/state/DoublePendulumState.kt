package com.example.matharium.pendulum.state

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.matharium.app.*
import com.example.matharium.pendulum.engine.*
import kotlinx.coroutines.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Manages the state and physical simulation loop for Double Pendulums.
 */
class DoublePendulumState(
    val prefs: AppPreferences,
    val initialPendulums: List<PendulumInstance>,
    val scope: CoroutineScope
) {
    val pendulums = mutableStateListOf<PendulumInstance>().apply {
        addAll(initialPendulums)
    }

    var nextId by mutableIntStateOf(pendulums.maxOfOrNull { it.id }?.plus(1) ?: 1)
    var running by mutableStateOf(false)
    var hasStarted by mutableStateOf(false)

    var frictionEnabled by mutableStateOf(prefs.pendulumFrictionEnabled)
    var frictionAmount by mutableFloatStateOf(prefs.pendulumFrictionAmount)
    var gravityAmount by mutableFloatStateOf(prefs.pendulumGravityAmount)
    var speedMultiplier by mutableFloatStateOf(prefs.pendulumSpeedMultiplier)
    var scale by mutableFloatStateOf(prefs.pendulumScale)
    var colorByVelocity by mutableStateOf(prefs.pendulumColorByVelocity)

    var displayMode by mutableStateOf(PendulumDisplayMode.SIMULATION)

    /**
     * Main Simulation Loop.
     * Runs on the provided scope using the frame clock.
     */
    suspend fun runSimulation() {
        if (!running) return

        var lastTimeNanos = System.nanoTime()
        val fixedDeltaTime = DoublePendulumConstants.PHYSICS_DT
        var accumulator = 0.0

        while (running) {
            withFrameNanos { frameTimeNanos ->
                val elapsedSeconds = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = frameTimeNanos
                val frameTime = elapsedSeconds.coerceAtMost(DoublePendulumConstants.MAX_ELAPSED_TIME) * speedMultiplier
                accumulator += frameTime
            }

            // OPTIMIZATION: Reuse primitive buffers to avoid per-frame List/Offset allocations
            val framePendulums = pendulums.toList()
            val maxSteps = DoublePendulumConstants.MAX_PHYSICS_STEPS
            val numPendulums = framePendulums.size
            
            // Use local temporary primitive arrays for calculations
            val x2Buffer = FloatArray(numPendulums * maxSteps)
            val y2Buffer = FloatArray(numPendulums * maxSteps)
            val angleXBuffer = FloatArray(numPendulums * maxSteps)
            val angleYBuffer = FloatArray(numPendulums * maxSteps)
            val stepCounts = IntArray(numPendulums)

            // Math on Background Thread
            withContext(Dispatchers.Default) {
                var steps = 0
                while (accumulator >= fixedDeltaTime && steps < maxSteps) {
                    framePendulums.forEachIndexed { i, p ->
                        p.logic.setGravity(gravityAmount.toDouble())
                        p.logic.setFriction(frictionAmount.toDouble())
                        p.logic.setFrictionEnabled(frictionEnabled)
                        p.logic.update(fixedDeltaTime)

                        val coords = p.logic.currentCoords
                        val offset = i * maxSteps + steps
                        x2Buffer[offset] = coords.x2.toFloat()
                        y2Buffer[offset] = coords.y2.toFloat()
                        angleXBuffer[offset] = (p.logic.thetaOne * 180.0 / PI).toFloat()
                        angleYBuffer[offset] = (p.logic.thetaTwo * 180.0 / PI).toFloat()
                        stepCounts[i]++
                    }
                    accumulator -= fixedDeltaTime
                    steps++
                }
                if (steps >= maxSteps) accumulator = 0.0
            }

            // Sync to UI State
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                framePendulums.forEachIndexed { i, p ->
                    if (p !in pendulums) return@forEachIndexed

                    val coords = p.logic.currentCoords
                    p.bob1 = Offset(coords.x1.toFloat(), coords.y1.toFloat())
                    p.bob2 = Offset(coords.x2.toFloat(), coords.y2.toFloat())
                    p.kineticEnergy = coords.kineticEnergy

                    // OPTIMIZATION: Decouple string formatting (runs on UI demand or throttled)
                    // Currently keeping it for simplicity but using cached logic values.
                    p.t1 = String.format(Locale.US, "%.1f", p.logic.thetaOne * 180.0 / PI)
                    p.t2 = String.format(Locale.US, "%.1f", p.logic.thetaTwo * 180.0 / PI)

                    if (colorByVelocity) {
                        val velocity = sqrt(coords.kineticEnergy).toFloat() * DoublePendulumConstants.VELOCITY_COLOR_MULTIPLIER
                        p.currentColor = Color.hsv(
                            (DoublePendulumConstants.VELOCITY_HUE_OFFSET + velocity * DoublePendulumConstants.VELOCITY_HUE_SCALE).coerceIn(0f, 360f),
                            DoublePendulumConstants.COLOR_SATURATION_ALT,
                            DoublePendulumConstants.COLOR_VALUE
                        )
                    } else {
                        p.currentColor = p.baseColor
                    }

                    // Batch update trails using primitive arrays (Zero Allocation)
                    for (s in 0 until stepCounts[i]) {
                        val offset = i * maxSteps + s
                        
                        p.trailX[p.trailPointer] = x2Buffer[offset]
                        p.trailY[p.trailPointer] = y2Buffer[offset]
                        p.trailPointer = (p.trailPointer + 1) % p.trailSize
                        if (p.trailCount < p.trailSize) p.trailCount++
                        
                        p.trailAngleX[p.angleTrailPointer] = angleXBuffer[offset]
                        p.trailAngleY[p.angleTrailPointer] = angleYBuffer[offset]
                        p.angleTrailPointer = (p.angleTrailPointer + 1) % p.angleTrailSize
                        if (p.angleTrailCount < p.angleTrailSize) p.angleTrailCount++
                    }
                }
            }
        }
    }

    fun saveToPrefs() {
        prefs.pendulumScale = scale
        prefs.pendulumFrictionEnabled = frictionEnabled
        prefs.pendulumFrictionAmount = frictionAmount
        prefs.pendulumGravityAmount = gravityAmount
        prefs.pendulumSpeedMultiplier = speedMultiplier
        prefs.pendulumColorByVelocity = colorByVelocity
        prefs.savePendulums(pendulums.toList())
    }

    fun resetAll() {
        running = false
        hasStarted = false
        pendulums.forEach { it.reset() }
    }
}

@Composable
fun rememberDoublePendulumState(
    prefs: AppPreferences,
    colors: AppColors,
    scope: CoroutineScope = rememberCoroutineScope()
): DoublePendulumState {
    val initial = remember { prefs.loadPendulums(colors.accentCyan) }
    return remember { DoublePendulumState(prefs, initial, scope) }
}