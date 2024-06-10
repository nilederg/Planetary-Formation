package storage.positionals

import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

abstract class Vector constructor() {
    lateinit var values: DoubleArray
    abstract fun clone(): Vector
    abstract fun getRel(a: Vector): Vector

    // Adds an input vector to this vector and stores the result in this vector
    fun add(vec: Vector) {
        for (i in values.indices) values[i] += vec.values[i]
    }

    // Dot product of this vector and an input vector
    fun dotProduct(vec: Vector): Double {
        var sum: Double = 0.0
        for (i in values.indices) sum += values[i] * vec.values[i]
        return sum
    }

    // Randomize self
    fun randomize() {
        val random: Random = Random()
        for (i in values.indices) values[i] = 2 * random.nextDouble() - 1
    }

    // Randomize to a unit vector
    // No limit to runtime but faster on average than non-probabilistic trig methods
    // monte carlo
    fun randomizeUnit() {
        // Keep randomizing until within a unit sphere
        // Equal distribution of solid angle
        randomize()
        while (absoluteSquare() > 1) {
            randomize()
        }
        // Project inside of sphere to surface - angle preserving
        normalize()
    }

    // Get absolute value of self, squared
    // Returns |self|^2
    // This is for optimization
    // Faster than absolute() and good for comparing dist with a constant
    // I have no idea if the compiler makes this obsolete, so I'm leaving it
    fun absoluteSquare(): Double {
        var dist: Double = 0.0
        for (i in values.indices) dist += values[i].pow(2.0)
        return dist
    }

    // True absolute value
    // Separate for optimization
    fun absolute(): Double {
        return sqrt(absoluteSquare())
    }

    // Returns its component with the largest magnitude
    fun largestComponent(): Int {
        var max: Double = 0.0
        var index: Int = 0
        for (i in values.indices) {
            if (abs(values[i]) > max) {
                max = values[i]
                index = i
            }
        }
        return index
    }

    // Scale self by a factor
    fun scale(factor: Double) {
        for (i in values.indices) values[i] *= factor
    }

    // Normalize self
    // Scale so that magnitude = 1
    fun normalize() {
        val factor: Double = 1 / absolute()
        scale(factor)
    }

    // Normalize to cube
    // Scale so that end of vector touches a face of a cube with edge length 2 (radius 1)
    fun normalizeCube() {
        val factor: Double = 1 / values[largestComponent()]
        scale(factor)
    }

    // Prints vector contents
    fun print() {
        print("[")
        for (i in 0 until (values.size - 1)) print(values[i].toString() + ", ")
        print(values[values.size - 1].toString() + "]")
    }

    // Prints vector contents with a new line
    // Just for convenience :)
    fun println() {
        print()
        kotlin.io.println()
    }

    // Floor the vector's values
    fun floor() {
        for (i in values.indices) values[i] = kotlin.math.floor(values[i])
    }
}