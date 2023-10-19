package storage

import storage.positionals.Vector2
import storage.positionals.Vector3
import storage.STL.TriangleFace
import storage.ScalarQuadTree.LocalMutator
import java.util.function.Function

interface PlanarScalarData {
    fun getPoint(point: Vector2): Long
    fun mutateLocal(operation: ScalarQuadTree.LocalMutator)
    fun evaluateLocal(operation: ScalarQuadTree.PointEvaluator)
    fun getQuadrant(x: Boolean, y: Boolean): PlanarScalarData
    fun getValues(): LongArray
    //fun exportTriangles(projector: Function<Vector3, Vector3>): Array<TriangleFace>
}