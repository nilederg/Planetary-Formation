package storage

import storage.STL.StlFile
import storage.ScalarQuadTree.ApplicationZone
import storage.ScalarQuadTree.LocalMutator
import storage.positionals.GeoCoord
import storage.positionals.Vector2
import storage.positionals.Vector3
import java.io.IOException
import java.util.function.BiFunction
import kotlin.math.pow

class ScalarSphere constructor(resolution: Int, maxRAM: Long) {
    private val faces // R(ight) L F B U D
            : Array<ScalarQuadTree>
    val memory: Long

    // Intentional temp function
    @Throws(IOException::class)
    fun exportFaceSTL(filename: String) {
        val file = StlFile(filename, " m", "Planetary Formation Testing STL Export")
        faces[0].exportCode({ vec: Vector3 -> vec }, file)
        file.close()
    }

    init {
        memory = (4.0.pow(resolution.toDouble()).toLong()) * PlanarScalarGrid.MEMORY_USAGE * 6
        if (memory > maxRAM) throw OutOfMemoryError("Insufficient available memory to initialize " + memoryString() + " sphere.")
        println("Building a sphere - Approximate memory allocation: " + memoryString())
        println(memory)

        faces = Array (6) { i ->
            println("Initializing face $i")
            return@Array ScalarQuadTree(null, resolution)
        }
        println(memoryString() + " Sphere initialized")
    }

    private fun memoryString(): String {
        val precision = 1
        if (memory > 1099511627776L) return round(memory / 1099511627776.0, precision) + " TiB"
        if (memory > 1073741824L) return round(memory / 1073741824.0, precision) + " GiB"
        if (memory > 1048576L) return round(memory / 1048576.0, precision) + " MiB"
        if (memory > 1024L) return round(memory / 1024.0, precision) + " KiB"
        return "$memory B"
    }

    // Returns the face covered by the provided latitude and longitude
    private fun getFace(point: GeoCoord): ScalarQuadTree {
        val location: Vector3 = Vector3.fromSphericalCoordinates(point)
        when (location.largestComponent()) {
            0 -> { return if (location.getX() >= 0) faces[0] else faces[1] }
            1 -> { return if (location.getY() >= 0) faces[0] else faces[1] }
            2 -> { return if (location.getZ() >= 0) faces[0] else faces[1] }
        }
        throw IllegalArgumentException("Case " + location.largestComponent() + " is not a valid dimension index.")
    }

    private fun getPointOnFace(cubicVector: Vector3): Vector2 {
        cubicVector.normalizeCube()
        when (cubicVector.largestComponent()) {
            0 -> { return Vector2(doubleArrayOf(cubicVector.getY() / 2 + 0.5, cubicVector.getZ() / 2 + 0.5)) }
            1 -> { return Vector2(doubleArrayOf(cubicVector.getX() / 2 + 0.5, cubicVector.getZ() / 2 + 0.5)) }
            2 -> { return Vector2(doubleArrayOf(cubicVector.getX() / 2 + 0.5, cubicVector.getY() / 2 + 0.5)) }
        }
        throw IllegalArgumentException("Case " + cubicVector.largestComponent() + " is not a valid dimension index.")
    }

    // Gets the value at any given point, with a GeoCoord
    fun getPoint(point: GeoCoord): Long {
        val cubicVector: Vector3 = Vector3.fromSphericalCoordinates(point)
        return getPoint(cubicVector)
    }

    // Gets the value at any given point, with a Vector3
    private fun getPoint(cubicVector: Vector3): Long {
        val largestComponent: Int = cubicVector.largestComponent()
        val largestValue: Double = cubicVector.values[largestComponent]
        val tree: ScalarQuadTree = if (largestValue >= 0) faces[largestComponent * 2] else faces[largestComponent * 2 + 1]
        return tree.getPoint(getPointOnFace(cubicVector))
    }

    fun interface SphereMutation {
        fun mutate(point: Vector3, value: Long): Long
    }

    fun interface SphereApplicationZone {
        // Point is point on sphere surface, range is radians distance from point
        // Returns true if anything within range of point is in the "zone", false otherwise
        fun checkWithin(point: Vector3, range: Double): Boolean
    }

    // Evaluates a lambda, mutating the value at every point on the sphere
    // Lambda takes in its own geographic position and outputs its new value
    // Efficiency improved by only running on necessary regions with ApplicationZone
    fun mutateSphereLocal(operation: SphereMutation, zone: SphereApplicationZone) {
        for (i in 0..5) {
            val finalI: Int = i
            println("Mutating face $i")
            // 1 radian is actually very close to the maximum distance from center here, and it's easier on the computer
            if (!zone.checkWithin(faceCenter(i), 1.0)) continue
            // Only mutate if within zone
            val localOperation = LocalMutator { point: Vector2, value: Long -> operation.mutate(placeFace(point, finalI), value) }
            val localZone = ApplicationZone { point: Vector2, range: Double -> zone.checkWithin(placeFace(point, finalI), range) }
            faces[i].mutateLocal(localOperation, localZone, 1.0, Vector2(doubleArrayOf(0.0, 0.0)))
        }
    }

    // Input 1 is this, input 2 is inSphere
    fun biMutate(operator: BiFunction<Long, Long, Long>, inSphere: ScalarSphere) {
        mutateSphereLocal({ point: Vector3, value: Long -> operator.apply(value, inSphere.getPoint(point)) }, { _: Vector3, _: Double -> true })
    }

    fun exportPng(fileSize: Long) {
        // TODO implement this feature
        //      export a png with width:height ratio 2:1
        //      in equidistant projection
        //      implement soon so testing can occur
    }

    fun exportStl() {
        // Header and total tri count
    }

    companion object {
        private fun round(value: Double, precision: Int): String {
            return value.toLong().toString() + "." + (((value % 1) * (10.0.pow(precision)).toInt() + 0.5).toInt())
        }

        private fun faceCenter(face: Int): Vector3 {
            when (face) {
                0 -> { return Vector3(doubleArrayOf(-1.0, 0.0, 0.0)) }
                1 -> { return Vector3(doubleArrayOf( 1.0, 0.0, 0.0)) }
                2 -> { return Vector3(doubleArrayOf(0.0, -1.0, 0.0)) }
                3 -> { return Vector3(doubleArrayOf(0.0,  1.0, 0.0)) }
                4 -> { return Vector3(doubleArrayOf(0.0, 0.0, -1.0)) }
                5 -> { return Vector3(doubleArrayOf(0.0, 0.0,  1.0)) }
            }
            throw IllegalArgumentException("Face must be an integer between 0 and 6.")
        }

        // places the point on a face, and outputs where that point on that face is in the 3d cube
        private fun placeFace(point: Vector2, face: Int): Vector3 {
            var location: Vector3? = null
            when (face) {
                0 -> location = Vector3(doubleArrayOf(-1.0, point.getX() * 2 - 1, point.getY() * 2 - 1))
                1 -> location = Vector3(doubleArrayOf( 1.0, point.getX() * 2 - 1, point.getY() * 2 - 1))
                2 -> location = Vector3(doubleArrayOf(point.getX() * 2 - 1, -1.0, point.getY() * 2 - 1))
                3 -> location = Vector3(doubleArrayOf(point.getX() * 2 - 1,  1.0, point.getY() * 2 - 1))
                4 -> location = Vector3(doubleArrayOf(point.getX() * 2 - 1, point.getY() * 2 - 1, -1.0))
                5 -> location = Vector3(doubleArrayOf(point.getX() * 2 - 1, point.getY() * 2 - 1,  1.0))
            }
            location!!.normalize()
            return location
        }
    }
}