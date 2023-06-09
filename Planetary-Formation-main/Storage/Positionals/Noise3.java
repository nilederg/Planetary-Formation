package Storage.Positionals;

public class Noise3 {
  private Vector3[][][] vectors;
  public final int[] dimensions;

  // Initialize with custom dimensions
  public Noise3(int[] dims) {
    // Set dimensions
    dimensions = dims.clone();
    // Initialize vector array dimensions
    vectors = new Vector3[(int) dims[0]][(int) dims[1]][(int) dims[2]];
    // Initialize individual vectors (everything starts at 0)
    for (int x = 0; x < dimensions[0]; x++)
      for (int y = 0; y < dimensions[1]; y++)
        for (int z = 0; z < dimensions[2]; z++)
          vectors[x][y][z] = new Vector3();
  }

  // Randomizes the internal vectors
  public void randomize() {
    // i wonder if there's a way to get rid of redundant recursive similar for loops
    // without using something crazy like lambdas
    for (int x = 0; x < dimensions[0]; x++)
      for (int y = 0; y < dimensions[1]; y++)
        for (int z = 0; z < dimensions[2]; z++)
          vectors[x][y][z].randomizeUnit();
  }

  // Uses 5th order smoothstep to blend between 2 values at a phase
  private static double smoothStep(double first, double second, double phase) {
    double func = 6 * Math.pow(phase, 5) - 15 * Math.pow(phase, 4) + 10 * Math.pow(phase, 3);
    return first + func * (second - first);
  }

  // Calculates dot product between single internal vector and relative input
  // point vector
  // Vector corner is a position in space that has a vector at its position - it
  // is not the vector itself
  private double getDotSingle(Vector3 point, Vector3 corner) {
    // Get vector to multiply by
    int posX = Math.min((int) corner.getX(), dimensions[0] - 1);
    int posY = Math.min((int) corner.getY(), dimensions[1] - 1);
    int posZ = Math.min((int) corner.getZ(), dimensions[2] - 1);
    Vector3 base = vectors[posX][posY][posZ];
    // Point relative to corner
    Vector3 offset = corner.getRel(point);
    double output = base.dotProduct(offset);
    return output;
  }

  // Calculates value at a point
  public double getPoint(Vector3 inPoint) {
    Vector3 point = inPoint.clone();
    if (point.getX() >= dimensions[0] - 1)
      point.setX(dimensions[0] - 1.00000001);
    if (point.getY() >= dimensions[1] - 1)
      point.setY(dimensions[1] - 1.00000001);
    if (point.getZ() >= dimensions[2] - 1)
      point.setZ(dimensions[2] - 1.00000001);

    Vector3 base = point.clone(); // Floored point
    base.floor();

    // Eliminate redundancy by creating redundancy.
    Vector3 offset = new Vector3();
    Vector3 phase = base.getRel(point);
    double[] xBases = { 0, 0 }; // For interpolation

    for (int xOff = 0; xOff <= 1; xOff++) {
      double[] yBases = { 0, 0 }; // For interpolation

      for (int yOff = 0; yOff <= 1; yOff++) {
        double[] zBases = { 0, 0 }; // For interpolation

        for (int zOff = 0; zOff <= 1; zOff++) {
          // yes I know this is redundant
          // but the alternative is replacing all '*off's
          // with a list, and that would look so ugly
          offset.setX(xOff);
          offset.setY(yOff);
          offset.setZ(zOff);
          offset.add(base);

          // Use vector to lookup other vector in a 3d array
          Vector3 gradientVec = new Vector3();
          int posX = Math.min((int) offset.getX(), dimensions[0] - 1);
          int posY = Math.min((int) offset.getY(), dimensions[1] - 1);
          int posZ = Math.min((int) offset.getZ(), dimensions[2] - 1);
          double[] values = vectors[posX][posY][posZ].getVals();
          gradientVec.setVals(values);
          // Store the two values to an array
          zBases[zOff] = getDotSingle(point, offset);
        }
        // Interpolate between the two z corners at the z value
        yBases[yOff] = smoothStep(zBases[0], zBases[1], phase.getVals()[2]);
      }
      // Interpolate between the two y points at the y value
      xBases[xOff] = smoothStep(yBases[0], yBases[1], phase.getVals()[1]);
    }
    // Interpolate between the two x points at the x value
    return smoothStep(xBases[0], xBases[1], phase.getVals()[0]);
    // hold up is this code unreadable (oh no)
    // my whitespace looks goofy
  }
}