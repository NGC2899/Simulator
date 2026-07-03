package com.example.myapplication.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.fourier.FourierSeries
import com.example.myapplication.pendulum.DoublePendulum

enum class Screen {
    Welcome,
    FourierSeries,
    DoublePendulum,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                AnimatedVisibility(
                    visible = currentScreen != Screen.Welcome,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    TopNavigationBar(
                        title = when (currentScreen) {
                            Screen.FourierSeries -> "Fourier Series"
                            Screen.DoublePendulum -> "Double Pendulum"
                            else -> ""
                        },
                        colors = colors,
                        onBack = { currentScreen = Screen.Welcome }
                    )
                }

                // Screen Content
                Box(modifier = Modifier.weight(1f)) {
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
                        }, label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            Screen.Welcome -> WelcomeScreen(
                                onNavigateToDoublePendulum = { currentScreen = Screen.DoublePendulum },
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
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(AppDesign.navBarHeight)
            .padding(horizontal = AppDesign.spacingLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(AppDesign.sidebarButtonSize)
                .clip(RoundedCornerShape(AppDesign.radiusSmall))
                .background(colors.cardSurface.copy(alpha = AppDesign.opacityLow))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.accentCyan
            )
        }

        Spacer(modifier = Modifier.width(AppDesign.spacingMedium))

        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = AppDesign.textHeadline,
            fontWeight = FontWeight.Bold
        )
    }
}
