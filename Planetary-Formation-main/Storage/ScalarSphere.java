package Storage;

import Storage.Positionals.Vector2;
import Storage.Positionals.Vector3;
import Storage.Positionals.GeoCoord;

import java.util.function.BiFunction;

public class ScalarSphere {
    private final ScalarQuadTree[] faces; // R(ight) L F B U D

    public ScalarSphere(int resolution) {
        faces = new ScalarQuadTree[]{new ScalarQuadTree(null, resolution)};
    }

    // Returns the face covered by the provided latitude and longitude
    private ScalarQuadTree getFace(GeoCoord point) {
        Vector3 location = Vector3.fromSphericalCoordinates(point);
        switch (location.largestComponent()) {
            case 0 -> {
                return location.getX() >= 0 ?
                        faces[0] : faces[1];
            }
            case 1 -> {
                return location.getY() >= 0 ?
                        faces[0] : faces[1];
            }
            case 2 -> {
                return location.getZ() >= 0 ?
                        faces[0] : faces[1];
            }
        }
        throw new IllegalArgumentException("Case " + location.largestComponent() + " is not a valid dimension index.");
    }

    private Vector2 getPointOnFace(Vector3 cubicVector) {
        cubicVector.normalizeCube();
        switch (cubicVector.largestComponent()) {
            case 0 -> {
                return new Vector2(new double[]{cubicVector.getY() / 2 + 0.5, cubicVector.getZ() / 2 + 0.5});
            }
            case 1 -> {
                return new Vector2(new double[]{cubicVector.getX() / 2 + 0.5, cubicVector.getZ() / 2 + 0.5});
            }
            case 2 -> {
                return new Vector2(new double[]{cubicVector.getX() / 2 + 0.5, cubicVector.getY() / 2 + 0.5});
            }
        }
        // Should be an unreachable state
        throw new IllegalArgumentException("Case " + cubicVector.largestComponent() + " is not a valid dimension index.");
    }

    // Gets the value at any given point, with a GeoCoord
    public double getPoint(GeoCoord point) {
        Vector3 cubicVector = Vector3.fromSphericalCoordinates(point);
        return getPoint(cubicVector);
    }

    // Gets the value at any given point, with a Vector3
    private double getPoint(Vector3 cubicVector) {
        int largestComponent = cubicVector.largestComponent();
        double largestValue = cubicVector.getVals()[largestComponent];
        ScalarQuadTree tree = largestValue >= 0 ?
                faces[largestComponent * 2] :
                faces[largestComponent * 2 + 1];
        return tree.getPoint(getPointOnFace(cubicVector));
    }

    @FunctionalInterface
    public interface SphereMutation {
        double mutate(Vector3 point, double value);
    }

    @FunctionalInterface
    public interface SphereApplicationZone {
        // Point is point on sphere surface, range is radians distance from point
        // Returns true if anything within range of point is in the "zone", false otherwise
        boolean checkWithin(Vector3 point, double range);
    }

    private static Vector3 faceCenter(int face) {
        switch (face) {
            case 0: return new Vector3(new double[] {-1, 0, 0});
            case 1: return new Vector3(new double[] { 1, 0, 0});
            case 2: return new Vector3(new double[] {0, -1, 0});
            case 3: return new Vector3(new double[] {0,  1, 0});
            case 4: return new Vector3(new double[] {0, 0, -1});
            case 5: return new Vector3(new double[] {0, 0,  1});
        }
        throw new IllegalArgumentException("Face must be an integer between 0 and 6.");
    }

    private static Vector3 placeFace(Vector2 point, int face) {
        Vector3 location = null;
        switch (face) {
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
        return location;
    }

    // Evaluates a lambda, mutating the value at every point on the sphere
    // Lambda takes in its own geographic position and outputs its new value
    // Efficiency improved by only running on necessary regions with ApplicationZone
    public void mutateSphereLocal(SphereMutation operation, SphereApplicationZone zone) {
        for (int i = 0; i < 6; i ++) {
            int finalI = i;
            // 1 radian is actually very close to the maximum distance from center here, and it's easier on the computer
            if (!zone.checkWithin(faceCenter(i), 1))
                continue;
            // Only mutate if within zone
            ScalarQuadTree.LocalMutator localOperation = (Vector2 point, double value) -> {
                return operation.mutate(placeFace(point, finalI), value);
            };
            ScalarQuadTree.ApplicationZone localZone = (Vector2 point, double range) -> {
                return zone.checkWithin(placeFace(point, finalI), range);
            };
            faces[i].mutateLocal(localOperation, localZone, 1, new Vector2(new double[] {0, 0}));
        }
    }

    // Input 1 is this, input 2 is inSphere
    public void biMutate(BiFunction<Double, Double, Double> operator, ScalarSphere inSphere) {
        mutateSphereLocal((Vector3 point, double value) -> {
            return operator.apply(value, inSphere.getPoint(point));
        }, (Vector3 point, double range) -> {return true;});
    }

    public void exportPng(long fileSize) {
        // TODO implement this feature
        //      export a png with width:height ratio 2:1
        //      in equidistant projection
        //      implement soon so testing can occur
    }

    public void exportStl(long filesize) {
        // TODO implement this feature
        //      export stereolithography mesh
    }
}
