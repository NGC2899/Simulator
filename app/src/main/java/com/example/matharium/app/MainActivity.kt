package com.example.matharium.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.matharium.fourier.FourierSeries
import com.example.matharium.fourier.VoiceProcessing
import com.example.matharium.pendulum.DoublePendulum
import com.example.matharium.fourd.FourDScreen
import com.example.matharium.R

enum class Screen {
    Welcome,
    FourierSeries,
    DoublePendulum,
    VoiceProcessing,
    FourD,
    Settings,
}

val LocalAppPrefs = staticCompositionLocalOf<AppPreferences> { error("No AppPreferences provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPreferences(this)
        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }
            var isAnimatedBg by remember { mutableStateOf(prefs.isAnimatedBackground) }

            CompositionLocalProvider(LocalAppPrefs provides prefs) {
                AppTheme(darkTheme = isDarkTheme) {
                    MainContainer(
                        isAnimatedBg = isAnimatedBg,
                        onToggleTheme = {
                            isDarkTheme = !isDarkTheme
                            prefs.isDarkTheme = isDarkTheme
                        },
                        onToggleAnimatedBg = {
                            isAnimatedBg = it
                            prefs.isAnimatedBackground = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContainer(
    isAnimatedBg: Boolean,
    onToggleTheme: () -> Unit,
    onToggleAnimatedBg: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current
    var currentScreen by remember { mutableStateOf(Screen.Welcome) }

    BackHandler(enabled = currentScreen != Screen.Welcome) {
        currentScreen = Screen.Welcome
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
        if (isAnimatedBg) {
            AnimatedBlobBackground(
                blob1Color = colors.accentCyan,
                blob2Color = colors.accentViolet,
                label = "mainBlobs"
            ) {
                ScreenTransition(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    onToggleTheme = onToggleTheme,
                    isAnimatedBg = isAnimatedBg,
                    onToggleAnimatedBg = onToggleAnimatedBg
                )
            }
        } else {
            ScreenTransition(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it },
                onToggleTheme = onToggleTheme,
                isAnimatedBg = isAnimatedBg,
                onToggleAnimatedBg = onToggleAnimatedBg
            )
        }
    }
}

@Composable
fun ScreenTransition(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onToggleTheme: () -> Unit,
    isAnimatedBg: Boolean,
    onToggleAnimatedBg: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current
    
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != Screen.Welcome) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(
                    slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(
                    slideOutHorizontally { it } + fadeOut())
            } using SizeTransform(clip = false)
        }, label = "screenTransition",
        modifier = Modifier.fillMaxSize()
    ) { screen ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar integrated into sliding content
            if (screen != Screen.Welcome) {
                TopNavigationBar(
                    title = when (screen) {
                        Screen.FourierSeries -> "Fourier Series"
                        Screen.DoublePendulum -> "Double Pendulum"
                        Screen.VoiceProcessing -> "Voice Processing"
                        Screen.FourD -> "4D Simulation"
                        Screen.Settings -> "Settings"
                        else -> ""
                    },
                    colors = colors,
                    onBack = { onNavigate(Screen.Welcome) }
                )
            }

            // Screen Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppDesign.spacingLarge)
            ) {
                when (screen) {
                    Screen.Welcome -> WelcomeScreen(
                        onNavigateToDoublePendulum = {
                            onNavigate(Screen.DoublePendulum)
                        },
                        onNavigateToFourierSeries = { onNavigate(Screen.FourierSeries) },
                        onNavigateToVoiceProcessing = { onNavigate(Screen.VoiceProcessing) },
                        onNavigateToFourD = { onNavigate(Screen.FourD) },
                        onNavigateToSettings = { onNavigate(Screen.Settings) }
                    )

                    Screen.FourierSeries -> FourierSeries()
                    Screen.DoublePendulum -> DoublePendulum()
                    Screen.VoiceProcessing -> VoiceProcessing()
                    Screen.FourD -> FourDScreen()
                    Screen.Settings -> SettingsScreen(
                        isAnimatedBg = isAnimatedBg,
                        onToggleAnimatedBg = onToggleAnimatedBg,
                        onToggleTheme = onToggleTheme
                    )
                }
            }
        }
    }
}

@Composable
fun TopNavigationBar(
    title: String,
    colors: AppColors,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.navBarHeight)
            .padding(AppDesign.spacingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(AppDesign.buttonHeight)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AppDesign.radiusCard))
                .background(colors.cardSurface.copy(alpha = 0.45f))
                .border(
                    1.dp,
                    colors.cardBorder.copy(alpha = 0.6f),
                    RoundedCornerShape(AppDesign.radiusCard)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.menu_outline),
                contentDescription = "Menu",
                tint = colors.accentCyan,
                modifier = Modifier.size(AppDesign.iconMedium)
            )
        }

        Text(
            text = title,
            fontSize = AppDesign.textHeadlineLarge,
            fontWeight = FontWeight.Bold
        )

        // Empty placeholder to keep title centered if we want, or just leave it.
        // For now, let's just remove the theme toggle Box.
        Spacer(modifier = Modifier.size(AppDesign.buttonHeight))
    }
}
