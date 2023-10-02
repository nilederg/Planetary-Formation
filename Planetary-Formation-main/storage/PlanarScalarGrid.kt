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

/**
 * 64 by 64 grid of integer values
 *
 * Imperfect but accurate, storing 4096 integers in 4953 bytes
 *
 * More accurate with smoother surfaces
 */
@OptIn(ExperimentalUnsignedTypes::class)
class PlanarScalarGrid internal constructor(data: Array<LongArray>) : PlanarScalarData {
    // Repeating layers of higher resolution but smaller range hone in on the precise value for each point
    // Floating-point exponents apply to 4x4 sectors of their constituent values, ensuring infinite range
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
        intOffsets = IntArray(4*4)
        shortOffsets = ShortArray(16*16)
        byteOffsets = ByteArray(64*64)
        intExponent = 0u
        shortExponents = UByteArray(4*4)
        byteExponents = UByteArray(16*16)
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

    /*
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
        throw IllegalStateException("Unreachable state")
    }
    private fun byteOffset(point: Index): Long {
        return (2.0.pow(byteExponents[(point / 4).arrIndex(16)].toInt())).toLong() * byteSummer(point)
    }

    override fun getPoint(point: Vector2): Long {
        val index = Index.fromVector(point, 64)
        return byteOffset(index) + shortOffset(index) + intOffset(index) + mean
    }

    // Setting

    /**
     * Set all points in the grid to the specified longs
     * @see Index
     * TODO: Illegible code?????
     */
    fun set(points: LongArray) {
        val shortMean = LongArray(16*16)
        val intMean = LongArray(4*4)

        // Calculate means of short sectors (only consider core 4 points, as others are offsets)
        Index.iterate(16) { shortSector: Index ->
            shortMean[shortSector.arrIndex(16)] = Index.average(2) { offset: Index ->
                val position = shortSector * 4 + 1 + offset
                return@average points[position.arrIndex(64)]
            }
        }

        // Calculate means of int sectors
        Index.iterate(4) { intSector: Index ->
            intMean[intSector.arrIndex(4)] = Index.average(4) { offset: Index ->
                val position = intSector * 4 + offset
                return@average shortMean[position.arrIndex(16)]
            }
        }

        // Calculate total mean
        mean = Index.average(4) { intSector: Index ->
            return@average intMean[intSector.arrIndex(4)]
        }

        // Calculate int sector exponent
        var maxOffset = 0L
        Index.iterate(4) { intSector: Index ->
            val offset = mean - intMean[intSector.arrIndex(4)]
            intMean[intSector.arrIndex(4)] = offset
            if (offset.absoluteValue > maxOffset)
                maxOffset = offset.absoluteValue
        }
        intExponent = (32 - maxOffset.countLeadingZeroBits()).coerceIn(0, 255).toUByte() // Dark magic, do not touch!
        // Calculate int sector offsets
        Index.iterate(4) { intSector: Index ->
            intOffsets[intSector.arrIndex(4)] = (intMean[intSector.arrIndex(4)] shr intExponent.toInt()).toInt()
        }

        // Calculate short sector exponent
        Index.iterate(4) { intSector: Index ->
            var maxOffset = 0L
            Index.iterate(4) { shortSector: Index ->
                val position = intSector * 4 + shortSector
                val offset = mean + intMean[intSector.arrIndex(4)] - shortMean[position.arrIndex(16)]
                shortMean[position.arrIndex(16)] = offset
                if (offset.absoluteValue > maxOffset)
                    maxOffset = offset.absoluteValue
            }
            shortExponents[intSector.arrIndex(4)] = (48 - maxOffset.countLeadingZeroBits()).coerceIn(0, 255).toUByte()
        }

        // Calculate short sector offsets
        Index.iterate(16) { shortSector: Index ->
            shortOffsets[shortSector.arrIndex(16)] = (shortMean[shortSector.arrIndex(16)] shr shortExponents[(shortSector / 4).arrIndex(4)].toInt()).toShort()
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
        val quadrantData: Array<LongArray> = Array(256) { LongArray(256) }
        for (i in 0 until 128) {
            for (j in 0 until 128) {
                val value: Long = getPoint(Vector2(doubleArrayOf((i + xStart).toDouble(), (j + yStart).toDouble())))
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

    /*public override fun exportTriangles(projector: Function<Vector3, Vector3>): Array<TriangleFace> {
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
    }*/

    companion object {
        const val MEMORY_USAGE: Int = 5033
    }
}