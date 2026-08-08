package com.example.matharium.app

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Animation Durations for AppDesign
val AppDesign.animDurationFast get() = 150
val AppDesign.animDurationStandard get() = 300
val AppDesign.animDurationSlow get() = 500

data class BlobAnimParams(
    val xStart: Float, val xEnd: Float,
    val yStart: Float, val yEnd: Float,
    val duration: Int
)

@Composable
fun AnimatedBlobBackground(
    blob2Color: Color,
    blob1Color: Color,
    blob1Alpha: Float = AppDesign.opacityLow,
    blob2Alpha: Float = AppDesign.opacityLow,
    blob1Size: Float = 800f,
    blob2Size: Float = 600f,
    label: String = "blobs",
    content: @Composable BoxScope.() -> Unit
) {
    val animParams = remember {
        Pair(
            BlobAnimParams(
                xStart = kotlin.random.Random.nextFloat() * 400f - 200f,
                xEnd = kotlin.random.Random.nextFloat() * 400f - 200f,
                yStart = kotlin.random.Random.nextFloat() * 600f,
                yEnd = kotlin.random.Random.nextFloat() * 600f,
                duration = kotlin.random.Random.nextInt(8000, 15000)
            ),
            BlobAnimParams(
                xStart = kotlin.random.Random.nextFloat() * 400f - 200f,
                xEnd = kotlin.random.Random.nextFloat() * 400f - 200f,
                yStart = kotlin.random.Random.nextFloat() * 600f,
                yEnd = kotlin.random.Random.nextFloat() * 600f,
                duration = kotlin.random.Random.nextInt(10000, 18000)
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = label)
    val b1X by infiniteTransition.animateFloat(
        initialValue = animParams.first.xStart, targetValue = animParams.first.xEnd,
        animationSpec = infiniteRepeatable(
            tween(animParams.first.duration),
            RepeatMode.Reverse
        ), label = "${label}_b1x"
    )
    val b1Y by infiniteTransition.animateFloat(
        initialValue = animParams.first.yStart, targetValue = animParams.first.yEnd,
        animationSpec = infiniteRepeatable(
            tween(animParams.first.duration + 2000),
            RepeatMode.Reverse
        ), label = "${label}_b1y"
    )
    val b2X by infiniteTransition.animateFloat(
        initialValue = animParams.second.xStart, targetValue = animParams.second.xEnd,
        animationSpec = infiniteRepeatable(
            tween(animParams.second.duration),
            RepeatMode.Reverse
        ), label = "${label}_b2x"
    )
    val b2Y by infiniteTransition.animateFloat(
        initialValue = animParams.second.yStart, targetValue = animParams.second.yEnd,
        animationSpec = infiniteRepeatable(
            tween(animParams.second.duration + 3000),
            RepeatMode.Reverse
        ), label = "${label}_b2y"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = b1X.dp, y = b1Y.dp)
                .size(blob1Size.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(blob1Color.copy(alpha = blob1Alpha), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .offset(x = b2X.dp, y = b2Y.dp)
                .size(blob2Size.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(blob2Color.copy(alpha = blob2Alpha), Color.Transparent)
                    )
                )
        )
        content()
    }
}
