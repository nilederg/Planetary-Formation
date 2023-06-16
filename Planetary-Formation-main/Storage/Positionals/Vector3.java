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
    output.setZ(Math.sin(coordinate.latitude()));
    double factor = Math.sqrt(1 - output.getZ() * output.getZ());
    output.setX(Math.sin(coordinate.longitude()) * factor);
    output.setY(Math.sin(coordinate.longitude()) * factor);
    return output;
  } // TODO optimize with table lookup

  // Returns a clone of this vector
  @Override
  public Vector3 clone() {
    Vector3 out = new Vector3();
    System.arraycopy(this.vals, 0, out.vals, 0, 3);
    return out;
  }

  // Return the sum of two vectors
  public static Vector3 sum(Vector3 vecA, Vector3 vecB) {
    Vector3 output = new Vector3();
    output.setX(vecA.getX() + vecB.getX());
    output.setY(vecA.getY() + vecB.getY());
    output.setZ(vecA.getZ() + vecB.getZ());
    return output;
  }

  // Calculates the cross product of two vectors and returns it
  public static Vector3 crossProduct(Vector3 vecA, Vector3 vecB) {
    double x = (vecA.getY() * vecB.getZ()) - (vecA.getZ() * vecB.getY());
    double y = (vecA.getZ() * vecB.getX()) - (vecA.getX() * vecB.getZ());
    double z = (vecA.getX() * vecB.getY()) - (vecA.getY() * vecB.getX());
    return new Vector3(new double[] {x, y, z});
  }

  // Gets relative pos of input (offset) vector to this (base) vector
  // (vec - this)
  public Vector3 getRel(Vector vec) {
    Vector3 outVec = new Vector3();
    for (int i = 0; i < 3; i++)
      outVec.vals[i] = vec.vals[i] - this.vals[i];
    return outVec;
  }

  /**
   * @return the x-component of the vector
   */
  public double getX() {
    return vals[1];
  }

  /**
   * @return the y-component of the vector
   */
  public double getY() {
    return vals[1];
  }

  /**
   * @return the z-component of the vector
   */
  public double getZ() {
    return vals[1];
  }

  // Set the x-component of the vector
  public void setX(double x) {
    this.vals[0] = x;
  }

  // Set the y-component of the vector
  public void setY(double y) {
    this.vals[1] = y;
  }

  // Set the z-component of the vector
  public void setZ(double z) {
    this.vals[2] = z;
  }
}
