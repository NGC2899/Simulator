package com.example.matharium.pendulum

import kotlin.math.*

class DoublePendulumLogic {
    // -------------------------- State Variables --------------------------

    var thetaOne: Double = 1.571
        private set
    var thetaTwo: Double = 1.571
        private set
    var omegaOne = 0.0
        private set
    var omegaTwo = 0.0
        private set
    var lengthOne = 1.0
        private set
    var lengthTwo = 1.0
        private set
    var massOne = 1.0
        private set
    var massTwo = 1.0
        private set

    private var gravity = 9.80665
    private var friction = 0.0001
    private var frictionEnabled = true

    val currentCoords = Coordinates()

    class Coordinates {
        var x1: Double = 0.0
        var x2: Double = 0.0
        var y1: Double = 0.0
        var y2: Double = 0.0
        var kineticEnergy: Double = 0.0
        var potentialEnergy: Double = 0.0
    }

    // State structure for RK4
    private data class State(val t1: Double, val t2: Double, val w1: Double, val w2: Double)

    // -------------------------- Functions --------------------------

    fun initialize(t1: Double, t2: Double, w1: Double, w2: Double, l1: Double, l2: Double, m1: Double, m2: Double) {
        this.thetaOne = t1
        this.thetaTwo = t2
        this.omegaOne = w1
        this.omegaTwo = w2
        this.lengthOne = l1
        this.lengthTwo = l2
        this.massOne = m1
        this.massTwo = m2
        updateCoordinates()
    }

    fun setFriction(f: Double) { this.friction = f }
    fun setFrictionEnabled(e: Boolean) { this.frictionEnabled = e }
    fun setGravity(g: Double) { this.gravity = g }

    /**
     * RK4 Integration for superior stability.
     */
    fun update(dt: Double): Coordinates {
        val s = State(thetaOne, thetaTwo, omegaOne, omegaTwo)

        val k1 = derivative(s)
        val k2 = derivative(add(s, k1, dt / 2))
        val k3 = derivative(add(s, k2, dt / 2))
        val k4 = derivative(add(s, k3, dt))

        thetaOne += (dt / 6.0) * (k1.t1 + 2 * k2.t1 + 2 * k3.t1 + k4.t1)
        thetaTwo += (dt / 6.0) * (k1.t2 + 2 * k2.t2 + 2 * k3.t2 + k4.t2)
        omegaOne += (dt / 6.0) * (k1.w1 + 2 * k2.w1 + 2 * k3.w1 + k4.w1)
        omegaTwo += (dt / 6.0) * (k1.w2 + 2 * k2.w2 + 2 * k3.w2 + k4.w2)

        // Safety check for numerical instability (NaN or Infinity)
        if (thetaOne.isNaN() || thetaTwo.isNaN() || omegaOne.isNaN() || omegaTwo.isNaN() ||
            thetaOne.isInfinite() || thetaTwo.isInfinite() || omegaOne.isInfinite() || omegaTwo.isInfinite()) {
            // Reset to a safe state if instability occurs
            thetaOne = 0.0
            thetaTwo = 0.0
            omegaOne = 0.0
            omegaTwo = 0.0
        }

        // Apply friction
        if (frictionEnabled) {
            val damping = max(0.0, 1.0 - friction)
            omegaOne *= damping
            omegaTwo *= damping
        }

        return updateCoordinates()
    }

    private fun updateCoordinates(): Coordinates {
        // Calculate positions
        currentCoords.x1 = lengthOne * sin(thetaOne)
        currentCoords.y1 = lengthOne * cos(thetaOne)
        currentCoords.x2 = currentCoords.x1 + lengthTwo * sin(thetaTwo)
        currentCoords.y2 = currentCoords.y1 + lengthTwo * cos(thetaTwo)

        // Energy calculations
        val v1x = lengthOne * omegaOne * cos(thetaOne)
        val v1y = -lengthOne * omegaOne * sin(thetaOne)
        val v2x = v1x + lengthTwo * omegaTwo * cos(thetaTwo)
        val v2y = v1y - lengthTwo * omegaTwo * sin(thetaTwo)

        currentCoords.kineticEnergy = 0.5 * massOne * (v1x * v1x + v1y * v1y) + 0.5 * massTwo * (v2x * v2x + v2y * v2y)
        currentCoords.potentialEnergy = -massOne * gravity * lengthOne * cos(thetaOne) - massTwo * gravity * (lengthOne * cos(thetaOne) + lengthTwo * cos(thetaTwo))

        // Final safety check for positions
        if (currentCoords.x1.isNaN() || currentCoords.y1.isNaN() || currentCoords.x2.isNaN() || currentCoords.y2.isNaN()) {
            currentCoords.x1 = 0.0; currentCoords.y1 = 0.0
            currentCoords.x2 = 0.0; currentCoords.y2 = 0.0
        }

        return currentCoords
    }

    private fun derivative(s: State): State {
        val safeLengthOne = if (lengthOne <= 1e-6) 1e-6 else lengthOne
        val safeLengthTwo = if (lengthTwo <= 1e-6) 1e-6 else lengthTwo
        val safeMassOne = if (massOne <= 1e-6) 1e-6 else massOne
        val safeMassTwo = if (massTwo <= 1e-6) 1e-6 else massTwo

        val delta = s.t1 - s.t2
        var den = 2 * safeMassOne + safeMassTwo - safeMassTwo * cos(2 * delta)
        if (abs(den) < 1e-6) {
            den = if (den >= 0) 1e-6 else -1e-6
        }

        val a1 = (-gravity * (2 * safeMassOne + safeMassTwo) * sin(s.t1)
                - safeMassTwo * gravity * sin(s.t1 - 2 * s.t2)
                - 2 * sin(delta) * safeMassTwo * (s.w2 * s.w2 * safeLengthTwo + s.w1 * s.w1 * safeLengthOne * cos(delta))) / (safeLengthOne * den)

        val a2 = (2 * sin(delta) * (s.w1 * s.w1 * safeLengthOne * (safeMassOne + safeMassTwo)
                + gravity * (safeMassOne + safeMassTwo) * cos(s.t1)
                + s.w2 * s.w2 * safeLengthTwo * safeMassTwo * cos(delta))) / (safeLengthTwo * den)

        return State(s.w1, s.w2, a1, a2)
    }

    private fun add(s: State, d: State, dt: Double): State {
        return State(s.t1 + d.t1 * dt, s.t2 + d.t2 * dt, s.w1 + d.w1 * dt, s.w2 + d.w2 * dt)
    }

    companion object {
        // Logic no longer needs a fixed DT constant as it is passed from the simulation loop
    }
}
