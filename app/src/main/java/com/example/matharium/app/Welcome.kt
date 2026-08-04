package com.example.matharium.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import com.example.matharium.R

@Composable
fun WelcomeScreen(
    onNavigateToDoublePendulum: () -> Unit,
    onNavigateToFourierSeries: () -> Unit,
    onNavigateToVoiceProcessing: () -> Unit,
    onNavigateToFourD: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val colors = LocalAppColors.current

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(AppDesign.welcomeSectionSpacing))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = AppDesign.welcomeSectionSpacing)
        ) {
            Text(
                text = "Welcome to",
                color = colors.textSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Text(
                text = "Matharium",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        listOf(colors.textPrimary, colors.accentCyan.copy(alpha = 0.7f))
                    ),
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "Explore the beauty of mathematics",
                color = colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge),
            horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge),
            contentPadding = PaddingValues(bottom = AppDesign.spacingLarge)
        ) {
            // Fourier Series Card
            item {
                EntranceAnimation(visible = showContent, index = 0) {
                    SimulationCard(
                        title = "Fourier\nSeries",
                        colors = colors,
                        onClick = onNavigateToFourierSeries,
                        imageVector = FluentIcons.FontAwesomeWaveSquare,
                        tint = colors.accentCyan
                    )
                }
            }

            // Voice Processing Card
            item {
                EntranceAnimation(visible = showContent, index = 1) {
                    SimulationCard(
                        title = "Voice\nProcessing",
                        colors = colors,
                        onClick = onNavigateToVoiceProcessing,
                        imageVector = FluentIcons.BootstrapSoundwave
                    )
                }
            }

            // Double Pendulum Card
            item {
                EntranceAnimation(visible = showContent, index = 2) {
                    SimulationCard(
                        title = "Double\nPendulum",
                        colors = colors,
                        onClick = onNavigateToDoublePendulum,
                        imageVector = FluentIcons.FluentuiSystemIconsDataLine
                    )
                }
            }

            // 4D Simulation Card
            item {
                EntranceAnimation(visible = showContent, index = 3) {
                    SimulationCard(
                        title = "4D\nSimulation",
                        colors = colors,
                        onClick = onNavigateToFourD,
                        imageVector = FluentIcons.FluentuiSystemIconsCubeMultiple
                    )
                }
            }

            // Settings Card
            item {
                EntranceAnimation(visible = showContent, index = 4) {
                    SimulationCard(
                        title = "Settings",
                        colors = colors,
                        onClick = onNavigateToSettings,
                        imageVector = FluentIcons.FluentuiSystemIconsSettingsCogMultiple
                    )
                }
            }

            // Future Placeholder
            item {
                EntranceAnimation(visible = showContent, index = 5) {
                    EmptyCard(colors = colors)
                }
            }
        }
    }
}

@Composable
fun EntranceAnimation(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    var animatedVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible && !animatedVisible) {
            delay((index * 100L).milliseconds)
            animatedVisible = true
        }
    }

    AnimatedVisibility(
        visible = animatedVisible,
        enter = fadeIn(tween(800, easing = EaseOutCubic)) +
                slideInVertically(tween(600, easing = EaseOutCubic)) { it / 2 },
    ) {
        content()
    }
}

@Composable
fun SimulationCard(
    title: String,
    colors: AppColors,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tint: androidx.compose.ui.graphics.Color = colors.accentCyan
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = 0.35f))
            .border(
                1.dp,
                colors.cardBorder.copy(alpha = 0.5f),
                RoundedCornerShape(AppDesign.radiusCard)
            )
            .clickable { onClick() }
            .padding(AppDesign.spacingLarge)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun EmptyCard(colors: AppColors) {
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .border(
                width = 1.dp,
                color = colors.cardBorder.copy(alpha = 0.15f),
                shape = RoundedCornerShape(AppDesign.radiusCard)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = FluentIcons.AppsRegular,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.2f),
            modifier = Modifier.size(32.dp)
        )
    }
}
