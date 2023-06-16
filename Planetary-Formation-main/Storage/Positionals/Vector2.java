package Storage.Positionals;

import java.util.function.DoubleUnaryOperator;

public class Vector2 extends Vector {
    // Initialization with value input
    public Vector2(double[] values) {
        this.vals = values;
    }

    // Initialization without value input (overload)
    public Vector2() {
        this.vals = new double[]{ 0, 0 };
    }

    // Evaluates an arbitrary operation on each component, then returns the result
    public void mutateWithLambda(DoubleUnaryOperator operation) {
        double[] newVals = getVals();
        for (int i = 0; i < 2; i ++) {
            newVals[i] = operation.applyAsDouble(newVals[i]);
        }
        setVals(newVals);
    }

    // Returns a clone of this vector
    @Override
    public Vector2 clone() {
        Vector2 out = new Vector2();
        System.arraycopy(this.vals, 0, out.vals, 0, 2);
        return out;
    }

    // Return the sum of two vectors
    public static Vector2 sum(Vector2 vecA, Vector2 vecB) {
        Vector2 output = new Vector2();
        output.setX(vecA.getX() + vecB.getX());
        output.setY(vecA.getY() + vecB.getY());
        return output;
    }

    // Gets relative pos of input (offset) vector to this (base) vector
    // (vec - this)
    public Vector2 getRel(Vector vec) {
        Vector2 outVec = new Vector2();
        for (int i = 0; i < 2; i++)
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

    // Set the x-component of the vector
    public void setX(double x) {
        this.vals[0] = x;
    }

    // Set the y-component of the vector
    public void setY(double y) {
        this.vals[1] = y;
    }
}
