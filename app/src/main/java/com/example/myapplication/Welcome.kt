package com.example.myapplication

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.bgTop, colors.bgBottom)
                )
            )
    ) {
        AnimatedBlobBackground(
            blob1Color = colors.accentCyan,
            blob2Color = colors.accentViolet,
            blob1Size = 800f,
            blob2Size = 600f,
            label = "welcomeBlobs"
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    text = "Welcome",
                    color = colors.textPrimary,
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "Select to start",
                    color = colors.accentCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Fourier Transform Card
                    item {
                        EntranceAnimation(visible = showContent, index = 1) {
                            SimulationCard(
                                title = "Fourier Series",
                                colors = colors,
                                onClick = onNavigateToFourierSeries
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Waves,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    // Double Pendulum Card
                    item {
                        EntranceAnimation(visible = showContent, index = 0) {
                            SimulationCard(
                                title = "Double Pendulum",
                                colors = colors,
                                onClick = onNavigateToDoublePendulum
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(40.dp)
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
        } // AnimatedBlobBackground
    } // outer Box
}

@Composable
fun EntranceAnimation(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    var animatedVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 100L)
            animatedVisible = true
        }
    }

    AnimatedVisibility(
        visible = animatedVisible,
        enter = fadeIn(tween(800, easing = EaseOutCubic)) +
                slideInVertically(tween(800, easing = EaseOutCubic)) { it / 6 }
    ) {
        content()
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
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(
                if (enabled) colors.cardSurface.copy(alpha = 0.45f)
                else colors.cardSurface.copy(alpha = 0.2f)
            )
            .border(
                width = 1.dp,
                color = if (enabled) colors.cardBorder.copy(alpha = 0.6f)
                else colors.cardBorder.copy(alpha = 0.3f),
                shape = RoundedCornerShape(AppDesign.radiusCard)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                color = if (enabled) colors.textPrimary else colors.textSecondary.copy(alpha = 0.7f),
                fontSize = AppDesign.textTitle,
                textAlign = TextAlign.Center,
                fontWeight = if (enabled) FontWeight.Normal else FontWeight.Light
            )
        }
    }
}

@Composable
fun EmptyCard(colors: AppColors) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 1.dp,
                color = colors.cardBorder.copy(alpha = 0.25f),
                shape = RoundedCornerShape(AppDesign.radiusCard)
            )
    )
}
