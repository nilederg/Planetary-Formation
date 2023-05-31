package Storage;

import Storage.Positionals.Vector2;
import Storage.Positionals.Vector3;
import Storage.Positionals.GeoCoord;

import java.util.function.Function;

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

    // Gets the value at any given point
    public double getPoint(GeoCoord point) {
        Vector3 cubicVector = Vector3.fromSphericalCoordinates(point);
        cubicVector.normalizeCube();

        switch (cubicVector.largestComponent()) {
            case 0: //  Which side of the cube, based on sign                   The point on the face to be called
                return (cubicVector.getX() >= 0 ? faces[0] : faces[1]).getPoint(new Vector2(new double[]{cubicVector.getY(), cubicVector.getZ()}));
            case 1:
                return (cubicVector.getY() >= 0 ? faces[0] : faces[1]).getPoint(new Vector2(new double[]{cubicVector.getX(), cubicVector.getZ()}));
            case 2:
                return (cubicVector.getZ() >= 0 ? faces[0] : faces[1]).getPoint(new Vector2(new double[]{cubicVector.getX(), cubicVector.getY()}));
        }

        throw new IllegalArgumentException("Case " + cubicVector.largestComponent() + " is not a valid dimension index.");
    }

    @FunctionalInterface
    public interface SphereMutator {
        //
    }

    // Evaluates a lambda, mutating the value at every point on the sphere
    // Lambda takes in its own geographic position and outputs its new value
    public void mutateSphere(Function<GeoCoord, Double> operation) {
        //
    }
}
