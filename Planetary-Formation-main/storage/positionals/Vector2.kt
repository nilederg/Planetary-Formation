package storage.positionals

import java.util.function.DoubleUnaryOperator

class Vector2 : Vector {
    // Initialization with value input
    constructor(values: DoubleArray) {
        this.values = values
    }

    // Initialization without value input (overload)
    constructor() {
        values = doubleArrayOf(0.0, 0.0)
    }

    // Evaluates an arbitrary operation on each component, then returns the result
    fun mutateWithLambda(operation: DoubleUnaryOperator) {
        val newVals: DoubleArray = values
        for (i in 0..1) {
            newVals[i] = operation.applyAsDouble(newVals[i])
        }
        values = newVals
    }

    // Returns a clone of this vector
    public override fun clone(): Vector2 {
        val out: Vector2 = Vector2()
        System.arraycopy(values, 0, out.values, 0, 2)
        return out
    }

    // Gets relative pos of input (offset) vector to this (base) vector
    // (vec - this)
    public override fun getRel(vec: Vector): Vector2 {
        val outVec: Vector2 = Vector2()
        for (i in 0..1) outVec.values[i] = vec.values[i] - values[i]
        return outVec
    }

    fun getX(): Double {
        return values[0]
    }

    fun getY(): Double {
        return values[1]
    }

    fun setX(x: Double) {
        values[0] = x
    }

    fun setY(y: Double) {
        values[1] = y
    }

    companion object {
        fun fromCoords(x: Double, y: Double): Vector2 {
            return Vector2(doubleArrayOf(x, y))
        }

        // Return the sum of two vectors
        fun sum(vecA: Vector2, vecB: Vector2): Vector2 {
            val output: Vector2 = Vector2()
            output.setX(vecA.getX() + vecB.getX())
            output.setY(vecA.getY() + vecB.getY())
            return output
        }
    }
}