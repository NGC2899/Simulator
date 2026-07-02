package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

private enum class Screen {
    FourierSeries,
    DoublePendulum,
    Welcome,
    ComingSoon
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Welcome) }
    var drawerOpen by remember { mutableStateOf(false) }
    var darkTheme by remember { mutableStateOf(false) }

    AppTheme(darkTheme = darkTheme) {
        val colors = LocalAppColors.current

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.bgTop
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Background gradient ──
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(colors.bgTop, colors.bgBottom)
                            )
                        )
                )

                // ── Screen content (blurred when drawer open) ──
                val blurRadius by animateDpAsState(
                    targetValue = if (drawerOpen) AppDesign.blurStandard else 0.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "blur"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .let { if (blurRadius > 0.dp) it.blur(blurRadius) else it }
                        .statusBarsPadding()
                        .padding(
                            start = if (currentScreen == Screen.Welcome) AppDesign.borderNone else AppDesign.spacingLarge,
                            top = if (currentScreen == Screen.Welcome) AppDesign.borderNone else AppDesign.navBarHeight,
                            end = if (currentScreen == Screen.Welcome) AppDesign.borderNone else AppDesign.spacingLarge,
                            bottom = if (currentScreen == Screen.Welcome) AppDesign.borderNone else AppDesign.spacingHeadline
                        )
                ) {
                    when (currentScreen) {
                        Screen.Welcome        -> WelcomeScreen(
                            onNavigateToDoublePendulum = { currentScreen = Screen.DoublePendulum },
                            onNavigateToFourierSeries = { currentScreen = Screen.FourierSeries }
                        )
                        Screen.FourierSeries  -> FourierSeries()
                        Screen.DoublePendulum -> DoublePendulum()
                        Screen.ComingSoon     -> Unit
                    }
                }

                // ── Full-screen slide-in menu ──
                AnimatedVisibility(
                    visible = drawerOpen,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)) + slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 400)) + slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (colors.isDark) colors.cardSurface else Color.White)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(colors.accentCyan.copy(alpha = 0.18f), Color.Transparent),
                                    center = Offset(-100f, 400f),
                                    radius = 1000f
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { drawerOpen = false }
                    ) {
                        AnimatedBlobBackground(
                            blob1Color = colors.accentCyan,
                            blob2Color = colors.accentViolet,
                            blob1Alpha = 0.12f,
                            blob2Alpha = 0.12f,
                            blob1Size = 950f,
                            blob2Size = 750f,
                            label = "drawerBlobs"
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .padding(top = 100.dp, start = 32.dp, end = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Navigation",
                                    fontSize = AppDesign.textTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentCyan,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(AppDesign.spacingSmall))

                                val menuItems = listOf(
                                    Screen.Welcome to "🏠  Welcome Home",
                                    Screen.FourierSeries to "📈  Fourier Series",
                                    Screen.DoublePendulum to "🪄  Double Pendulum",
                                    Screen.ComingSoon to "🔮  Coming Soon"
                                )

                                menuItems.forEachIndexed { index, (screen, label) ->
                                    // Use a separate key for items to ensure animations restart
                                    key(screen) {
                                        AnimatedVisibility(
                                            visible = drawerOpen,
                                            enter = slideInHorizontally(
                                                initialOffsetX = { -50 },
                                                animationSpec = tween(durationMillis = 600, delayMillis = 150 + index * 100)
                                            ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 150 + index * 100)),
                                            exit = fadeOut(animationSpec = tween(durationMillis = 200))
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = AppDesign.textHeadlineLarge,
                                                fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Medium,
                                                color = if (currentScreen == screen) colors.textPrimary else colors.textSecondary,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(AppDesign.radiusLarge))
                                                    .clickable {
                                                        currentScreen = screen
                                                        drawerOpen = false
                                                    }
                                                    .padding(horizontal = AppDesign.spacingLarge, vertical = AppDesign.spacingMedium)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Tap anywhere to close",
                                color = colors.textSecondary.copy(alpha = 0.5f),
                                fontSize = AppDesign.textBodyLarge,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 60.dp)
                            )
                        } // AnimatedBlobBackground
                    } // outer Box
                }

                // ── Top nav bar ──
                if (currentScreen != Screen.Welcome) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = AppDesign.spacingLarge + AppDesign.spacingSmall, vertical = AppDesign.spacingMedium)
                    ) {
                        // Nav pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppDesign.radiusPill))
                                .background(colors.cardSurface.copy(alpha = AppDesign.opacityHigh + AppDesign.opacitySubtle))
                                .border(AppDesign.borderThin, colors.cardBorder, RoundedCornerShape(AppDesign.radiusPill))
                                .padding(horizontal = AppDesign.navPillPaddingHorizontal, vertical = AppDesign.navPillPaddingVertical),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Hamburger / Close
                            NavIconButton(
                                label = if (drawerOpen) "✕" else "☰",
                                onClick = { drawerOpen = !drawerOpen },
                                colors = colors
                            )

                            // App title
                            Text(
                                text = when (currentScreen) {
                                    Screen.Welcome -> "🏠  Welcome"
                                    Screen.FourierSeries -> "📈  Fourier Series"
                                    Screen.DoublePendulum -> "🪄  Double Pendulum"
                                    else -> "🔮  Coming Soon"
                                },
                                fontSize = AppDesign.textTitle,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )

                            // Theme toggle
                            NavIconButton(
                                label = if (darkTheme) "☀️" else "🌙",
                                onClick = { darkTheme = !darkTheme },
                                colors = colors
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconButton(
    label: String,
    onClick: () -> Unit,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = AppDesign.textTitleLarge, color = colors.textPrimary)
    }
}
