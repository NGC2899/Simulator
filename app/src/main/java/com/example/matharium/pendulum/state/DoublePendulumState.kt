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

            val framePendulums = pendulums.toList()
            val newPoints = List(framePendulums.size) { mutableListOf<Offset>() }
            val newAnglePoints = List(framePendulums.size) { mutableListOf<Offset>() }

            // Math on Background Thread
            withContext(Dispatchers.Default) {
                var steps = 0
                while (accumulator >= fixedDeltaTime && steps < DoublePendulumConstants.MAX_PHYSICS_STEPS) {
                    framePendulums.forEachIndexed { i, p ->
                        p.logic.setGravity(gravityAmount.toDouble())
                        p.logic.setFriction(frictionAmount.toDouble())
                        p.logic.setFrictionEnabled(frictionEnabled)
                        p.logic.update(fixedDeltaTime)

                        val coords = p.logic.currentCoords
                        newPoints[i].add(Offset(coords.x2.toFloat(), coords.y2.toFloat()))
                        newAnglePoints[i].add(
                            Offset(
                                (p.logic.thetaOne * 180.0 / PI).toFloat(),
                                (p.logic.thetaTwo * 180.0 / PI).toFloat()
                            )
                        )
                    }
                    accumulator -= fixedDeltaTime
                    steps++
                }
                if (steps >= DoublePendulumConstants.MAX_PHYSICS_STEPS) accumulator = 0.0
            }

            // Sync to UI State
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                framePendulums.forEachIndexed { i, p ->
                    if (p !in pendulums) return@forEachIndexed

                    val coords = p.logic.currentCoords
                    p.bob1 = Offset(coords.x1.toFloat(), coords.y1.toFloat())
                    p.bob2 = Offset(coords.x2.toFloat(), coords.y2.toFloat())
                    p.kineticEnergy = coords.kineticEnergy

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

                    p.trail.addAll(newPoints[i])
                    while (p.trail.size > DoublePendulumConstants.TRAIL_MAX_POINTS) p.trail.removeAt(0)

                    p.angleTrail.addAll(newAnglePoints[i])
                    while (p.angleTrail.size > DoublePendulumConstants.ANGLE_TRAIL_MAX_POINTS) p.angleTrail.removeAt(0)
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