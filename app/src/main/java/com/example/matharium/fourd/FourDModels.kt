package com.example.matharium.fourd

enum class FourDShape {
    CUBE,
    MOBIUS_STRIP,
    KLEIN_BOTTLE
}

data class PointND(val coords: DoubleArray) {
    val dimensions: Int get() = coords.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PointND) return false
        return coords.contentEquals(other.coords)
    }

    override fun hashCode(): Int {
        return coords.contentHashCode()
    }
}

data class Edge(val p1: Int, val p2: Int)

data class Face(val p1: Int, val p2: Int, val p3: Int, val p4: Int? = null)
