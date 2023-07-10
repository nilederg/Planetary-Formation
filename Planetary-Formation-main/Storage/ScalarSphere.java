package Storage;

import Storage.Positionals.Vector2;
import Storage.Positionals.Vector3;
import Storage.Positionals.GeoCoord;

public class ScalarSphere {
    private ScalarQuadTree faces[]; // R(ight) L F B U D

    ScalarSphere() {
        faces = new ScalarQuadTree[]{new ScalarQuadTree(null, 3)};
    }

    // Returns the face covered by the provided latitude and longitude
    public ScalarQuadTree getFace(GeoCoord point) {
        Vector3 location = Vector3.fromSphericalCoordinates(point);
        switch (location.largestComponent()) {
            case 0:
                return location.getX() >= 0 ?
                        faces[0] : faces[1];
            case 1:
                return location.getY() >= 0 ?
                        faces[0] : faces[1];
            case 2:
                return location.getZ() >= 0 ?
                        faces[0] : faces[1];
        }
        throw new IllegalArgumentException("Case " + location.largestComponent() + " is not a valid dimension index.");
    }

    private Vector2 getPointOnFace(Vector3 cubicVector) {
        cubicVector.normalizeCube();
        switch (cubicVector.largestComponent()) {
            case 0:
                return new Vector2(new double[]{cubicVector.getY() / 2 + 0.5, cubicVector.getZ() / 2 + 0.5});
            case 1:
                return new Vector2(new double[]{cubicVector.getX() / 2 + 0.5, cubicVector.getZ() / 2 + 0.5});
            case 2:
                return new Vector2(new double[]{cubicVector.getX() / 2 + 0.5, cubicVector.getY() / 2 + 0.5});
        }
        // Should be an unreachable state
        throw new IllegalArgumentException("Case " + cubicVector.largestComponent() + " is not a valid dimension index.");
    }

    // Gets the value at any given point
    public double getPoint(GeoCoord point) {
        Vector3 cubicVector = Vector3.fromSphericalCoordinates(point);

        int largestComponent = cubicVector.largestComponent();
        double largestValue = cubicVector.getVals()[largestComponent];
        ScalarQuadTree tree = largestValue >= 0 ?
                faces[largestComponent * 2] :
                faces[largestComponent * 2 + 1];
        return tree.getPoint(getPointOnFace(cubicVector));
    }

    @FunctionalInterface
    public interface LocalSphereMutator {
        double mutate(Vector3 point, double value);
    }

    // Evaluates a lambda, mutating the value at every point on the sphere
    // Lambda takes in its own geographic position and outputs its new value
    public void mutateSphereLocal(LocalSphereMutator operation) {
        for (int i = 0; i < 6; i ++) {
            int finalI = i;
            ScalarQuadTree.LocalMutator localOperation = (Vector2 point, double value) -> {
                Vector3 location = null;
                switch (finalI) {
                    case 0:
                        location = new Vector3(new double[] {-1, point.getX() * 2 - 1, point.getY() * 2 - 1});
                    case 1:
                        location = new Vector3(new double[] { 1, point.getX() * 2 - 1, point.getY() * 2 - 1});
                    case 2:
                        location = new Vector3(new double[] {point.getX() * 2 - 1, -1, point.getY() * 2 - 1});
                    case 3:
                        location = new Vector3(new double[] {point.getX() * 2 - 1,  1, point.getY() * 2 - 1});
                    case 4:
                        location = new Vector3(new double[] {point.getX() * 2 - 1, point.getY() * 2 - 1, -1});
                    case 5:
                        location = new Vector3(new double[] {point.getX() * 2 - 1, point.getY() * 2 - 1,  1});
                }
                location.normalize();
                return operation.mutate(location, value);
            };
            faces[i].mutateLocal(localOperation);
            // TODO define localOperation
        }
    }
}
