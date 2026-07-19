package com.example.matharium.pendulum

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.matharium.app.*
import com.example.matharium.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sqrt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoublePendulum() {
    val colors = LocalAppColors.current
    val prefs = LocalAppPrefs.current

    val pendulums = remember {
        val list = mutableStateListOf<PendulumInstance>()
        list.addAll(prefs.loadPendulums(colors.accentCyan))
        list
    }
    var nextId by remember { mutableIntStateOf(pendulums.maxOfOrNull { it.id }?.plus(1) ?: 1) }
    var running by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }

    var frictionEnabled by remember { mutableStateOf(false) }
    var frictionAmount by remember { mutableFloatStateOf(0.0002f) }
    var gravityAmount by remember { mutableFloatStateOf(9.8f) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(prefs.pendulumScale) }
    var colorByVelocity by remember { mutableStateOf(false) }

    var displayMode by remember { mutableStateOf(DisplayMode.SIMULATION) }

    // Save state when it changes
    LaunchedEffect(scale) { prefs.pendulumScale = scale }
    LaunchedEffect(pendulums.toList()) { prefs.savePendulums(pendulums.toList()) }

    // Main Simulation Loop - Optimized for cross-device consistency
    LaunchedEffect(running, speedMultiplier, gravityAmount, frictionAmount, frictionEnabled) {
        if (!running) return@LaunchedEffect

        var lastTimeNanos = System.nanoTime()
        val fixedDeltaTime = DoublePendulumConstants.PHYSICS_DT // Matches DoublePendulumLogic.DT
        var accumulator = 0.0

        while (running) {
            withFrameNanos { frameTimeNanos ->
                val elapsedSeconds = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = frameTimeNanos

                // Clamp elapsed time to prevent physics explosion after a long pause/lag
                val frameTime = elapsedSeconds.coerceAtMost(DoublePendulumConstants.MAX_ELAPSED_TIME) * speedMultiplier
                accumulator += frameTime
            }

            val framePendulums = pendulums.toList()
            val newPoints = List(framePendulums.size) { mutableListOf<Offset>() }
            val newAnglePoints = List(framePendulums.size) { mutableListOf<Offset>() }

            // Perform physics steps on a background thread to keep UI responsive
            withContext(Dispatchers.Default) {
                var steps = 0
                // Cap physics steps per frame (max 20 steps / 80ms sim time) to prevent UI lockup
                while (accumulator >= fixedDeltaTime && steps < DoublePendulumConstants.MAX_PHYSICS_STEPS) {
                    framePendulums.forEachIndexed { i, p ->
                        p.logic.setGravity(gravityAmount.toDouble())
                        p.logic.setFriction(frictionAmount.toDouble())
                        p.logic.setFrictionEnabled(frictionEnabled)
                        p.logic.update()

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
                // If we lagged too much, just drop the accumulated time
                if (steps >= DoublePendulumConstants.MAX_PHYSICS_STEPS) accumulator = 0.0
            }

            // Sync physics results to UI state (read cached coords, do NOT call update() again)
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                framePendulums.forEachIndexed { i, p ->
                    if (p !in pendulums) return@forEachIndexed

                    val coords = p.logic.currentCoords
                    p.bob1 = Offset(coords.x1.toFloat(), coords.y1.toFloat())
                    p.bob2 = Offset(coords.x2.toFloat(), coords.y2.toFloat())
                    p.kineticEnergy = coords.kineticEnergy

                    // Update angle strings so UI and logic stay in sync
                    p.t1 = String.format(Locale.US, "%.1f", p.logic.thetaOne * 180.0 / PI)
                    p.t2 = String.format(Locale.US, "%.1f", p.logic.thetaTwo * 180.0 / PI)

                    if (colorByVelocity) {
                        val velocity = sqrt(coords.kineticEnergy).toFloat() * DoublePendulumConstants.VELOCITY_COLOR_MULTIPLIER
                        p.currentColor =
                            Color.hsv(
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {

        // --- FIXED TOP SECTION ---
        DoublePendulumVisualizer(
            colors = colors,
            pendulums = pendulums,
            running = running,
            onHasStartedChange = { hasStarted = it },
            displayMode = displayMode,
            onDisplayModeChange = { displayMode = it },
            scale = scale,
            onScaleChange = { scale = it },
            prefs = prefs
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
        ) {
            Button(
                onClick = {
                    if (!hasStarted) {
                        pendulums.forEach { it.initialize(gravityAmount); it.trail.clear(); it.angleTrail.clear() }
                        hasStarted = true
                    }
                    running = !running
                },
                enabled = pendulums.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight),
                shape = RoundedCornerShape(AppDesign.radiusMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (running) colors.accentHell else colors.accentCyan,
                    disabledContainerColor = colors.accentCyan.copy(alpha = 0.3f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    if (running) painterResource(id = R.drawable.pause_outline) else painterResource(
                        id = R.drawable.caret_forward_outline
                    ),
                    null,
                    tint = colors.textOnAccent,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
                Spacer(Modifier.width(AppDesign.spacingSmall))
                @Suppress("SpellCheckingInspection")
                Text(
                    if (running) "Pause" else if (hasStarted) "Resume" else "Simulate",
                    fontWeight = FontWeight.Bold,
                    color = colors.textOnAccent,
                )
            }

            AnimatedVisibility(
                visible = hasStarted,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Button(
                    onClick = {
                        running = false
                        hasStarted = false
                        pendulums.forEach { it.reset() }
                    },
                    modifier = Modifier.height(AppDesign.buttonHeight),
                    shape = RoundedCornerShape(AppDesign.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardSurface),
                    border = BorderStroke(AppDesign.borderStandard, colors.accentCyan)
                ) {
                    @Suppress("SpellCheckingInspection")
                    Text("Reset", color = colors.accentCyan)
                }
            }
        }

        // --- SCROLLABLE BOTTOM SECTION ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
        ) {
            // Physics Environment
            DoublePendulumEnvironment(
                colors = colors,
                speedMultiplier = speedMultiplier,
                onSpeedChange = { speedMultiplier = it },
                gravityAmount = gravityAmount,
                onGravityChange = { gravityAmount = it },
                frictionEnabled = frictionEnabled,
                onFrictionEnabledChange = { frictionEnabled = it },
                frictionAmount = frictionAmount,
                onFrictionAmountChange = { frictionAmount = it },
                colorByVelocity = colorByVelocity,
                onColorByVelocityChange = { colorByVelocity = it }
            )

            // Pendulum Management
            DoublePendulumManager(
                colors = colors,
                pendulums = pendulums,
                running = running,
                onRunningChange = { running = it },
                hasStarted = hasStarted,
                onHasStartedChange = { hasStarted = it },
                nextId = nextId,
                onNextIdChange = { nextId = it },
                prefs = prefs
            )

            // Energy Distribution (Only in Complex Mode)
            AnimatedVisibility(
                visible = displayMode == DisplayMode.COMPLEX,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                EnergyDashboard(
                    modifier = Modifier.fillMaxWidth(),
                    pendulums = pendulums,
                    colors = colors
                )
            }

            // Explainer Card
            GlassCard(colors = colors) {
                Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
                    @Suppress("SpellCheckingInspection")
                    Text(
                        "How it works",
                        fontSize = AppDesign.textHeadline,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(AppDesign.spacingSmall))
                    @Suppress("SpellCheckingInspection")
                    Text(
                        "The Double Pendulum constitutes a compelling physics experiment that demonstrates the elegance of physical principles, illustrating how a system can simultaneously exhibit unpredictability and determinism. By establishing the initial angles (θ1 and θ2) and subsequently releasing the pendulum, one can observe the behavior of the system under controlled conditions.",
                        color = colors.textSecondary,
                        lineHeight = DoublePendulumConstants.LINE_HEIGHT_EXPLAINER
                    )
                }
            }
        }
    }
}
