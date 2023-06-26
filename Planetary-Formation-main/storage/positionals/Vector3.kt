package storage.positionals

import kotlin.math.sin
import kotlin.math.sqrt

// A 3d vector class
class Vector3 : Vector {
    // Initialization with value input
    constructor(values: DoubleArray) {
        this.values = values
    }

    // Initialization without value input (overload)
    constructor() {
        values = doubleArrayOf(0.0, 0.0, 0.0)
    }

    // Returns a clone of this vector
    public override fun clone(): Vector3 {
        val out: Vector3 = Vector3()
        System.arraycopy(values, 0, out.values, 0, 3)
        return out
    }

    // Gets relative pos of input (offset) vector to this (base) vector
    // (vec - this)
    public override fun getRel(vec: Vector): Vector3 {
        val outVec: Vector3 = Vector3()
        for (i in 0..2) outVec.values[i] = vec.values[i] - values[i]
        return outVec
    }

    fun getX(): Double {
        return values[0]
    }

    fun getY(): Double {
        return values[1]
    }

    fun getZ(): Double {
        return values[2]
    }

    fun setX(x: Double) {
        values[0] = x
    }

    fun setY(y: Double) {
        values[1] = y
    }

    fun setZ(z: Double) {
        values[2] = z
    }

    companion object {
        val zero: Vector3 = Vector3(doubleArrayOf(0.0, 0.0, 0.0))
        fun fromCoords(x: Double, y: Double, z: Double): Vector3 {
            return Vector3(doubleArrayOf(x, y, z))
        }

        // Creates a vector from latitude and longitude
        fun fromSphericalCoordinates(coordinate: GeoCoord): Vector3 {
            val output: Vector3 = Vector3()
            output.setZ(sin(coordinate.latitude))
            val factor: Double = sqrt(1 - output.getZ() * output.getZ())
            output.setX(sin(coordinate.longitude) * factor)
            output.setY(sin(coordinate.longitude) * factor)
            return output
        } // TODO optimize with table lookup

        // Return the sum of two vectors
        fun sum(vecA: Vector3, vecB: Vector3): Vector3 {
            val output: Vector3 = Vector3()
            output.setX(vecA.getX() + vecB.getX())
            output.setY(vecA.getY() + vecB.getY())
            output.setZ(vecA.getZ() + vecB.getZ())
            return output
        }

        fun crossProduct(vecA: Vector3, vecB: Vector3): Vector3 {
            val x: Double = (vecA.getY() * vecB.getZ()) - (vecA.getZ() * vecB.getY())
            val y: Double = (vecA.getZ() * vecB.getX()) - (vecA.getX() * vecB.getZ())
            val z: Double = (vecA.getX() * vecB.getY()) - (vecA.getY() * vecB.getX())
            return Vector3(doubleArrayOf(x, y, z))
        }
    }
}