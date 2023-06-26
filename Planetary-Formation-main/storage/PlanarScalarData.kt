package storage

import storage.positionals.Vector2
import storage.positionals.Vector3
import storage.STL.TriangleFace
import storage.ScalarQuadTree.LocalMutator
import java.util.function.Function

open interface PlanarScalarData {
    // TODO: Convert to fixed-point in millimeters
    open fun getPoint(point: Vector2): Double
    open fun mutateLocal(operation: LocalMutator)
    open fun getQuadrant(x: Boolean, y: Boolean): PlanarScalarData
    open fun exportTriangles(projector: Function<Vector3, Vector3>): Array<TriangleFace>
}