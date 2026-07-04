package com.example.myapplication.app

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomeScreen(
    onNavigateToDoublePendulum: () -> Unit,
    onNavigateToFourierSeries: () -> Unit
) {
    val colors = LocalAppColors.current

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesign.spacingLarge)
            .padding(bottom = AppDesign.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Āvang",
            fontSize = AppDesign.textDisplayLarge,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(AppDesign.welcomeSectionSpacing))

        Text(
            text = "Select to start",
            color = colors.accentCyan,
            fontSize = AppDesign.textHeadline,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(AppDesign.welcomeGridSpacing))

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
                        onClick = onNavigateToFourierSeries
                    ) {
                        Icon(
                            imageVector = Icons.Default.BlurCircular,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(AppDesign.iconWelcome)
                        )
                    }
                }
            }

            // Double Pendulum Card
            item {
                EntranceAnimation(visible = showContent, index = 1) {
                    SimulationCard(
                        title = "Double Pendulum",
                        colors = colors,
                        onClick = onNavigateToDoublePendulum
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(AppDesign.iconWelcome)
                        )
                    }
                }
            }

            // Coming Soon Card
            item {
                EntranceAnimation(visible = showContent, index = 2) {
                    SimulationCard(
                        title = "Coming Soon",
                        colors = colors,
                        enabled = false
                    )
                }
            }

            // Empty Placeholder Cards
            items(3) { index ->
                EntranceAnimation(visible = showContent, index = index + 3) {
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
    icon: @Composable (() -> Unit)? = null
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
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(AppDesign.spacingLarge)
        ) {
            Text(
                text = title,
                color = if (enabled) androidx.compose.ui.graphics.Color.Unspecified else colors.textSecondary.copy(
                    alpha = 0.7f
                ),
                fontSize = AppDesign.textBodyLarge,
                textAlign = TextAlign.Start,
                fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (icon != null) {
                Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                    icon()
                }

            }
        }
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
