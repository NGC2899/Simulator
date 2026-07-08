package com.example.matharium.pendulum

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DoublePendulumConstants {
    const val PHYSICS_DT = 0.004
    const val MAX_ELAPSED_TIME = 0.1
    const val MAX_PHYSICS_STEPS = 20

    const val TRAIL_MAX_POINTS = 120
    const val ANGLE_TRAIL_MAX_POINTS = 1000

    const val VELOCITY_COLOR_MULTIPLIER = 0.2f
    const val VELOCITY_HUE_OFFSET = 200f
    const val VELOCITY_HUE_SCALE = 100f
    const val VECTOR_LINE_SCALE = 25f
    const val GRAPH_RENDER_SCALE = 0.004f
    const val ZOOM_RANGE_START = 11f
    const val ZOOM_RANGE_END = 550f
    const val ZOOM_DISPLAY_DIVISOR = 1.1f

    const val MAX_PENDULUMS = 30
    const val CHAOS_BATCH_COUNT = 5
    const val CHAOS_ANGLE_INCREMENT = 0.2
    const val INITIAL_DISPLAY_COUNT = 5

    const val ENERGY_PROGRESS_MULTIPLIER = 0.015f
    val ENERGY_PROGRESS_WIDTH = 100.dp
    val LINE_HEIGHT_EXPLAINER = 20.sp

    const val COLOR_SATURATION = 0.7f
    const val COLOR_VALUE = 0.9f
    const val COLOR_SATURATION_ALT = 0.8f

    val BOB_RADIUS_SCALE = 0.75f
    val SLIDER_SPEED_RANGE = 0.1f..5f
    val SLIDER_GRAVITY_RANGE = 0f..25f
    val SLIDER_FRICTION_RANGE = 0f..0.005f
    val SPACER_TINY = 6.dp
}
