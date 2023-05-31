package Storage;

import Storage.Positionals.Vector2;

public class PlanarScalarGrid implements PlanarScalarData{
    private double[][] data;
    PlanarScalarGrid(double[][] data) {
        this.data = data;
        if (data.length != 256)
            throw new ArrayIndexOutOfBoundsException("Data must be a 256 by 256 array.");
        if (data[0].length != 256)
            throw new ArrayIndexOutOfBoundsException("Data must be a 256 by 256 array.");
    }

    PlanarScalarGrid() {
        this.data = new double[256][256];
    }

    public double getPoint(Vector2 point) {
        int xIndex = (int)(point.getX() * 256.0);
        int yIndex = (int)(point.getY() * 256.0);
        return this.data[xIndex][yIndex];
    }

    private void setPoint(double x, double y, double value) {
        int xIndex = (int)(x * 256.0);
        int yIndex = (int)(y * 256.0);
        this.data[xIndex][yIndex] = value;
    }
}