package com.example.myapplication

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoublePendulum() {
    val colors = LocalAppColors.current

    val pendulums = remember { mutableStateListOf(PendulumInstance(0, colors.accentCyan)) }
    var nextId by remember { mutableIntStateOf(1) }
    var running by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }

    var frictionEnabled by remember { mutableStateOf(false) }
    var frictionAmount by remember { mutableFloatStateOf(0.0002f) }
    var gravityAmount by remember { mutableFloatStateOf(9.8f) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(100f) }
    var colorByVelocity by remember { mutableStateOf(false) }
    var isEnvExpanded by remember { mutableStateOf(false) }
    var isPendulumManagerExpanded by remember { mutableStateOf(false) }

    var draggingPendulumId by remember { mutableStateOf<Int?>(null) }
    var draggingBobType by remember { mutableStateOf(DragTarget.NONE) }
    var displayMode by remember { mutableStateOf(DisplayMode.SIMULATION) }

    // Persistent "template" angles to prevent resetting to 0/90 when list is cleared
    var templateT1 by remember { mutableStateOf("90.0") }
    var templateT2 by remember { mutableStateOf("90.0") }

    LaunchedEffect(running, hasStarted, pendulums.size) {
        if (!running && (!hasStarted || draggingPendulumId != null)) {
            pendulums.forEach { it.updatePositions() }
        }
    }

    pendulums.forEach { p ->
        LaunchedEffect(p.t1, p.t2, p.l1, p.l2, running, hasStarted, draggingPendulumId) {
            if (!running && (!hasStarted || draggingPendulumId == p.id)) p.updatePositions()
        }
    }

    // Main Simulation Loop - Optimized for cross-device consistency
    LaunchedEffect(running, speedMultiplier, gravityAmount, frictionAmount, frictionEnabled) {
        if (!running) return@LaunchedEffect

        var lastTimeNanos = System.nanoTime()
        val fixedDeltaTime = 0.004 // Matches DoublePendulumLogic.DT
        var accumulator = 0.0

        while (running) {
            withFrameNanos { frameTimeNanos ->
                val elapsedSeconds = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = frameTimeNanos

                // Clamp elapsed time to prevent physics explosion after a long pause/lag
                val frameTime = elapsedSeconds.coerceAtMost(0.1) * speedMultiplier
                accumulator += frameTime
            }

            val newPoints = List(pendulums.size) { mutableListOf<Offset>() }
            val newAnglePoints = List(pendulums.size) { mutableListOf<Offset>() }

            // Perform physics steps on a background thread to keep UI responsive
            withContext(Dispatchers.Default) {
                while (accumulator >= fixedDeltaTime) {
                    pendulums.forEachIndexed { i, p ->
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
                }
            }

            // Sync physics results to UI state (read cached coords, do NOT call update() again)
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                pendulums.forEachIndexed { i, p ->
                    val coords = p.logic.currentCoords
                    p.bob1 = Offset(coords.x1.toFloat(), coords.y1.toFloat())
                    p.bob2 = Offset(coords.x2.toFloat(), coords.y2.toFloat())
                    p.kineticEnergy = coords.kineticEnergy

                    if (colorByVelocity) {
                        val velocity = sqrt(coords.kineticEnergy).toFloat() * 0.2f
                        p.currentColor =
                            Color.hsv((200f + velocity * 100f).coerceIn(0f, 360f), 0.8f, 0.9f)
                    } else {
                        p.currentColor = p.baseColor
                    }

                    p.trail.addAll(newPoints[i])
                    while (p.trail.size > 120) p.trail.removeAt(0)

                    p.angleTrail.addAll(newAnglePoints[i])
                    while (p.angleTrail.size > 1000) p.angleTrail.removeAt(0)
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
        // ── Physics Environment ──────────────────────────────────
        GlassCard(colors = colors) {
            Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEnvExpanded = !isEnvExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Environment Settings",
                        color = colors.textPrimary,
                        fontSize = AppDesign.textBodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        if (isEnvExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = colors.textSecondary
                    )
                }

                AnimatedVisibility(visible = isEnvExpanded) {
                    Column(modifier = Modifier.padding(top = AppDesign.spacingMedium)) {
                        // Speed Slider
                        LabeledSlider(
                            label = "Time Scale",
                            valueDisplay = String.format(Locale.US, "%.1fx", speedMultiplier),
                            value = speedMultiplier,
                            range = 0.1f..15f,
                            colors = colors
                        ) { speedMultiplier = it }

                        // Gravity Slider
                        LabeledSlider(
                            label = "Gravity",
                            valueDisplay = "$gravityAmount m/s²",
                            value = gravityAmount,
                            range = 0f..25f,
                            colors = colors
                        ) { gravityAmount = it }

                        Spacer(Modifier.height(AppDesign.spacingSmall))

                        // Friction Toggle
                        ToggleRow(
                            label = "Air Resistance",
                            checked = frictionEnabled,
                            onCheckedChange = { frictionEnabled = it },
                            colors = colors
                        )

                        AnimatedVisibility(
                            visible = frictionEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            LabeledSlider(
                                label = "Damping Factor",
                                valueDisplay = String.format(Locale.US, "%.4f", frictionAmount),
                                value = frictionAmount,
                                range = 0f..0.005f,
                                colors = colors
                            ) {
                                frictionAmount = it
                                pendulums.forEach { p -> p.logic.setFriction(it.toDouble()) }
                            }
                        }

                        Spacer(Modifier.height(AppDesign.spacingSmall))

                        // Visual Options
                        ToggleRow(
                            label = "Color by Velocity (Heatmap)",
                            checked = colorByVelocity,
                            onCheckedChange = { colorByVelocity = it },
                            colors = colors
                        )
                    }
                }
            }
        }

        // ── Pendulum Management ───────────────────────────────────
        GlassCard(colors = colors) {
            Column(
                modifier = Modifier.padding(AppDesign.spacingLarge)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPendulumManagerExpanded = !isPendulumManagerExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pendulum Manager",
                        color = colors.textPrimary,
                        fontSize = AppDesign.textBodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        if (isPendulumManagerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = colors.textSecondary
                    )
                }

                Spacer(Modifier.height(AppDesign.spacingSmall))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                ) {
                    // Gradient Border Add Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(AppDesign.buttonHeightSmall)
                            .clip(RoundedCornerShape(AppDesign.radiusButton))
                            .background(colors.cardSurface.copy(AppDesign.opacityLow))
                            .border(
                                BorderStroke(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(colors.accentCyan, colors.accentViolet)
                                    )
                                ),
                                RoundedCornerShape(AppDesign.radiusButton)
                            )
                            .clickable {
                                val last = pendulums.lastOrNull()
                                val nextT1 = last?.t1 ?: templateT1
                                val nextT2 = last?.t2 ?: templateT2
                                val color = Color.hsv(Random.nextFloat() * 360f, 0.7f, 0.9f)
                                pendulums.add(PendulumInstance(nextId++, color, nextT1, nextT2))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                modifier = Modifier.size(AppDesign.iconSmall),
                                tint = colors.accentCyan
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Add",
                                fontSize = AppDesign.textSmall,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Chaos Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(AppDesign.buttonHeightSmall)
                            .clip(RoundedCornerShape(AppDesign.radiusButton))
                            .background(colors.accentViolet.copy(AppDesign.opacityLow))
                            .border(
                                2.dp,
                                colors.accentViolet.copy(AppDesign.opacityMedium),
                                RoundedCornerShape(AppDesign.radiusButton)
                            )
                            .clickable {
                                repeat(5) {
                                    val last = pendulums.lastOrNull()
                                    val baseT1 = last?.t1 ?: templateT1
                                    val baseT2 = last?.t2 ?: templateT2
                                    val nextT1 = String.format(
                                        Locale.US,
                                        "%.1f",
                                        (baseT1.toDoubleOrNull() ?: 90.0) + 0.2
                                    )
                                    val randomColor =
                                        Color.hsv(Random.nextFloat() * 360f, 0.7f, 0.9f)
                                    pendulums.add(
                                        PendulumInstance(
                                            nextId++,
                                            randomColor,
                                            nextT1,
                                            baseT2
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Cloud,
                                null,
                                modifier = Modifier.size(AppDesign.iconSmall),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Chaos",
                                fontSize = AppDesign.textSmall,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Remove All Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(AppDesign.buttonHeightSmall)
                            .clip(RoundedCornerShape(AppDesign.radiusButton))
                            .background(colors.accentHell.copy(AppDesign.opacityLow))
                            .border(
                                2.dp,
                                colors.accentHell.copy(AppDesign.opacityMedium),
                                RoundedCornerShape(AppDesign.radiusButton)
                            )
                            .clickable {
                                pendulums.clear()
                                nextId = 0
                                running = false
                                hasStarted = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DeleteForever,
                                null,
                                tint = colors.accentHell,
                                modifier = Modifier.size(AppDesign.iconSmall)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Clear",
                                fontSize = AppDesign.textSmall,
                                color = colors.accentHell,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isPendulumManagerExpanded,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(
                        animationSpec = tween(
                            300
                        )
                    ),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = AppDesign.spacingSmall)
                            .animateContentSize(animationSpec = tween(250)),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Show only first 5 settings to avoid UI lag if grid is spawned
                        val displayList = if (pendulums.size > 5) pendulums.take(5) else pendulums
                        Column(verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)) {
                            displayList.forEachIndexed { _, p ->
                                PendulumSettingsCard(
                                    p,
                                    colors,
                                    pendulums.size > 1,
                                    onParameterChange = { hasStarted = false }) {
                                    if (pendulums.size == 1) {
                                        templateT1 = p.t1
                                        templateT2 = p.t2
                                    }
                                    pendulums.remove(p)
                                    if (pendulums.isEmpty()) {
                                        nextId = 0
                                    }
                                }
                            }
                            if (pendulums.size > 5) {
                                Text(
                                    "... and ${pendulums.size - 5} more pendulums",
                                    color = colors.textSecondary,
                                    fontSize = AppDesign.textSmall,
                                    modifier = Modifier.padding(start = AppDesign.spacingMedium)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Action Buttons ────────────────────────────────────────
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
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight),
                shape = RoundedCornerShape(AppDesign.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = if (running) colors.accentHell else colors.accentCyan)
            ) {
                Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(AppDesign.spacingSmall))
                Text(
                    if (running) "Pause" else if (hasStarted) "Resume" else "Simulate",
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = hasStarted,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Button(
                    onClick = {
                        running = false; hasStarted =
                        false; pendulums.forEach { it.trail.clear(); it.angleTrail.clear(); it.updatePositions() }
                    },
                    modifier = Modifier.height(AppDesign.buttonHeight),
                    shape = RoundedCornerShape(AppDesign.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardSurface),
                    border = BorderStroke(AppDesign.borderStandard, colors.accentCyan)
                ) {
                    Text("Reset", color = colors.accentCyan)
                }
            }
        }

        // ── Visualization Canvas ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDesign.canvasHeightSmall)
                .clip(RoundedCornerShape(AppDesign.radiusCanvas))
                .background(
                    Brush.radialGradient(
                        listOf(
                            colors.accentViolet.copy(0.08f),
                            colors.cardSurface.copy(0.7f)
                        )
                    )
                )
                .border(
                    1.dp,
                    colors.cardBorder.copy(0.3f),
                    RoundedCornerShape(AppDesign.radiusCanvas)
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(displayMode) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (running) return@detectDragGestures
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touch = offset - center

                                if (displayMode == DisplayMode.SIMULATION) {
                                    for (p in pendulums.asReversed()) {
                                        if ((touch - p.bob1 * scale).getDistance() < AppDesign.sidebarButtonSize.toPx() * 0.8f) {
                                            draggingPendulumId = p.id; draggingBobType =
                                                DragTarget.BOB1; hasStarted =
                                                false; return@detectDragGestures
                                        }
                                        if ((touch - p.bob2 * scale).getDistance() < AppDesign.sidebarButtonSize.toPx() * 0.8f) {
                                            draggingPendulumId = p.id; draggingBobType =
                                                DragTarget.BOB2; hasStarted =
                                                false; return@detectDragGestures
                                        }
                                    }
                                } else if (displayMode == DisplayMode.GRAPH) {
                                    val graphScale = scale * 0.004f
                                    for (p in pendulums.asReversed()) {
                                        val currentPos = Offset(
                                            p.t1.toFloatOrNull() ?: 0f,
                                            p.t2.toFloatOrNull() ?: 0f
                                        )
                                        if ((touch - currentPos * graphScale).getDistance() < AppDesign.sidebarButtonSize.toPx() * 0.8f) {
                                            draggingPendulumId = p.id
                                            draggingBobType = DragTarget.ANGLE_DOT
                                            hasStarted = false
                                            return@detectDragGestures
                                        }
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                if (running || draggingPendulumId == null) return@detectDragGestures
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touch = (change.position - center)
                                val p = pendulums.find { it.id == draggingPendulumId }
                                    ?: return@detectDragGestures

                                if (displayMode == DisplayMode.SIMULATION) {
                                    val touchScaled = touch / scale
                                    if (draggingBobType == DragTarget.BOB1) p.t1 = String.format(
                                        Locale.US,
                                        "%.1f",
                                        atan2(
                                            touchScaled.x.toDouble(),
                                            touchScaled.y.toDouble()
                                        ) * 180.0 / PI
                                    )
                                    else {
                                        val rel = touchScaled - p.bob1; p.t2 = String.format(
                                            Locale.US,
                                            "%.1f",
                                            atan2(rel.x.toDouble(), rel.y.toDouble()) * 180.0 / PI
                                        )
                                    }
                                } else if (displayMode == DisplayMode.GRAPH) {
                                    val graphScale = scale * 0.004f
                                    val touchScaled = touch / graphScale
                                    p.t1 =
                                        String.format(Locale.US, "%.1f", touchScaled.x.toDouble())
                                    p.t2 =
                                        String.format(Locale.US, "%.1f", touchScaled.y.toDouble())
                                }
                            },
                            onDragEnd = {
                                draggingPendulumId = null; draggingBobType = DragTarget.NONE
                            },
                            onDragCancel = {
                                draggingPendulumId = null; draggingBobType = DragTarget.NONE
                            }
                        )
                    }
            ) {
                when (displayMode) {
                    DisplayMode.SIMULATION -> {
                        translate(size.width / 2f, size.height / 2f) {
                            pendulums.forEach { p ->
                                drawChaosTrail(p.trail, p.currentColor, scale)
                                drawLine(
                                    p.currentColor,
                                    Offset.Zero,
                                    p.bob1 * scale,
                                    2.5f,
                                    StrokeCap.Round
                                )
                                drawLine(
                                    p.currentColor,
                                    p.bob1 * scale,
                                    p.bob2 * scale,
                                    2.5f,
                                    StrokeCap.Round
                                )
                                drawCircle(p.currentColor, AppDesign.radiusSmall.toPx() * 0.75f, p.bob1 * scale)
                                drawCircle(p.currentColor, AppDesign.radiusSmall.toPx(), p.bob2 * scale)
                            }
                            drawCircle(colors.pivot, AppDesign.spacingExtraSmall.toPx(), Offset.Zero)
                        }
                    }

                    DisplayMode.GRAPH -> {
                        translate(size.width / 2f, size.height / 2f) {
                            // Draw Grid/Axes
                            val axisColor = colors.textSecondary.copy(AppDesign.opacityMedium)
                            drawLine(
                                axisColor,
                                Offset(-size.width / 2, 0f),
                                Offset(size.width / 2, 0f),
                                1f
                            )
                            drawLine(
                                axisColor,
                                Offset(0f, -size.height / 2),
                                Offset(0f, size.height / 2),
                                1f
                            )

                            val graphScale = scale * 0.004f
                            pendulums.forEach { p ->
                                if (p.angleTrail.size > 1) {
                                    // Optimization: Use a single Path instead of thousands of drawLine calls
                                    val path = Path().apply {
                                        val start = p.angleTrail.first() * graphScale
                                        moveTo(start.x, start.y)
                                        for (i in 1 until p.angleTrail.size) {
                                            val point = p.angleTrail[i] * graphScale
                                            lineTo(point.x, point.y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = p.currentColor.copy(AppDesign.opacityMedium),
                                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                                    )
                                }

                                // Current position dot
                                val currentT1 =
                                    if (hasStarted) (p.logic.thetaOne * 180.0 / PI).toFloat() else (p.t1.toFloatOrNull()
                                        ?: 0f)
                                val currentT2 =
                                    if (hasStarted) (p.logic.thetaTwo * 180.0 / PI).toFloat() else (p.t2.toFloatOrNull()
                                        ?: 0f)
                                drawCircle(
                                    p.currentColor,
                                    AppDesign.radiusSmall.toPx() * 0.75f,
                                    Offset(currentT1, currentT2) * graphScale
                                )
                            }
                        }
                    }
                }
            }

            // ── Sidebar Navigation ──
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = AppDesign.spacingMedium),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DisplayModeButton(
                    icon = Icons.Default.Timeline,
                    selected = displayMode == DisplayMode.SIMULATION,
                    colors = colors
                ) { displayMode = DisplayMode.SIMULATION }

                DisplayModeButton(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    selected = displayMode == DisplayMode.GRAPH,
                    colors = colors
                ) { displayMode = DisplayMode.GRAPH }

                Spacer(Modifier.height(10.dp))

                // Clear Trails Button
                IconButton(
                    onClick = { pendulums.forEach { it.trail.clear(); it.angleTrail.clear() } },
                    modifier = Modifier
                        .size(AppDesign.sidebarButtonSize)
                        .background(
                            colors.accentHell.copy(0.15f),
                            RoundedCornerShape(AppDesign.radiusSmall)
                        )
                        .border(
                            1.dp,
                            colors.accentHell.copy(0.3f),
                            RoundedCornerShape(AppDesign.radiusSmall)
                        )
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        null,
                        tint = colors.accentHell,
                        modifier = Modifier.size(AppDesign.iconMedium)
                    )
                }
            }

            // ── Zoom Handler (Right Sidebar) ──
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = AppDesign.spacingMedium)
                    .width(AppDesign.sidebarButtonSize)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(AppDesign.termsBoxHeight)
                        .background(
                            colors.cardSurface.copy(AppDesign.opacityMedium),
                            RoundedCornerShape(AppDesign.radiusCard)
                        )
                        .border(
                            1.dp,
                            colors.cardBorder.copy(AppDesign.opacityMedium),
                            RoundedCornerShape(AppDesign.radiusCard)
                        )
                        .padding(vertical = AppDesign.spacingMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = colors.accentCyan,
                            modifier = Modifier.size(AppDesign.iconSmallMedium)
                        )

                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 11f..550f,
                            modifier = Modifier
                                .weight(1f)
                                .layout { measurable: Measurable, constraints ->
                                    val placeable = measurable.measure(
                                        constraints.copy(
                                            minWidth = constraints.minHeight,
                                            maxWidth = constraints.maxHeight,
                                            minHeight = constraints.minWidth,
                                            maxHeight = constraints.maxWidth
                                        )
                                    )
                                    layout(placeable.height, placeable.width) {
                                        placeable.placeWithLayer(
                                            x = -(placeable.width - placeable.height) / 2,
                                            y = (placeable.width - placeable.height) / 2
                                        ) {
                                            rotationZ = -90f
                                        }
                                    }
                                },
                            colors = SliderDefaults.colors(
                                thumbColor = colors.accentCyan,
                                activeTrackColor = colors.accentCyan,
                                inactiveTrackColor = colors.fieldBorder.copy(AppDesign.opacityMedium)
                            )
                        )

                        Icon(
                            Icons.Default.Remove,
                            null,
                            tint = colors.accentCyan,
                            modifier = Modifier.size(AppDesign.iconSmallMedium)
                        )

                        Text(
                            "${(scale / 1.1f).toInt()}%",
                            color = colors.textPrimary,
                            fontSize = AppDesign.textCaption,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Energy Dashboard Overlay
            EnergyDashboard(
                modifier = Modifier
                    .padding(bottom = AppDesign.spacingLarge)
                    .align(Alignment.BottomCenter),
                pendulums = pendulums,
                colors = colors
            )
        }


        GlassCard(colors = colors) {
            Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
                Text(
                    "How it works",
                    color = colors.textPrimary,
                    fontSize = AppDesign.textTitle,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppDesign.spacingSmall))
                Text(
                    "The Double Pendulum constitutes a compelling physics experiment that demonstrates the elegance of physical principles, illustrating how a system can simultaneously exhibit unpredictability and determinism. By establishing the initial angles (θ1 and θ2) and subsequently releasing the pendulum, one can observe the behavior of the system under controlled conditions.",
                    color = colors.textSecondary,
                    fontSize = AppDesign.textBodyLarge,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EnergyDashboard(
    modifier: Modifier,
    pendulums: List<PendulumInstance>,
    colors: AppColors
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Kinetic Energy",
            color = colors.textSecondary,
            fontSize = AppDesign.textOverline,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = AppDesign.spacingExtraSmall)
        )
        pendulums.take(3).forEach { p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(AppDesign.radiusSmall)
                        .clip(CircleShape)
                        .background(p.currentColor)
                )
                Spacer(Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { (p.kineticEnergy.toFloat() * 0.015f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(100.dp)
                        .height(AppDesign.spacingExtraSmall),
                    color = p.currentColor,
                    trackColor = Color.White.copy(alpha = AppDesign.opacityLow)
                )
            }
            Spacer(Modifier.height(AppDesign.spacingExtraSmall))
        }
    }
}

