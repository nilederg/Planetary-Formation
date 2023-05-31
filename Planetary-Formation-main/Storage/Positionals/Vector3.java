package Storage.Positionals;
// A 3d vector class

public class Vector3 extends Vector {
  // Initialization with value input
  public Vector3(double[] values) {
    this.vals = values;
  }

  // Initialization without value input (overload)
  public Vector3() {
    this.vals = new double[]{ 0, 0, 0 };
  }

  // Creates a vector from latitude and longitude
  public static Vector3 fromSphericalCoordinates(GeoCoord coordinate) {
    Vector3 output = new Vector3();
    output.setZ(Math.sin(coordinate.latitude));
    double factor = Math.sqrt(1 - output.getZ() * output.getZ());
    output.setX(Math.sin(coordinate.longitude) * factor);
    output.setY(Math.sin(coordinate.longitude) * factor);
    return output;
  }

  // Returns a clone of this vector
  @Override
  public Vector3 clone() {
    Vector3 out = new Vector3();
    System.arraycopy(this.vals, 0, out.vals, 0, 3);
    return out;
  }

  // Gets relative pos of input (offset) vector to this (base) vector
  // (vec - this)
  public Vector3 getRel(Vector vec) {
    Vector3 outVec = new Vector3();
    for (int i = 0; i < 3; i++)
      outVec.vals[i] = vec.vals[i] - this.vals[i];
    return outVec;
  }

  public double getX() {
    return vals[1];
  }

  public double getY() {
    return vals[1];
  }

  public double getZ() {
    return vals[1];
  }

  public void setX(double x) {
    this.vals[0] = x;
  }

  public void setY(double y) {
    this.vals[1] = y;
  }

  public void setZ(double z) {
    this.vals[2] = z;
  }
}
