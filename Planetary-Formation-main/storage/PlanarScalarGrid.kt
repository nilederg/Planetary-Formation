package storage

import storage.STL.TriangleFace
import storage.ScalarQuadTree.LocalMutator
import storage.positionals.Index
import storage.positionals.Vector2
import storage.positionals.Vector3
import java.awt.geom.IllegalPathStateException
import java.util.function.BiFunction
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
class PlanarScalarGrid internal constructor(data: LongArray?) : PlanarScalarData {
    // Repeating layers of higher resolution but smaller range hone in on the precise value for each point
    // Floating-point exponents apply to 4x4 sectors of their constituent values, ensuring infinite range
    private var mean: Long                 // 1x1   8B
    private var intExponent: UByte         // 1x1   1B
    private val intOffsets: IntArray       // 4x4   64B + 20B overhead
    private val shortExponents: UByteArray // 4x4   16B + 20B overhead
    private val shortOffsets: ShortArray   // 16x16 512B + 20B overhead
    private val byteExponents: UByteArray  // 16x16 256B + 20B overhead
    private val byteOffsets: ByteArray     // 64x64 4096B + 20B overhead
    //                                              4953B + 100B+20B overhead = 5073B
    // Point = mean + 2^intExponent * intOffset + 2^shortExponent * shortOffset + 2^byteExponent *
    //      : byteOffset
    //      : byteOffset + adjacentByteOffset
    //      : byteOffset + adjacentByteOffset + otherAdjacentByteOffset

    init {
        mean = 0L
        intOffsets = IntArray(4*4)
        shortOffsets = ShortArray(16*16)
        byteOffsets = ByteArray(64*64)
        intExponent = 0u
        shortExponents = UByteArray(4*4)
        byteExponents = UByteArray(16*16)

        if (data != null) {
            if (data.size != (64 * 64)) throw ArrayIndexOutOfBoundsException("Data must be a 64 by 64 (one dimensional) array.")
            set(data)
        }
    }

    // Getting

    // Get one of the offsets at the specified index (size=4)
    private fun intOffset(point: Index): Long {
        return intOffsets[point.arrIndex(4)].toLong() shr intExponent.toInt()
    }

    private fun shortOffset(point: Index): Long {
        return shortOffsets[point.arrIndex(16)].toLong() shr shortExponents[(point / 4).arrIndex(4)].toInt()
    }

    /*
     * VVVV
     * >##<
     * >##<
     * ^^^^
     */
    /*private*/ fun byteSummer(point: Index): Short {
        val sectorPos = point % 4
        val sectorBase = point - sectorPos
        //println(sectorPos.toString())
        val offsetHere = byteOffsets[point.arrIndex(64)].toShort()
        if (sectorPos.y == 0)
            return (offsetHere + byteSummer(Index(point.x, sectorBase.y + 1))).toShort()
        if (sectorPos.y == 3)
            return (offsetHere + byteSummer(Index(point.x, sectorBase.y + 2))).toShort()
        if (sectorPos.x == 0)
            return (offsetHere + byteSummer(Index(sectorBase.x + 1, point.y))).toShort()
        if (sectorPos.x == 3)
            return (offsetHere + byteSummer(Index(sectorBase.x + 2, point.y))).toShort()
        return offsetHere
    }
    /*private*/ fun byteOffset(point: Index): Long {
        return byteSummer(point).toLong() shr byteExponents[(point / 4).arrIndex(16)].toInt()
    }

    fun getPoint(point: Index): Long {
        return byteOffset(point) + shortOffset(point / 4) + intOffset(point / 16) + mean
    }

    override fun getPoint(point: Vector2): Long {
        val index = Index.fromVector(point, 64)
        return getPoint(index)
    }

    // Setting

