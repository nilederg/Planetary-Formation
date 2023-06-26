package storage

import storage.positionals.Vector2
import storage.positionals.Vector3
import storage.STL.TriangleFace
import storage.ScalarQuadTree.LocalMutator
import java.util.function.Function

class PlanarScalarGrid internal constructor(data: Array<DoubleArray>) : PlanarScalarData {
    // Split implementation cuts space required nearly in half without losing significant precision
    // Total 260 KiB
    private val mean // 4 KiB
            : Array<DoubleArray>
    private val offset // 256 KiB
            : Array<FloatArray>

    init {
        if (data.size != 256) throw ArrayIndexOutOfBoundsException("Data must be a 256 by 256 array.")
        if (data[0].size != 256) throw ArrayIndexOutOfBoundsException("Data must be a 256 by 256 array.")
        mean = Array (16) { DoubleArray(16) }
        offset = Array (256) { FloatArray(256) }

        // Loop through all 256 sectors
        for (i in 0..15) {
            for (j in 0..15) {
                // Get average in sector
                var sum: Double = 0.0
                for (k in 0..15) {
                    for (l in 0..15) {
                        sum += data[i * 16 + k][j * 16 + l].toDouble()
                    }
                }
                sum /= 256.0
                mean[i][j] = sum
                // Store offset for each pixel in sector from sector average
                for (k in 0..15) {
                    for (l in 0..15) {
                        offset[i * 16 + k][j * 16 + l] = (data[i * 16 + k][j * 16 + l] - sum).toFloat()
                    }
                }
            }
        }
    }

    public override fun getPoint(point: Vector2): Double {
        val x: Int = (point.getX() * 256.0).toInt().coerceAtMost(255)
        val y: Int = (point.getY() * 256.0).toInt().coerceAtMost(255)
        return mean[x / 16][y / 16] + offset[x][y]
    }

    public override fun mutateLocal(operation: LocalMutator) {
        for (i in 0..15) {
            for (j in 0..15) {
                // Temporary array with excessive precision before compression into f32
                val sector: Array<DoubleArray> = Array(16) { DoubleArray(16) }
                for (k in 0..15) {
                    for (l in 0..15) {
                        val position: Vector2 = Vector2(doubleArrayOf((i * 16 + k) / 256.0, (j * 16 + l) / 256.0))
                        val value: Double = getPoint(Vector2(doubleArrayOf(i.toDouble(), j.toDouble()))).toDouble()
                        sector[k][l] = operation.mutate(position, value)
                    }
                }
                val average: Double = sectorAverage(sector)
                mean[i][j] = average
                // Compress into float32 and store to offset
                for (k in 0..15) {
                    for (l in 0..15) {
                        offset[i * 16 + k][j * 16 + l] = (sector[k][l] - average).toFloat()
                    }
                }
            }
        }
    }

    public override fun getQuadrant(x: Boolean, y: Boolean): PlanarScalarGrid {
        val xStart: Int = if (x) 0 else 128
        val yStart: Int = if (y) 0 else 128
        val quadrantData: Array<DoubleArray> = Array(256) { DoubleArray(256) }
        for (i in 0..127) {
            for (j in 0..127) {
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
        for (i in 0..254) {
            for (j in 0..254) {
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
        const val MEMORY_USAGE: Int = 266240

        private fun sectorAverage(sector: Array<DoubleArray>): Double {
            var sum: Double = 0.0
            for (i in 0..15) {
                for (j in 0..15) {
                    sum += sector[i][j]
                }
            }
            return sum / 256
        }
    }
}