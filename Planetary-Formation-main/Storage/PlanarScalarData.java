package Storage;

import Storage.Positionals.Vector2;

public interface PlanarScalarData {
    public double getPoint(Vector2 point);
    public void mutateLocal(ScalarQuadTree.LocalMutator operation);
}
