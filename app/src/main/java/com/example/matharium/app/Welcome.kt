package com.example.matharium.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    onNavigateToVoiceProcessing: () -> Unit
) {
    val colors = LocalAppColors.current

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesign.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Welcome to",
            color = colors.accentCyan,
            fontSize = AppDesign.textHeadline,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(AppDesign.spacingSmall))

        Text(
                text = "Matharium",
        fontSize = AppDesign.textDisplayLarge,
        fontWeight = FontWeight.Normal,
        letterSpacing = 1.sp
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fourier Transform Card
            item {
                EntranceAnimation(visible = showContent, index = 0) {
                    SimulationCard(
                        title = "Fourier Series",
                        colors = colors,
                        onClick = onNavigateToFourierSeries,
                    ) {
                        Icon(
                            painterResource(id = R.drawable.fourier_waves),
                            contentDescription = null,
                            tint = colors.textPrimary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(220.dp)
                        )
                    }
                }
            }

            // Voice Processing Card
            item {
                EntranceAnimation(visible = showContent, index = 1) {
                    SimulationCard(
                        title = "Voice Processing",
                        colors = colors,
                        onClick = onNavigateToVoiceProcessing
                    ) {
                        Icon(
                            painterResource(id = R.drawable.mic_outline),
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 40.dp)
                                .size(60.dp)
                        )
                    }
                }
            }

            // Double Pendulum Card
            item {
                EntranceAnimation(visible = showContent, index = 2) {
                    SimulationCard(
                        title = "Double Pendulum",
                        colors = colors,
                        onClick = onNavigateToDoublePendulum
                    ) {
                        Icon(
                            painterResource(id = R.drawable.pendulum_menu),
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 40.dp)
                                .size(60.dp)
                        )
                    }
                }
            }

            // Coming Soon Card
            item {
                EntranceAnimation(visible = showContent, index = 3) {
                    SimulationCard(
                        title = "Coming Soon",
                        colors = colors,
                        enabled = false
                    )
                }
            }

            // Empty Placeholder Cards
            items(2) { index ->
                EntranceAnimation(visible = showContent, index = index + 4) {
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
            delay((index * 120L).milliseconds)
            animatedVisible = true
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(2f)) {
        AnimatedVisibility(
            visible = animatedVisible,
            enter = fadeIn(tween(800, easing = EaseOutCubic)) +
                    slideInHorizontally(tween(600, easing = EaseOutCubic)) { it / -2 },
            modifier = Modifier.matchParentSize()
        ) {
            content()
        }
    }
}

@Composable
fun SimulationCard(
    title: String,
    colors: AppColors,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    icon: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .aspectRatio(3f)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = 0.45f))
            .border(
                1.dp,
                colors.cardBorder.copy(alpha = 0.6f),
                RoundedCornerShape(AppDesign.radiusCard)
            )
            .clickable(enabled = enabled) { onClick() }
    ) {
        if (icon != null) {
            icon()
        }

        Text(
            text = title,
            color = if (enabled) Color.Unspecified else colors.textSecondary.copy(
                alpha = 0.7f
            ),
            fontSize = AppDesign.textBodyLarge,
            textAlign = TextAlign.Start,
            fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 40.dp)
        )
    }
}

@Composable
fun EmptyCard(colors: AppColors) {
    Box(
        modifier = Modifier
            .aspectRatio(3f)
            .border(
                width = AppDesign.borderThin,
                color = colors.cardBorder.copy(alpha = 0.25f),
                shape = RoundedCornerShape(AppDesign.radiusCard)
            )
    )
}
