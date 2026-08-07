package com.example.matharium.fourd.state

import androidx.compose.runtime.*
import com.example.matharium.fourd.engine.FourDLogic
import kotlinx.coroutines.*

/**
 * Manages the state and transformation pipeline for 4D objects.
 */
class FourDState(
    val scope: CoroutineScope
) {
    var selectedShape by mutableStateOf(FourDShape.CUBE)
    var dimensions by mutableIntStateOf(4)
    var isRotating by mutableStateOf(false)
    
    // Accumulate rotation matrix from user input
    var rotationMatrix by mutableStateOf(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0))
    var autoAngle by mutableDoubleStateOf(0.0)

    // Cached geometry (3D or ND)
    private var baseVertices = listOf<PointND>()
    var edges = listOf<Edge>()
        private set
    var faces = listOf<Face>()
        private set

    // Final 2D Projected points for rendering
    var projectedPoints = listOf<PointND>()
        private set

    init {
        updateShape()
    }

    fun updateShape() {
        val (v, e, f) = FourDLogic.generateShape(selectedShape, dimensions)
        baseVertices = v
        edges = e
        faces = f
        updateProjection()
    }

    /**
     * Simulation loop for automatic rotation.
     */
    suspend fun runRotationLoop() {
        if (!isRotating) return
        while (isRotating) {
            withFrameNanos {
                autoAngle += 0.006
            }
            updateProjection()
        }
    }

    fun rotateUser(deltaX: Double, deltaY: Double) {
        val sensitivity = 0.01
        val rotX = FourDLogic.rotationMatrix(1.0, 0.0, 0.0, -deltaY * sensitivity)
        val rotY = FourDLogic.rotationMatrix(0.0, 1.0, 0.0, -deltaX * sensitivity)
        val deltaRot = FourDLogic.multiplyMatrices(rotY, rotX)
        rotationMatrix = FourDLogic.multiplyMatrices(deltaRot, rotationMatrix)
        updateProjection()
    }

    /**
     * Executes the N-Dimensional projection pipeline.
     * This is moved out of the Canvas for performance.
     */
    fun updateProjection() {
        // 1. Higher-dim rotation
        val autoRotated = FourDLogic.rotateHigherDims(baseVertices, dimensions, autoAngle)
        
        // 2. User 3D rotation
        val userRotated = autoRotated.map { p ->
            FourDLogic.transform3D(p, rotationMatrix)
        }
        
        // 3. Project to 2D
        projectedPoints = FourDLogic.projectNDto2D(userRotated, dimensions)
    }
}

@Composable
fun rememberFourDState(scope: CoroutineScope = rememberCoroutineScope()): FourDState {
    return remember { FourDState(scope) }
}