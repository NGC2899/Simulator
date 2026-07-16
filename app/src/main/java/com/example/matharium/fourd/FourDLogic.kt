package com.example.matharium.fourd

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

object FourDLogic {

    fun generateShape(shape: FourDShape, dim: Int): Triple<List<PointND>, List<Edge>, List<Face>> {
        return when (shape) {
            FourDShape.CUBE -> {
                val (v, e) = generateHypercube(dim)
                Triple(v, e, emptyList())
            }
            FourDShape.MOBIUS_STRIP -> generateMobiusStrip(dim)
            FourDShape.KLEIN_BOTTLE -> generateKleinBottle(dim)
        }
    }

    private fun generateHypercube(dim: Int): Pair<List<PointND>, List<Edge>> {
        val vertices = mutableListOf<PointND>()
        val numVertices = 1 shl dim
        val normalization = 1.0 / sqrt(dim.toDouble())

        for (i in 0 until numVertices) {
            val coords = DoubleArray(dim)
            for (j in 0 until dim) {
                coords[j] = (if ((i shr j) and 1 == 1) 1.5 else -1.5) * normalization
            }
            vertices.add(PointND(coords))
        }

        val edges = mutableListOf<Edge>()
        for (i in 0 until numVertices) {
            for (j in 0 until dim) {
                val neighbor = i xor (1 shl j)
                if (neighbor > i) {
                    edges.add(Edge(i, neighbor))
                }
            }
        }
        return Pair(vertices, edges)
    }

    private fun generateMobiusStrip(dim: Int): Triple<List<PointND>, List<Edge>, List<Face>> {
        val vertices = mutableListOf<PointND>()
        val faces = mutableListOf<Face>()
        val uRes = 30
        val vRes = 10
        for (i in 0 until uRes) {
            val u = (i.toDouble() / uRes) * 2 * PI
            for (j in 0 until vRes) {
                val v = (j.toDouble() / vRes) * 2.0 - 1.0
                val coords = DoubleArray(dim)
                val x = (1 + (v / 2) * cos(u / 2)) * cos(u)
                val y = (1 + (v / 2) * cos(u / 2)) * sin(u)
                val z = (v / 2) * sin(u / 2)
                if (dim >= 1) coords[0] = x
                if (dim >= 2) coords[1] = y
                if (dim >= 3) coords[2] = z
                for (k in 3 until dim) coords[k] = 0.0
                vertices.add(PointND(coords))
            }
        }

        for (i in 0 until uRes) {
            for (j in 0 until vRes - 1) {
                val iNext = (i + 1) % uRes
                val p1 = i * vRes + j
                val p2 = iNext * vRes + j
                val p3 = iNext * vRes + (j + 1)
                val p4 = i * vRes + (j + 1)
                faces.add(Face(p1, p2, p3, p4))
            }
        }
        return Triple(vertices, emptyList(), faces)
    }

    private fun generateKleinBottle(dim: Int): Triple<List<PointND>, List<Edge>, List<Face>> {
        val vertices = mutableListOf<PointND>()
        val faces = mutableListOf<Face>()
        val uRes = 25
        val vRes = 25
        for (i in 0 until uRes) {
            val u = (i.toDouble() / uRes) * 2 * PI
            for (j in 0 until vRes) {
                val v = (j.toDouble() / vRes) * 2 * PI
                val coords = DoubleArray(dim)
                val r = 1.0
                val x = (r + cos(u/2) * sin(v) - sin(u/2) * sin(2*v)) * cos(u)
                val y = (r + cos(u/2) * sin(v) - sin(u/2) * sin(2*v)) * sin(u)
                val z = sin(u/2) * sin(v) + cos(u/2) * sin(2*v)
                val w = cos(u/2)
                if (dim >= 1) coords[0] = x
                if (dim >= 2) coords[1] = y
                if (dim >= 3) coords[2] = z
                if (dim >= 4) coords[3] = w
                for (k in 4 until dim) coords[k] = 0.0
                vertices.add(PointND(coords))
            }
        }

        for (i in 0 until uRes) {
            val iNext = (i + 1) % uRes
            for (j in 0 until vRes) {
                val jNext = (j + 1) % vRes
                val p1 = i * vRes + j
                val p2 = iNext * vRes + j
                val p3 = iNext * vRes + jNext
                val p4 = i * vRes + jNext
                faces.add(Face(p1, p2, p3, p4))
            }
        }
        return Triple(vertices, emptyList(), faces)
    }

    /**
     * In-place multiplication of a point by a 3x3 matrix (applied to the first 3 dims)
     */
    fun transform3D(p: PointND, matrix: DoubleArray): PointND {
        if (p.coords.size < 3) return p
        val c = p.coords
        val res = c.copyOf()
        res[0] = matrix[0] * c[0] + matrix[1] * c[1] + matrix[2] * c[2]
        res[1] = matrix[3] * c[0] + matrix[4] * c[1] + matrix[5] * c[2]
        res[2] = matrix[6] * c[0] + matrix[7] * c[1] + matrix[8] * c[2]
        return PointND(res)
    }

    /**
     * Generates a rotation matrix for an axis and angle
     */
    fun rotationMatrix(axisX: Double, axisY: Double, axisZ: Double, angle: Double): DoubleArray {
        val c = cos(angle)
        val s = sin(angle)
        val t = 1.0 - c
        
        // Axis normalization
        val len = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
        if (len < 1e-6) return doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)
        val x = axisX / len
        val y = axisY / len
        val z = axisZ / len

        return doubleArrayOf(
            t * x * x + c,     t * x * y - s * z, t * x * z + s * y,
            t * x * y + s * z, t * y * y + c,     t * y * z - s * x,
            t * x * z - s * y, t * y * z + s * x, t * z * z + c
        )
    }

    /**
     * Matrix multiplication for 3x3 matrices
     */
    fun multiplyMatrices(a: DoubleArray, b: DoubleArray): DoubleArray {
        val res = DoubleArray(9)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                res[i * 3 + j] = a[i * 3 + 0] * b[0 * 3 + j] +
                                 a[i * 3 + 1] * b[1 * 3 + j] +
                                 a[i * 3 + 2] * b[2 * 3 + j]
            }
        }
        return res
    }

    fun rotateHigherDims(points: List<PointND>, dim: Int, autoAngle: Double): List<PointND> {
        if (dim <= 3) return points
        return points.map { p ->
            val coords = p.coords.copyOf()
            // Rotate in higher planes (3,4), (4,5) etc. or (0, dim-1)
            for (i in 0 until dim - 1 step 2) {
                val a = i
                val b = dim - 1
                if (a >= dim || b >= dim) continue
                
                val currentAngle = autoAngle * (1.0 + i * 0.1)
                val valA = coords[a]
                val valB = coords[b]
                coords[a] = valA * cos(currentAngle) - valB * sin(currentAngle)
                coords[b] = valA * sin(currentAngle) + valB * cos(currentAngle)
            }
            PointND(coords)
        }
    }

    fun projectNDto2D(points: List<PointND>, dim: Int, viewDistance: Double = 5.0): List<PointND> {
        var currentPoints = points
        var currentDim = dim
        while (currentDim > 2) {
            val d = currentDim
            currentPoints = currentPoints.map { p ->
                val coords = p.coords
                val factor = viewDistance / (viewDistance - coords[d - 1])
                val projectedCoords = DoubleArray(d - 1)
                for (i in 0 until d - 1) {
                    projectedCoords[i] = coords[i] * factor
                }
                PointND(projectedCoords)
            }
            currentDim--
        }
        return currentPoints
    }
}
