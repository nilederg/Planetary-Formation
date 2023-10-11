package storage

import storage.STL.StlFile
import storage.positionals.Vector2
import storage.positionals.Vector3
import java.io.IOException
import java.util.function.Function

// A recursive field that stores a grid of scalars
class ScalarQuadTree {
    private val parent: ScalarQuadTree?
    private var children: Array<Array<ScalarQuadTree>>?
    private var data: PlanarScalarData?
    private var leafNode: Boolean

    // Universal node constructor
    internal constructor(parent: ScalarQuadTree?, remainingLevels: Int) {
        if (remainingLevels == 0) {
            this.parent = parent
            children = null
            leafNode = true
            data = PlanarScalarGrid(LongArray(64 * 64))
            return
        }
        this.parent = parent
        children = Array(2) { Array(2) { ScalarQuadTree(this, remainingLevels - 1) } }
        leafNode = false
        data = null
    }

    // Specialized leaf node constructor
    // A node can still change between branch and leaf after construction
    // TODO do that
    private constructor(parent: ScalarQuadTree?, data: PlanarScalarData?) {
        this.parent = parent
        children = null
        leafNode = true
        this.data = data
    }

    // Returns the child node that would contain those coordinates
    private fun relevantChild(point: Vector2): ScalarQuadTree? {
        // children[0][.] if x < 0.5, children[1][.] otherwise. Same for y.
        return children?.get(if ((point.getX() < 0.5)) 0 else 1)?.get(if ((point.getY() < 0.5)) 0 else 1)
    }

    private fun getLeafNode(point: Vector2): ScalarQuadTree {
        if (leafNode) return this
        val leafPoint: Vector2 = point.clone()
        leafPoint.mutateWithLambda { x: Double -> (2 * x % 1) }
        return relevantChild(point)!!.getLeafNode(point)
    }

    fun getPoint(point: Vector2): Long {
        val terminalNode: ScalarQuadTree = getLeafNode(point)
        return terminalNode.data!!.getPoint(point)
    }

    private fun split() {
        // You can't iterate on a boolean - sorry!
        children = arrayOf(arrayOf(
                ScalarQuadTree(this, data!!.getQuadrant(false, false)),
                ScalarQuadTree(this, data!!.getQuadrant(false, true))), arrayOf(
                ScalarQuadTree(this, data!!.getQuadrant(true,  false)),
                ScalarQuadTree(this, data!!.getQuadrant(true,  true))))
        data = null
        leafNode = false
    }

    fun interface LocalMutator {
        fun mutate(point: Vector2, value: Long): Long
    }

    fun interface ApplicationZone {
        // Point is point on sphere surface, range is radians distance from point
        // Returns true if anything within range of point is in the "zone", false otherwise
        fun checkWithin(point: Vector2, range: Double): Boolean
    }

    // Operation is applied to every point within zone
    fun mutateLocal(operation: LocalMutator, zone: ApplicationZone, size: Double, position: Vector2) {
        // Don't bother with it if not in the zone
        if (!zone.checkWithin(Vector2.sum(position, Vector2(doubleArrayOf(size / 2, size / 2))), size)) {
            println(position.getX().toString() + " " + position.getY() + ", " + "Skipped at " + size)
            return
        }
        if (leafNode) {
            data!!.mutateLocal { point: Vector2, value: Long ->
                val x: Double = point.getX() * size + position.getX()
                val y: Double = point.getY() * size + position.getY()
                if (x > 1 || y > 1) throw IllegalArgumentException("x or y greater than 1")
                operation.mutate(Vector2(doubleArrayOf(x, y)), value)
            }
        } else {
            for (i in 0..1) {
                for (j in 0..1) {
                    val offset = Vector2(doubleArrayOf(size * i / 2, size * j / 2))
                    children!![i][j].mutateLocal(operation, zone, size / 2, Vector2.sum(position, offset))
                }
            }
        }
    }

    /*@Throws(IOException::class)
    fun exportCode(projector: Function<Vector3, Vector3>, file: StlFile) {
        if (leafNode) {
            file.writeTriangles(data!!.exportTriangles(projector))
        } else {
            children!![0][0].exportCode(projector, file)
            children!![0][1].exportCode(projector, file)
            children!![1][0].exportCode(projector, file)
            children!![1][1].exportCode(projector, file)
        }
    }*/
}