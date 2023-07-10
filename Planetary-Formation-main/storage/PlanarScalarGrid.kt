package storage

import storage.STL.TriangleFace
import storage.ScalarQuadTree.LocalMutator
import storage.positionals.Index
import storage.positionals.Vector2
import storage.positionals.Vector3
import java.awt.geom.IllegalPathStateException
import java.util.function.Function
import kotlin.math.absoluteValue
import kotlin.math.pow

@OptIn(ExperimentalUnsignedTypes::class)
class PlanarScalarGrid internal constructor(data: Array<LongArray>) : PlanarScalarData {
    // Repeating layers of higher resolution but smaller range hone in on the precise value for each point
    private var mean: Long                 // 1x1   8B
    private var intExponent: UByte         // 1x1   1B
    private val intOffsets: IntArray       // 4x4   64B + 12B overhead
    private val shortExponents: UByteArray // 4x4   16B + 12B overhead
    private val shortOffsets: ShortArray   // 16x16 512B + 12B overhead
    private val byteExponents: UByteArray  // 16x16 256B + 12B overhead
    private val byteOffsets: ByteArray     // 64x64 4096B + 12B overhead
    //                                              4953B + 60B+20B overhead = 5033B
    // Point = mean + 2^intExponent * intOffset + 2^shortExponent * shortOffset + 2^byteExponent *
    //      : byteOffset
    //      : byteOffset + adjacentByteOffset
    //      : byteOffset + adjacentByteOffset + otherAdjacentByteOffset

    init {
        if (data.size != 64) throw ArrayIndexOutOfBoundsException("Data must be a 64 by 64 array.")
        if (data[0].size != 64) throw ArrayIndexOutOfBoundsException("Data must be a 64 by 64 array.")

        mean = 0L
        intOffsets = IntArray(16)
        shortOffsets = ShortArray(256)
        byteOffsets = ByteArray(4096)
        intExponent = 0u
        shortExponents = UByteArray(16)
        byteExponents = UByteArray(256)
    }

    // Getting

    // Get one of the offsets at the specified index (size=64)
    private fun intOffset(point: Index): Long {
        return (2.0.pow(intExponent.toInt())).toLong() * intOffsets[(point / 16).arrIndex(4)]
    }

    private fun shortOffset(point: Index): Long {
        return ((2.0.pow(shortExponents[(point / 16).arrIndex(4)].toInt())).toLong()
                * shortOffsets[(point / 4).arrIndex(16)])
    }

    /**
     * VVVV
     * >##<
     * >##<
     * ^^^^
     */
    private fun byteSummer(point: Index): Short {
        val sectorPos = point % 4
        val offsetHere = byteOffsets[point.arrIndex(64)]
        if ((sectorPos.x == 1 || sectorPos.x == 2) &&
            (sectorPos.y == 1 || sectorPos.y == 2))
            return offsetHere.toShort()
        if (sectorPos.y == 0)
            return (offsetHere + byteSummer(Index(point.x, 1))).toShort()
        if (sectorPos.y == 3)
            return (offsetHere + byteSummer(Index(point.x, 2))).toShort()
        if (sectorPos.x == 0)
            return (offsetHere + byteSummer(Index(1, point.y))).toShort()
        if (sectorPos.x == 3)
            return (offsetHere + byteSummer(Index(2, point.y))).toShort()
        throw IllegalPathStateException("Unreachable state")
    }
    private fun byteOffset(point: Index): Long {
        return (2.0.pow(byteExponents[(point / 4).arrIndex(16)].toInt())).toLong() * byteSummer(point)
    }

    private fun getFixedPoint(point: Vector2): Long {
        val index = Index.fromVector(point, 64)
        return byteOffset(index) + shortOffset(index) + intOffset(index) + mean
    }

    override fun getPoint(point: Vector2): Double {
        return getFixedPoint(point).toDouble()
    }

    // Setting

    fun set(points: LongArray) {
        Index.iterate(4) { intSector: Index ->
            var maxExponent: UByte = 0u
            Index.iterate(4) { shortSector: Index ->
                var sum = 0.0
                Index.iterate(2) { offset: Index ->
                    val position = intSector * 16 + shortSector * 4 + offset + 1
                    sum += points[position.arrIndex(64)]
                }
                sum /= 4
                var exponent: UByte = 0u
                val value = sum
                // Bit-manipulate the exponent out of the double
                exponent = (value.absoluteValue.toRawBits() shr 52).coerceAtMost(UByte.MAX_VALUE.toLong()).toUByte()
                if (exponent > maxExponent)
                    maxExponent = exponent
            }
            shortExponents[intSector.arrIndex(4)] = maxExponent
        }
    }

    public override fun mutateLocal(operation: LocalMutator) {
        val newVals = LongArray(4096)
        Index.iterate(64) {point: Index ->
            newVals[point.arrIndex(64)] = operation.mutate(point.toVector(64), byteOffset(point) + shortOffset(point) + intOffset(point) + mean)
        }
        set(newVals)
    }

    public override fun getQuadrant(x: Boolean, y: Boolean): PlanarScalarGrid {
        val xStart: Int = if (x) 0 else 32
        val yStart: Int = if (y) 0 else 32
        val quadrantData: Array<DoubleArray> = Array(256) { LongArray(256) }
        for (i in 0 until 128) {
            for (j in 0 until 128) {
                val value: Double = getPoint(Vector2(doubleArrayOf((i + xStart).toDouble(), (j + yStart).toDouble()))).toDouble()
                // @formatter:off
                quadrantData[i * 2]    [j * 2]     = value
                quadrantData[i * 2]    [j * 2 + 1] = value
                quadrantData[i * 2 + 1][j * 2]     = value
                quadrantData[i * 2 + 1][j * 2 + 1] = value
                // @formatter:on
            }
        }
        return PlanarScalarGrid(quadrantData)
    }

    public override fun exportTriangles(projector: Function<Vector3, Vector3>): Array<TriangleFace> {
        val triangles: Array<TriangleFace?> = arrayOfNulls<TriangleFace>(255 * 255 * 2)
        for (i in 0 until 255) {
            for (j in 0 until 255) {
                val x: Double = i / 256.0
                val y: Double = j / 256.0
                val unit: Double = 1 / 256.0
                val vertices: Array<Vector3> = arrayOf(
                        projector.apply(Vector3.fromCoords(x + unit, y, getPoint(Vector2.fromCoords(x + unit, y)))),
                        projector.apply(Vector3.fromCoords(x, y + unit, getPoint(Vector2.fromCoords(x, y + unit)))),
                        projector.apply(Vector3.fromCoords(x, y, getPoint(Vector2.fromCoords(x, y))))
                )
                triangles[(i * 255 + j) * 2] = TriangleFace.fromOrientingPoint(Vector3.zero, false, vertices)
                vertices[2] = projector.apply(Vector3.fromCoords(x + unit, y + unit, getPoint(Vector2.fromCoords(x + unit, y + unit))))
                triangles[(i * 255 + j) * 2 + 1] = TriangleFace.fromOrientingPoint(Vector3.zero, false, vertices)
            }
        }
        return triangles.requireNoNulls()
    }

    companion object {
        const val MEMORY_USAGE: Int = 5033
    }
}