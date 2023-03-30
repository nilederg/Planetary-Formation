package Positionals;

public class Noise3 {
  private Vector[][][] vectors;
  public Vector dimensions;

  // Initialize with custom dimensions
  public Noise3(Vector dims) {
    // Set dimensions
    dimensions = dims.clone();
    // Initialize vector array dimensions
    vectors = new Vector[(int) dims.getVals()[0]][(int) dims.getVals()[1]][(int) dims.getVals()[2]];
    // Initialize individual vectors (everything starts at 0)
    for (int x = 0; x < dimensions.getVals()[0]; x++) {
      for (int y = 0; y < dimensions.getVals()[1]; y++) {
        for (int z = 0; z < dimensions.getVals()[2]; z++) {
          vectors[x][y][z] = new Vector();
        }
      }
    }
  }

  // Randomizes the internal vectors
  public void randomize() {
    // i wonder if there's a way to get rid of redundant recursive similar for loops
    // without using something crazy like lambdas
    for (int x = 0; x < dimensions.getVals()[0]; x++) {
      for (int y = 0; y < dimensions.getVals()[1]; y++) {
        for (int z = 0; z < dimensions.getVals()[2]; z++) {
          vectors[x][y][z].randomizeUnit();
        }
      }
    }
  }

  // Uses 5th order smoothstep to blend between 2 values at a phase
  public static double smoothStep(double first, double second, double phase) {
    double func = 6 * Math.pow(phase, 5) - 15 * Math.pow(phase, 4) + 10 * Math.pow(phase, 3);
    return first + func * (second - first);
  }

  // Calculates dot product between single internal vector and relative input
  // point vector
  // Vector corner is a position in space that has a vector at its position - it
  // is not the vector itself
  private double getDotSingle(Vector point, Vector corner) {
    // Get vector to multiply by
    Vector base = this.vectors[(int) corner.getVals()[0]][(int) corner.getVals()[1]][(int) corner.getVals()[2]];
    // Point relative to corner
    Vector offset = corner.getRel(point);
    double output = base.dotProduct(offset);
    return output;
  }

  // Calculates value at a point
  public double getPoint(Vector point) {
    Vector base = point.clone(); // Floored point
    base.floor();

    // Eliminate redundancy by creating redundancy.
    Vector offset = new Vector();
    Vector phase = base.getRel(point);
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
          Vector gradientVec = new Vector();
          double[] values = vectors[(int) offset.getVals()[0]][(int) offset.getVals()[1]][(int) offset.getVals()[2]].getVals();
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