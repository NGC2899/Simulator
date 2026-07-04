package com.example.myapplication.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.fourier.FourierSeries
import com.example.myapplication.pendulum.DoublePendulum

enum class Screen {
    Welcome,
    FourierSeries,
    DoublePendulum,
}

val LocalAppPrefs = staticCompositionLocalOf<AppPreferences> { error("No AppPreferences provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPreferences(this)
        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }
            CompositionLocalProvider(LocalAppPrefs provides prefs) {
                AppTheme(darkTheme = isDarkTheme) {
                    MainContainer(
                        onToggleTheme = {
                            isDarkTheme = !isDarkTheme
                            prefs.isDarkTheme = isDarkTheme
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContainer(onToggleTheme: () -> Unit) {
    val colors = LocalAppColors.current
    var currentScreen by remember { mutableStateOf(Screen.Welcome) }

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
            label = "mainBlobs"
        ) {
            // Entire Screen with Slide Transition
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
                                else -> ""
                            },
                            colors = colors,
                            onBack = { currentScreen = Screen.Welcome },
                            onToggleTheme = onToggleTheme
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
                                    currentScreen = Screen.DoublePendulum
                                },
                                onNavigateToFourierSeries = { currentScreen = Screen.FourierSeries }
                            )

                            Screen.FourierSeries -> FourierSeries()
                            Screen.DoublePendulum -> DoublePendulum()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopNavigationBar(
    title: String,
    colors: AppColors,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
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
                .size(AppDesign.sidebarButtonSize)
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
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.accentCyan,
            )
        }

        Text(
            text = title,
            fontSize = AppDesign.textHeadline,
            fontWeight = FontWeight.Bold
        )

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
                    onClick = onToggleTheme
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (colors.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle Theme",
                tint = colors.accentCyan,
            )
        }
    }
}
