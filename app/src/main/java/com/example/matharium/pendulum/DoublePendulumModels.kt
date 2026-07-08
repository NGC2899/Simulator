package com.example.matharium.pendulum

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI

class PendulumInstance(
    val id: Int,
    val baseColor: Color,
    t1Initial: String = "90.0",
    t2Initial: String = "90.0"
) {
    var t1 by mutableStateOf(t1Initial)
    var t2 by mutableStateOf(t2Initial)
    var l1 by mutableStateOf("1.0")
    var l2 by mutableStateOf("1.0")

    // Snapshot of angles when simulation starts to allow 'Reset' to return here
    private var startT1 = t1Initial
    private var startT2 = t2Initial

    var bob1 by mutableStateOf(Offset.Zero)
    var bob2 by mutableStateOf(Offset.Zero)
    var kineticEnergy by mutableDoubleStateOf(0.0)
    var currentColor by mutableStateOf(baseColor)
    var isExpanded by mutableStateOf(false)

    val trail = mutableStateListOf<Offset>()
    val angleTrail = mutableStateListOf<Offset>()

    val logic = DoublePendulumLogic()

    fun updatePositions() {
        val t1v = (t1.toDoubleOrNull() ?: 0.0) * PI / 180.0
        val t2v = (t2.toDoubleOrNull() ?: 0.0) * PI / 180.0
        val l1v = l1.toDoubleOrNull() ?: 1.0
        val l2v = l2.toDoubleOrNull() ?: 1.0
        logic.initialize(t1v, t2v, 0.0, 0.0, l1v, l2v, 1.0, 1.0)
        val coords = logic.currentCoords
        bob1 = Offset(coords.x1.toFloat(), coords.y1.toFloat())
        bob2 = Offset(coords.x2.toFloat(), coords.y2.toFloat())
    }

    fun initialize(gravity: Float) {
        startT1 = t1
        startT2 = t2
        logic.setGravity(gravity.toDouble())
        updatePositions()
    }

    fun reset() {
        t1 = startT1
        t2 = startT2
        trail.clear()
        angleTrail.clear()
        updatePositions()
    }
}

enum class DragTarget { NONE, BOB1, BOB2, ANGLE_DOT }
enum class DisplayMode { SIMULATION, GRAPH, COMPLEX }
