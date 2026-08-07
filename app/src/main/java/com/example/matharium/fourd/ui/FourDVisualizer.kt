package com.example.matharium.fourd.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.matharium.app.AppColors
import com.example.matharium.app.AppDesign
import com.example.matharium.fourd.state.*

/**
 * Rendering Layer for 4D Objects.
 * Only responsible for drawing pre-projected points.
 */
@Composable
fun FourDVisualizer(
    state: FourDState,
    colors: AppColors
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    state.rotateUser(dragAmount.x.toDouble(), dragAmount.y.toDouble())
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val scale = 80.dp.toPx()

        val projected = state.projectedPoints
        val edges = state.edges
        val faces = state.faces

        if (projected.isEmpty()) return@Canvas

        // Draw edges
        edges.forEach { edge ->
            val p1 = projected[edge.p1].coords
            val p2 = projected[edge.p2].coords

            drawLine(
                color = colors.accentCyan.copy(alpha = 0.8f),
                start = Offset(
                    centerX + p1[0].toFloat() * scale,
                    centerY + p1[1].toFloat() * scale
                ),
                end = Offset(
                    centerX + p2[0].toFloat() * scale,
                    centerY + p2[1].toFloat() * scale
                ),
                strokeWidth = AppDesign.strokeStandard.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw faces (Wireframe)
        faces.forEach { face ->
            val pts = listOfNotNull(
                projected.getOrNull(face.p1),
                projected.getOrNull(face.p2),
                projected.getOrNull(face.p3),
                face.p4?.let { projected.getOrNull(it) }
            ).map { it.coords }

            if (pts.size >= 3) {
                for (i in pts.indices) {
                    val p1 = pts[i]
                    val p2 = pts[(i + 1) % pts.size]
                    drawLine(
                        color = colors.accentCyan.copy(alpha = 0.4f),
                        start = Offset(
                            centerX + p1[0].toFloat() * scale,
                            centerY + p1[1].toFloat() * scale
                        ),
                        end = Offset(
                            centerX + p2[0].toFloat() * scale,
                            centerY + p2[1].toFloat() * scale
                        ),
                        strokeWidth = AppDesign.strokeThin.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw vertices (only for CUBE)
        if (state.selectedShape == FourDShape.CUBE) {
            projected.forEach { p ->
                drawCircle(
                    color = colors.accentViolet,
                    radius = AppDesign.spacingExtraSmall.toPx(),
                    center = Offset(
                        centerX + p.coords[0].toFloat() * scale,
                        centerY + p.coords[1].toFloat() * scale
                    )
                )
            }
        }
    }
}