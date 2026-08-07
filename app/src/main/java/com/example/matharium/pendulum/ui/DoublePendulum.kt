package com.example.matharium.pendulum.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.matharium.app.*
import com.example.matharium.R
import com.example.matharium.pendulum.state.*
import com.example.matharium.pendulum.engine.*
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
    val vibrate = rememberAppVibrator()
    val state = rememberDoublePendulumState(prefs, colors)

    // Sync State to Persistence
    LaunchedEffect(state.scale) { prefs.pendulumScale = state.scale }
    LaunchedEffect(state.frictionEnabled) { prefs.pendulumFrictionEnabled = state.frictionEnabled }
    LaunchedEffect(state.frictionAmount) { prefs.pendulumFrictionAmount = state.frictionAmount }
    LaunchedEffect(state.gravityAmount) { prefs.pendulumGravityAmount = state.gravityAmount }
    LaunchedEffect(state.speedMultiplier) { prefs.pendulumSpeedMultiplier = state.speedMultiplier }
    LaunchedEffect(state.colorByVelocity) { prefs.pendulumColorByVelocity = state.colorByVelocity }

    // Save configuration on list changes
    LaunchedEffect(state.pendulums.size) {
        prefs.savePendulums(state.pendulums.toList())
    }

    // Physics Loop moved to State
    LaunchedEffect(state.running) {
        state.runSimulation()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {

        // --- FIXED TOP SECTION ---
        DoublePendulumVisualizer(
            colors = colors,
            pendulums = state.pendulums,
            running = state.running,
            onHasStartedChange = { state.hasStarted = it },
            displayMode = state.displayMode,
            onDisplayModeChange = { state.displayMode = it },
            scale = state.scale,
            onScaleChange = { state.scale = it },
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
                    vibrate(true)
                    if (!state.hasStarted) {
                        state.pendulums.forEach { it.initialize(state.gravityAmount); it.trail.clear(); it.angleTrail.clear() }
                        state.hasStarted = true
                    }
                    state.running = !state.running
                },
                enabled = state.pendulums.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(AppDesign.buttonHeight),
                shape = RoundedCornerShape(AppDesign.radiusMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.running) colors.accentHell else colors.accentCyan,
                    disabledContainerColor = colors.accentCyan.copy(alpha = 0.3f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = if (state.running) FluentIcons.MaterialIconsPause else FluentIcons.VscodeCodiconsTriangleRight,
                    null,
                    tint = colors.textOnAccent,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
                Spacer(Modifier.width(AppDesign.spacingSmall))
                Text(
                    if (state.running) "Pause" else if (state.hasStarted) "Resume" else "Simulate",
                    fontWeight = FontWeight.Bold,
                    color = colors.textOnAccent,
                )
            }

            AnimatedVisibility(
                visible = state.hasStarted,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Button(
                    onClick = {
                        state.resetAll()
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

        // --- SCROLLABLE BOTTOM SECTION ---

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(AppDesign.radiusCard))
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
        ) {
            // Physics Environment
            DoublePendulumEnvironment(
                colors = colors,
                speedMultiplier = state.speedMultiplier,
                onSpeedChange = { state.speedMultiplier = it },
                gravityAmount = state.gravityAmount,
                onGravityChange = { state.gravityAmount = it },
                frictionEnabled = state.frictionEnabled,
                onFrictionEnabledChange = { state.frictionEnabled = it },
                frictionAmount = state.frictionAmount,
                onFrictionAmountChange = { state.frictionAmount = it },
                colorByVelocity = state.colorByVelocity,
                onColorByVelocityChange = { state.colorByVelocity = it }
            )

            // Pendulum Management
            DoublePendulumManager(
                colors = colors,
                pendulums = state.pendulums,
                running = state.running,
                onRunningChange = { state.running = it },
                hasStarted = state.hasStarted,
                onHasStartedChange = { state.hasStarted = it },
                nextId = state.nextId,
                onNextIdChange = { state.nextId = it },
                prefs = prefs
            )

            // Energy Distribution (Only in Complex Mode)
            AnimatedVisibility(
                visible = state.displayMode == PendulumDisplayMode.COMPLEX,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
        EnergyDashboard(
                    modifier = Modifier.fillMaxWidth(),
                    pendulums = state.pendulums,
                    colors = colors
                )
            }

            // Explainer Card
            GlassCard(colors = colors) {
                Column(modifier = Modifier.padding(AppDesign.spacingLarge)) {
                    Text(
                        "How it works",
                        fontSize = AppDesign.textHeadline,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(AppDesign.spacingSmall))
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