@Composable
private fun DisplayModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    colors: AppColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(AppDesign.sidebarButtonSize)
            .clickable { onClick() },
        shape = RoundedCornerShape(AppDesign.radiusSmall),
        color = if (selected) colors.accentCyan.copy(AppDesign.opacityLow) else colors.cardSurface.copy(
            AppDesign.opacityMedium
        ),
        border = BorderStroke(
            1.dp,
            if (selected) colors.accentCyan else colors.cardBorder.copy(AppDesign.opacityMedium)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDesign.iconMedium),
                tint = if (selected) colors.accentCyan else colors.textSecondary
            )
        }
    }
}

@Composable
fun LabeledSlider(
    label: String,
    valueDisplay: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    colors: AppColors,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = AppDesign.spacingSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = colors.textPrimary,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valueDisplay,
                color = colors.accentCyan,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(colors.accentCyan.copy(AppDesign.opacityLow), RoundedCornerShape(AppDesign.radiusSmall))
                    .padding(horizontal = AppDesign.spacingSmall, vertical = 2.dp)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentCyan,
                activeTrackColor = colors.accentCyan,
                inactiveTrackColor = colors.fieldBorder.copy(AppDesign.opacityMedium)
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.textFieldHeight)
            .clip(RoundedCornerShape(AppDesign.radiusSmall))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Let the Row handle the click
            colors = CheckboxDefaults.colors(
                checkedColor = colors.accentCyan,
                uncheckedColor = colors.fieldBorder,
                checkmarkColor = colors.textOnAccent
            )
        )
        Spacer(Modifier.width(AppDesign.spacingSmall))
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = AppDesign.textBodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PendulumSettingsCard(
    p: PendulumInstance,
    colors: AppColors,
    showDel: Boolean,
    onParameterChange: () -> Unit,
    onDel: () -> Unit
) {
    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { p.isExpanded = !p.isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(AppDesign.radiusSmall)
                            .clip(CircleShape)
                            .background(p.currentColor)
                    )
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(
                        "Pendulum  ${p.id}",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = AppDesign.textBodyLarge
                    )
                }
                Row {
                    if (showDel) IconButton(
                        onClick = onDel,
                        modifier = Modifier.size(AppDesign.iconLarge)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = colors.accentHell,
                            modifier = Modifier.size(AppDesign.iconSmall)
                        )
                    }
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Icon(
                        if (p.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = colors.textSecondary
                    )
                }
            }
            AnimatedVisibility(
                visible = p.isExpanded,
                enter = expandVertically(animationSpec = tween(50)) + fadeIn(
                    animationSpec = tween(
                        50
                    )
                ),
                exit = shrinkVertically(animationSpec = tween(50)) + fadeOut(
                    animationSpec = tween(
                        50
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(top = AppDesign.spacingSmall),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        PendulumField("L1", p.l1, colors) {
                            p.l1 = it; onParameterChange()
                        }; PendulumField("θ1 (°)", p.t1, colors) { p.t1 = it; onParameterChange() }
                    }
                    Column(Modifier.weight(1f)) {
                        PendulumField("L2", p.l2, colors) {
                            p.l2 = it; onParameterChange()
                        }; PendulumField("θ2 (°)", p.t2, colors) { p.t2 = it; onParameterChange() }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawChaosTrail(trail: List<Offset>, color: Color, scale: Float) {
    if (trail.size < 2) return
    for (i in 0 until trail.size - 1) {
        // Gradient transparency from tail (index 0) to tip (last index)
        val alpha = (i.toFloat() / trail.size) * 0.8f
        drawLine(
            color.copy(alpha = alpha),
            trail[i] * scale,
            trail[i + 1] * scale,
            2.5f,
            StrokeCap.Round
        )
    }
}

@Composable
private fun PendulumField(
    label: String,
    value: String,
    colors: AppColors,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, fontSize = AppDesign.textCaption) },
        textStyle = TextStyle(color = colors.textPrimary, fontSize = AppDesign.textBody),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true, modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDesign.spacingSmall),
        shape = RoundedCornerShape(AppDesign.radiusSmall),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.fieldFocused,
            focusedLabelColor = colors.accentCyan,
            unfocusedBorderColor = colors.fieldBorder
        )
    )
}