    /**
     * Set all points in the grid to the specified longs
     * @see Index
     * TODO: Illegible code?????
     */
    fun set(points: LongArray) {
        val byteValue = LongArray(64*64)
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
            val offset = intMean[intSector.arrIndex(4)] - mean
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
                val offset = shortMean[position.arrIndex(16)] - intOffset(intSector) - mean
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

        // bytesummer but for bytevalue instead
        fun calculateValue (shortSector: Index, byteSector: Index): Long {
            val bytePosition = shortSector * 4 + byteSector
            if (byteSector.y == 0) {
                return calculateValue(shortSector, byteSector + Index(0, 1)) + byteValue[bytePosition.arrIndex(64)]
            }
            if (byteSector.y == 3) {
                return calculateValue(shortSector, byteSector + Index(0, -1)) + byteValue[bytePosition.arrIndex(64)]
            }
            if (byteSector.x == 0) {
                return calculateValue(shortSector, byteSector + Index(1, 0)) + byteValue[bytePosition.arrIndex(64)]
            }
            if (byteSector.x == 3) {
                return calculateValue(shortSector, byteSector + Index(-1, 0)) + byteValue[bytePosition.arrIndex(64)]
            }
            return mean + intOffset(shortSector / 4) + shortOffset(shortSector) + byteValue[bytePosition.arrIndex(64)]
        }

        // Calculate byte offsets
        // Will the compiler optimize this by removing the loop?
        Index.iterate(16) { shortSector: Index ->
            // Calculate core 4 offsets
            Index.iterate(2) { byteSector: Index ->
                val bytePosition = shortSector * 4 + 1 + byteSector
                byteValue[bytePosition.arrIndex(64)] = points[bytePosition.arrIndex(64)] - shortOffset(shortSector) - intOffset(shortSector / 4) - mean
            }
            // Calculate left 2 offsets
            for (y in 1..2) {
                val byteSector = Index(0, y)
                val bytePosition = shortSector * 4 + byteSector
                byteValue[bytePosition.arrIndex(64)] = points[bytePosition.arrIndex(64)] - calculateValue(shortSector, byteSector + Index(1, 0))
            }
            // Calculate right 2 offsets
            for (y in 1..2) {
                val byteSector = Index(3, y)
                val bytePosition = shortSector * 4 + byteSector
                byteValue[bytePosition.arrIndex(64)] = points[bytePosition.arrIndex(64)] - calculateValue(shortSector, byteSector + Index(-1, 0))
            }
            // Calculate bottom 4 offsets
            for (x in 0..3) {
                val byteSector = Index(x, 0)
                val bytePosition = shortSector * 4 + byteSector
                byteValue[bytePosition.arrIndex(64)] = points[bytePosition.arrIndex(64)] - calculateValue(shortSector, byteSector + Index(0, 1))
            }
            // Calculate top 4 offsets
            for (x in 0..3) {
                val byteSector = Index(x, 3)
                val bytePosition = shortSector * 4 + byteSector
                byteValue[bytePosition.arrIndex(64)] = points[bytePosition.arrIndex(64)] - calculateValue(shortSector, byteSector + Index(0, -1))
            }

            // Calculate exponent
            var maxOffset = 0L
            Index.iterate(4) { bytePosition: Index ->
                if (byteValue[bytePosition.arrIndex(4)].absoluteValue > maxOffset) {
                    maxOffset = byteValue[bytePosition.arrIndex(4)].absoluteValue
                }
            }
            val byteExponent = (56 - maxOffset.countLeadingZeroBits()).coerceIn(0, 255).toUByte()
            byteExponents[shortSector.arrIndex(16)] = byteExponent

            // Calculate final offsets
            // This could likely be made slightly more accurate - idk how though
            Index.iterate(4) { bytePosition: Index ->
                val position = shortSector * 4 + bytePosition
                byteOffsets[position.arrIndex(64)] = (byteValue[position.arrIndex(64)] shr byteExponent.toInt()).toByte()
            }
        }
    }

    public override fun mutateLocal(operation: LocalMutator) {
        val newVals = LongArray(4096)
        Index.iterate(64) {point: Index ->
            newVals[point.arrIndex(64)] = operation.mutate(point.toVector(64), byteOffset(point) + shortOffset(point / 4) + intOffset(point / 16) + mean)
        }
        set(newVals)
    }

    public override fun getQuadrant(x: Boolean, y: Boolean): PlanarScalarGrid {
        val xStart: Int = if (x) 0 else 32
        val yStart: Int = if (y) 0 else 32
        val quadrantData: LongArray = LongArray(64 * 64)
        for (i in 0 until 32) {
            for (j in 0 until 32) {
                val value: Long = getPoint(Vector2(doubleArrayOf((i + xStart).toDouble(), (j + yStart).toDouble())))
                // @formatter:off
                quadrantData[Index(i * 2,     j * 2).arrIndex(64)]     = value
                quadrantData[Index(i * 2,     j * 2 + 1).arrIndex(64)] = value
                quadrantData[Index(i * 2 + 1, j * 2).arrIndex(64)]     = value
                quadrantData[Index(i * 2 + 1, j * 2 + 1).arrIndex(64)] = value
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

    public fun print() {
        println("Values")
        for (i in 0..31) {
            for (j in 0..63) {
                print(getPoint(Index(i, j)))
                print(" ")
            }
            println()
        }

        println()
        println()
        println("byteOffsets")

        for (i in 0..31) {
            for (j in 0..63) {
                print(byteOffsets[Index(i, j).arrIndex(64)])
                print(" ")
            }
            println()
        }

        println()
        println()
        println("shortOffsets")

        for (i in 0..15) {
            for (j in 0..15) {
                print(shortOffsets[Index(i, j).arrIndex(16)])
                print(" ")
            }
            println()
        }

        println()
        println()
        println("intOffsets")

        for (i in 0..3) {
            for (j in 0..3) {
                print(intOffsets[Index(i, j).arrIndex(4)])
                print(" ")
            }
            println()
        }

        println()
        println()
        println("byteExponents")

        for (i in 0..15) {
            for (j in 0..15) {
                print(byteExponents[Index(i, j).arrIndex(16)])
                print(" ")
            }
            println()
        }

        println()
        println()
        println("shortExponents")

        for (i in 0..3) {
            for (j in 0..3) {
                print(shortExponents[Index(i, j).arrIndex(4)])
                print(" ")
            }
            println()
        }

        println()
        println()
        println("intExponents")

        println(intExponent)
        println()
        println("mean")
        println(mean)
    }

    companion object {
        const val MEMORY_USAGE: Int = 5073
    }
}