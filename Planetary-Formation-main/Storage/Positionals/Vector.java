package Storage.Positionals;

import java.util.Random;

public abstract class Vector {
    protected double[] vals;

    public abstract Vector clone();
    public abstract Vector getRel(Vector a);

    // Adds an input vector to this vector and stores the result in this vector
    public void add(Vector vec) {
        for (int i = 0; i < getVals().length; i++)
            this.vals[i] += vec.vals[i];
    }

    // Dot product of this vector and an input vector
    public double dotProduct(Vector vec) {
        double sum = 0;
        for (int i = 0; i < getVals().length; i++)
            sum += this.vals[i] * vec.vals[i];
        return sum;
    }

    // Randomize self
    public void randomize() {
        Random random = new Random();
        for (int i = 0; i < getVals().length; i++)
            this.vals[i] = 2 * random.nextDouble() - 1;
    }

    // Randomize to a unit vector
    // No limit to runtime but faster on average than non-probabilistic trig methods
    // monte carlo
    public void randomizeUnit() {
        // Keep randomizing until within a unit sphere
        // Equal distribution of solid angle
        randomize();
        while (absoluteSquare() > 1) {
            randomize();
        }
        // Project inside of sphere to surface - angle preserving
        normalize();
    }

    // Get absolute value of self, squared
    // Returns |self|^2
    // This is for optimization
    // Faster than absolute() and good for comparing dist with a constant
    // I have no idea if the compiler makes this obsolete, so I'm leaving it
    public double absoluteSquare() {
        double dist = 0;
        for (int i = 0; i < getVals().length; i++)
            dist += Math.pow(this.vals[i], 2);
        return dist;
    }

    // True absolute value
    // Separate for optimization
    public double absolute() {
        return Math.sqrt(absoluteSquare());
    }

    // Returns its component with the largest magnitude
    public int largestComponent() {
        double max = 0;
        int index = 0;
        for (int i = 0; i < getVals().length; i ++) {
            if (Math.abs(getVals()[i]) > max) {
                max = getVals()[i];
                index = i;
            }
        }
        return index;
    }

    // Scale self by a factor
    public void scale(double factor) {
        for (int i = 0; i < getVals().length; i++)
            this.vals[i] *= factor;
    }

    // Normalize self
    // Scale so that magnitude = 1
    public void normalize() {
        double factor = 1 / absolute();
        scale(factor);
    }

    // Normalize to cube
    // Scale so that end of vector touches a face of a cube with edge length 2 (radius 1)
    public void normalizeCube() {
        double factor = 1 / getVals()[largestComponent()];
        scale(factor);
    }

    // Prints vector contents
    public void print() {
        System.out.print("[");
        for (int i = 0; i < vals.length - 1; i++)
            System.out.print(this.vals[i] + ", ");
        System.out.print(this.vals[vals.length - 1] + "]");
    }

    // Prints vector contents with a new line
    // Just for convenience :)
    public void println() {
        print();
        System.out.println();
    }

    // Floor the vector's values
    public void floor() {
        for (int i = 0; i < getVals().length; i++)
            this.vals[i] = Math.floor(this.vals[i]);
    }

    // get vector
    public double[] getVals() {
        return vals;
    }

    // set vector
    public void setVals(double[] vals) {
        this.vals = vals;
    }
}
