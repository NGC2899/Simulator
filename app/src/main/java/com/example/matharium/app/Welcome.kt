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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
    onNavigateToVoiceProcessing: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val colors = LocalAppColors.current

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.welcomenav),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
//                .offset(x = 20.dp, y = (-30).dp)
//                .rotate(-28f),
            contentScale = ContentScale.Fit,
//            alpha = 0.7f
        )

        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.welcomefooter),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.Fit,
            alpha = 0.3f
        )

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDesign.welcomeHeaderHeight),
                verticalArrangement = Arrangement.SpaceEvenly,
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

                HorizontalDivider(
                    Modifier
                        .width(50.dp)
                        .height(5.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.accentCyan, colors.bgTop)
                            ),
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
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(AppDesign.radiusCard)),
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
                        imageVector = FluentIcons.PhosphorWaveSquare,
                        tint = colors.accentCyan,
                        definition = "Visualize functions as sums of sines and cosines"
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
                        imageVector = FluentIcons.BootstrapSoundwave,
                        tint = colors.accentViolet,
                        definition = "Analyze and transform sounds with mathematics"
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
                        imageVector = FluentIcons.FluentuiSystemIconsDataLine,
                        tint = colors.yellow,
                        definition = "Simulate chaos and beautiful motion"
                    )
                }
            }

            // Settings Card
            item {
                EntranceAnimation(visible = showContent, index = 3) {
                    SimulationCard(
                        title = "Settings",
                        colors = colors,
                        onClick = onNavigateToSettings,
                        imageVector = FluentIcons.FluentuiSystemIconsSettingsCogMultiple,
                        tint = colors.gray,
                        definition = "Customize your experience"
                    )
                }
            }

            // Future Placeholder
            item {
                EntranceAnimation(visible = showContent, index = 4) {
                    EmptyCard(colors = colors)
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
        enter = fadeIn(tween(800, easing = EaseOutCubic)),
    ) {
        content()
    }
}

@Composable
fun SimulationCard(
    title: String,
    definition: String,
    colors: AppColors,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(AppDesign.opacityLow), colors.bgTop),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 1000f)

                )
            )
            .border(
                1.dp,
                tint.copy(AppDesign.opacitySubtle),
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
                modifier = Modifier.size(AppDesign.iconWelcome)
            )

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = AppDesign.textHeadlineLarge,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )

            Text(
                text = definition,
                color = colors.textSecondary,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EmptyCard(colors: AppColors) {
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .drawBehind {
                drawRoundRect(
                    color = colors.cardBorder.copy(AppDesign.opacityHigh),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(25f, 25f) // 10px dash, 10px gap
                        )
                    ),
                    cornerRadius = CornerRadius(
                        AppDesign.radiusCard.toPx(),
                        AppDesign.radiusCard.toPx()
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = FluentIcons.FluentuiSystemIconsAppsAddIn,
            contentDescription = null,
            tint = colors.cardBorder.copy(AppDesign.opacityHigh),
            modifier = Modifier.size(AppDesign.iconWelcome)
        )
    }
}
