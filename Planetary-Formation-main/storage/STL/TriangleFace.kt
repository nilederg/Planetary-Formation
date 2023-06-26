package storage.STL

import storage.positionals.Vector3

class TriangleFace internal constructor(normal: Vector3, vertices: Array<Vector3>) {
    private val normal: Vector3
    private val vertices: Array<Vector3>

    init {
        if (vertices.size != 3) throw IllegalArgumentException("TriangleFace must have 3 vertices.")
        this.normal = normal
        this.vertices = vertices
    }

    fun exportCode(): ByteArray {
        val byteArray = ByteArray(50)

        // Array of all values which will be stored
        val doubleArray: Array<DoubleArray> = Array(4) { DoubleArray(3) }
        doubleArray[0] = normal.values
        for (i in 0..2) doubleArray[i + 1] = vertices[i].values

        // Convert values into floats and store in byteArray
        for (i in 0..3) {
            for (j in 0..2) {
                val value: Float = doubleArray[i][j].toFloat()
                val bytes: ByteArray = floatToByteArray(value)
                assert(bytes.size == 4)
                for (k in 0..3) byteArray[4 * (3 * i + j) + k] = bytes.get(k)
            }
        }

        // Attribute Byte Count is always 0
        byteArray[48] = 0
        byteArray[49] = 0
        return byteArray
    }

    companion object {
        fun fromOrientingPoint(orientingPoint: Vector3, facingTowards: Boolean, vertices: Array<Vector3>): TriangleFace {
            if (vertices.size != 3) throw IllegalArgumentException("TriangleFace must have 3 vertices.")
            val AB: Vector3 = vertices[0].getRel(vertices[1])
            val AC: Vector3 = vertices[0].getRel(vertices[2])
            val normal: Vector3 = Vector3.crossProduct(AB, AC)
            val product: Double = normal.dotProduct(vertices[0].getRel(orientingPoint))
            if ((product > 0) xor facingTowards) {
                normal.scale(-1.0)
                val vertexA: Vector3 = vertices[0].clone()
                vertices[0] = vertices[1].clone()
                vertices[1] = vertexA
            }
            normal.normalize()
            return TriangleFace(normal, vertices)
        }

        private fun floatToByteArray(value: Float): ByteArray {
            val intBits: Int = java.lang.Float.floatToIntBits(value)
            return byteArrayOf((intBits shr 24).toByte(), (intBits shr 16).toByte(), (intBits shr 8).toByte(), (intBits).toByte())
        }
    }
}