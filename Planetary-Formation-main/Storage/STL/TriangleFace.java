package Storage.STL;

import Storage.Positionals.Vector3;

public class TriangleFace {
    public final Vector3 normal;
    public final Vector3[] vertices;

    // Creates a triangle with all points + the normal vector
    TriangleFace (Vector3 normal, Vector3[] vertices) {
        if (vertices.length != 3)
            throw new IllegalArgumentException("TriangleFace must have 3 vertices.");

        this.normal = normal;
        this.vertices = vertices;
    }

    // Creates a triangle with all points + a point on one side of the triangle
    // + a boolean stating whether the normal vector faces toward that point
    public static TriangleFace fromOrientingPoint (Vector3 orientingPoint, boolean facingTowards, Vector3[] vertices) {
        if (vertices.length != 3)
            throw new IllegalArgumentException("TriangleFace must have 3 vertices.");

        Vector3 AB = vertices[0].getRel(vertices[1]);
        Vector3 AC = vertices[0].getRel(vertices[2]);

        Vector3 normal = Vector3.crossProduct(AB, AC);

        double product = normal.dotProduct(vertices[0].getRel(orientingPoint));

        if (product > 0 ^ facingTowards) {
            normal.scale(-1);
            Vector3 vertexA = vertices[0].clone();
            vertices[0] = vertices[1].clone();
            vertices[1] = vertexA;
        }

        normal.normalize();

        return new TriangleFace(normal, vertices);
    }

    // Converts a float into an array of the 4 representative bytes
    private static byte[] floatToByteArray(float value) {
        int intBits =  Float.floatToIntBits(value);
        return new byte[] {
                (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }

    // Exports the 50 byte code of the triangle in a byte array, this is how it is stored in an stl file
    public byte[] exportSTLCode() {
        byte[] byteArray = new byte[50];

        // Array of all values which will be stored
        double[][] doubleArray = new double[4][3];
        doubleArray[0] = this.normal.getVals();
        for (int i = 0; i < 3; i ++)
            doubleArray[i+1] = this.vertices[i].getVals();

        // Convert values into floats and store in byteArray
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 3; j++) {
                float value = (float)doubleArray[i][j];
                byte[] bytes = floatToByteArray(value);
                assert bytes.length == 4;
                for (int k = 0; k < 4; k ++)
                    byteArray[4 * (3 * i + j) + k] = bytes[k];
            }
        }

        // Attribute Byte Count is always 0
        byteArray[48] = 0;
        byteArray[49] = 0;

        return byteArray;
    }
}
