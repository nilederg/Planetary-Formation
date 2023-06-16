package Storage;

import Storage.Positionals.Vector2;

public interface PlanarScalarData {
    // gets the value at a point on the field
    public double getPoint(Vector2 point);
    // Mutates every point on the field by setting it to the value output by the operation
    public void mutateLocal(ScalarQuadTree.LocalMutator operation);
    // Returns one quarter of the field, scaled up
    // false means 0, true means 1
    public PlanarScalarData getQuadrant(boolean x, boolean y);
}
