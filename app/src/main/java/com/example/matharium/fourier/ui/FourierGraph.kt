package com.example.matharium.fourier.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.FourierLogic
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FrequencyDomainGraph(
    spectrumData: List<FourierLogic.Complex>,
    colors: AppColors,
    currentWindingFreq: Float,
    timeProvider: () -> Float
) {
    val spectrumPath = remember { Path() }
    val projectedValuesBuffer = remember(spectrumData.size) { FloatArray(spectrumData.size) }

    GlassCard(colors = colors) {
        Column(modifier = Modifier.padding(AppDesign.radiusLarge)) {
            Text(
                "Frequency Domain (Real-time Center of Mass)",
                fontSize = AppDesign.textHeadline,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(AppDesign.spacingSmall))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(colors.cardSurface.copy(alpha = 0.1f), RoundedCornerShape(AppDesign.radiusSmall))
                    .padding(AppDesign.spacingSmall)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (spectrumData.isEmpty()) return@Canvas
                    
                    val w = size.width
                    val h = size.height
                    val maxFreq = 5.0f
                    val spectrumPoints = spectrumData.size
                    
                    var maxVal = 0.1f
                    for (i in 0 until spectrumPoints) {
                        val f = (i.toFloat() / spectrumPoints) * maxFreq
                        val coeff = spectrumData[i]
                        val angle = 2 * PI.toFloat() * f * timeProvider()
                        val valProj = (coeff.re * cos(angle) + coeff.im * sin(angle)).toFloat()
                        projectedValuesBuffer[i] = valProj
                        val absVal = if (valProj < 0) -valProj else valProj
                        if (absVal > maxVal) maxVal = absVal
                    }
                    
                    val axisColor = colors.textSecondary.copy(alpha = 0.2f)
                    val centerY = h / 2f
                    drawLine(axisColor, Offset(0f, centerY), Offset(w, centerY), 1.dp.toPx())
                    drawLine(axisColor, Offset(0f, 0f), Offset(0f, h), 1.dp.toPx())

                    spectrumPath.reset()
                    for (i in 0 until spectrumPoints) {
                        val x = (i.toFloat() / (spectrumPoints - 1)) * w
                        val y = centerY - (projectedValuesBuffer[i] / maxVal) * (h / 2f) * 0.9f
                        if (i == 0) spectrumPath.moveTo(x, y) else spectrumPath.lineTo(x, y)
                    }
                    
                    drawPath(
                        path = spectrumPath,
                        color = colors.accentCyan,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    val indicatorX = (currentWindingFreq / maxFreq) * w
                    if (indicatorX in 0f..w) {
                        drawLine(
                            color = colors.accentHell,
                            start = Offset(indicatorX, 0f),
                            end = Offset(indicatorX, h),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(AppDesign.spacingSmall))
            Text(
                "The peaks show the frequencies that make up your signal. The dashed line tracks your winding frequency.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
