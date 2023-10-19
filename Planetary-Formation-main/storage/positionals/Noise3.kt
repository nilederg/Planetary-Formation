package storage.positionals

import kotlin.math.pow

class Noise3 constructor(dims: IntArray?) {
    private val vectors: Array<Array<Array<Vector3>>>
    val dimensions: IntArray

    // Initialize with custom dimensions
    init {
        // Set dimensions
        dimensions = dims!!.clone()
        // Initialize vector array dimensions
        vectors = Array (dimensions[0]) { Array(dimensions[1]) { Array(dimensions[2]) { Vector3(DoubleArray(3)) } } }
        // Initialize individual vectors (everything starts at 0)
        for (x in 0 until dimensions[0]) for (y in 0 until dimensions[1]) for (z in 0 until dimensions[2]) vectors[x][y][z] = Vector3()
    }

    // Randomizes the internal vectors
    fun randomize() {
        // i wonder if there's a way to get rid of redundant recursive similar for loops
        // without using something crazy like lambdas
        for (x in 0 until dimensions[0]) {
            for (y in 0 until dimensions[1]) {
                for (z in 0 until dimensions[2]) {
                    vectors[x][y][z].randomizeUnit()
                }
            }
        }
    }

    // Randomizes only the relevant vectors for sampling a sphere.
    fun randomizeSphere() {
        if (dimensions[0] != dimensions[1] || dimensions[1] != dimensions[2]) {
            throw IllegalStateException("Noise field is not cubical.")
        }
        val applicableDistance = 3.0 / dimensions[0] // 3 is a wild guess
        val maximumDist = 1 + applicableDistance
        val minimumDist = 1 - applicableDistance
        for (x in 0 until dimensions[0]) {
            for (y in 0 until dimensions[1]) {
                val xf = (2 * x.toDouble() / dimensions[0]) - 1
                val yf = (2 * y.toDouble() / dimensions[1]) - 1
                // We can skip if its outside the cylinder to save time even checking z
                val dist = Math.sqrt((xf * xf) + (yf * yf))
                if (dist > maximumDist) {continue}
                for (z in 0 until dimensions[2]) {
                    val zf = (2 * z.toDouble() / dimensions[2]) - 1
                    // check if it's close to the sphere
                    val dist = Math.sqrt((xf * xf) + (yf * yf) + (zf * zf))
                    if (dist > maximumDist || dist < minimumDist) {continue}
                    vectors[x][y][z].randomizeUnit()
                }
            }
        }
    }

    // Calculates dot product between single internal vector and relative input
    // point vector
    // Vector corner is a position in space that has a vector at its position - it
    // is not the vector itself
    private fun getDotSingle(point: Vector3, corner: Vector3): Double {
        // Get vector to multiply by
        val posX: Int = corner.getX().toInt().coerceAtMost(dimensions[0] - 1)
        val posY: Int = corner.getY().toInt().coerceAtMost(dimensions[1] - 1)
        val posZ: Int = corner.getZ().toInt().coerceAtMost(dimensions[2] - 1)
        val base: Vector3 = vectors[posX][posY][posZ]
        // Point relative to corner
        val offset: Vector3 = corner.getRel(point)
        return base.dotProduct(offset)
    }

    // Calculates value at a point
    fun getPoint(inPoint: Vector3): Double {
        val point: Vector3 = inPoint.clone()
        if (point.getX() >= dimensions[0] - 1) point.setX(dimensions[0] - 1.00000001)
        if (point.getY() >= dimensions[1] - 1) point.setY(dimensions[1] - 1.00000001)
        if (point.getZ() >= dimensions[2] - 1) point.setZ(dimensions[2] - 1.00000001)
        val base: Vector3 = point.clone() // Floored point
        base.floor()

        // Eliminate redundancy by creating redundancy.
        val offset = Vector3()
        val phase: Vector3 = base.getRel(point)
        val xBases: DoubleArray = doubleArrayOf(0.0, 0.0) // For interpolation
        for (xOff in 0..1) {
            val yBases: DoubleArray = doubleArrayOf(0.0, 0.0) // For interpolation
            for (yOff in 0..1) {
                val zBases: DoubleArray = doubleArrayOf(0.0, 0.0) // For interpolation
                for (zOff in 0..1) {
                    // yes I know this is redundant
                    // but the alternative is replacing all '*off's
                    // with a list, and that would look so ugly
                    offset.setX(xOff.toDouble())
                    offset.setY(yOff.toDouble())
                    offset.setZ(zOff.toDouble())
                    offset.add(base)

                    // Use vector to lookup other vector in a 3d array
                    val gradientVec: Vector3 = Vector3()
                    val posX: Int = offset.getX().toInt().coerceAtMost(dimensions[0] - 1)
                    val posY: Int = offset.getY().toInt().coerceAtMost(dimensions[1] - 1)
                    val posZ: Int = offset.getZ().toInt().coerceAtMost(dimensions[2] - 1)
                    val values: DoubleArray = vectors[posX][posY][posZ].values
                    gradientVec.values = (values)
                    // Store the two values to an array
                    zBases[zOff] = getDotSingle(point, offset)
                }
                // Interpolate between the two z corners at the z value
                yBases[yOff] = smoothStep(zBases[0], zBases[1], phase.values[2])
            }
            // Interpolate between the two y points at the y value
            xBases[xOff] = smoothStep(yBases[0], yBases[1], phase.values[1])
        }
        // Interpolate between the two x points at the x value
        return smoothStep(xBases[0], xBases[1], phase.values[0])
        // hold up is this code unreadable (oh no)
        // my whitespace looks goofy
    }

    companion object {
        // Uses 5th order smoothstep to blend between 2 values at a phase
        private fun smoothStep(first: Double, second: Double, phase: Double): Double {
            val func: Double = 6 * phase.pow(5.0) - 15 * phase.pow(4.0) + 10 * phase.pow(3.0)
            return first + func * (second - first)
        }
    }
}