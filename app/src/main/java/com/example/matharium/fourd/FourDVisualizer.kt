package com.example.matharium.fourd

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import com.example.matharium.app.AppColors
import com.example.matharium.app.AppDesign

@Composable
fun FourDVisualizer(
    shape: FourDShape,
    dimensions: Int,
    isRotating: Boolean,
    colors: AppColors
) {
    // Current accumulated rotation matrix
    var rotationMatrix by remember { 
        mutableStateOf(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)) 
    }
    var autoAngle by remember { mutableDoubleStateOf(0.0) }

    // Reset rotation when shape or dimensions change significantly? 
    // Usually better to keep it unless the user explicitly resets.

    // Animation frame for automatic rotation
    LaunchedEffect(isRotating) {
        if (isRotating) {
            while (true) {
                withFrameNanos {
                    autoAngle += 0.006
                }
            }
        }
    }

    // Geometry data
    val (baseVertices, edges, faces) = remember(dimensions, shape) {
        FourDLogic.generateShape(shape, dimensions)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    
                    // Virtual Trackball Logic:
                    // A drag in X should rotate around the screen's Y axis.
                    // A drag in Y should rotate around the screen's X axis.
                    val sensitivity = 0.01
                    val deltaX = dragAmount.x.toDouble() * sensitivity
                    val deltaY = dragAmount.y.toDouble() * sensitivity
                    
                    // Create small rotation matrices for this frame's delta
                    val rotX = FourDLogic.rotationMatrix(1.0, 0.0, 0.0, -deltaY)
                    val rotY = FourDLogic.rotationMatrix(0.0, 1.0, 0.0, -deltaX)
                    
                    // Update global rotation matrix (Accumulate)
                    val deltaRot = FourDLogic.multiplyMatrices(rotY, rotX)
                    rotationMatrix = FourDLogic.multiplyMatrices(deltaRot, rotationMatrix)
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val scale = size.minDimension / 4f

        // 1. Apply N-Dimensional automatic rotation first
        val autoRotated = FourDLogic.rotateHigherDims(baseVertices, dimensions, autoAngle)
        
        // 2. Apply the user-controlled 3D rotation matrix
        val userRotated = autoRotated.map { p ->
            FourDLogic.transform3D(p, rotationMatrix)
        }
        
        // 3. Project to 2D
        val projected = FourDLogic.projectNDto2D(userRotated, dimensions)

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
                strokeWidth = AppDesign.strokeStandard,
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
                        strokeWidth = AppDesign.strokeThin,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw vertices (only for CUBE, or as small dots if needed)
        if (shape == FourDShape.CUBE) {
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
