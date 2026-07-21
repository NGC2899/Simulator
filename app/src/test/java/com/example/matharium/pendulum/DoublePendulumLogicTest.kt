package com.example.matharium.pendulum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DoublePendulumLogicTest {

    @Test
    fun `test update with delta time`() {
        val logic = DoublePendulumLogic()
        logic.initialize(1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0)
        
        val initialCoords = logic.currentCoords
        val x1Before = initialCoords.x1
        
        logic.update(0.01)
        
        val afterCoords = logic.currentCoords
        assertFalse("Pendulum should have moved", x1Before == afterCoords.x1)
    }

    @Test
    fun `test safety check for NaN`() {
        val logic = DoublePendulumLogic()
        // Initialize with infinity to trigger NaN in derivatives
        logic.initialize(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0)
        
        val coords = logic.update(0.01)
        
        assertFalse("x1 should not be NaN", coords.x1.isNaN())
        assertFalse("y1 should not be NaN", coords.y1.isNaN())
        assertEquals("x1 should be reset to 0", 0.0, coords.x1, 1e-6)
        assertEquals("y1 should be reset to 1", 1.0, coords.y1, 1e-6) // lengthOne * cos(0) = 1.0
    }
}
