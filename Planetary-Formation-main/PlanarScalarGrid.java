import java.util.zip.DataFormatException;

public class PlanarScalarGrid implements PlanarScalarData{
    private final int[] size;
    private double[][] data;
    PlanarScalarGrid(double[][] data) {
        this.size = new int[] {data.length, data[0].length};
        this.data = data;
    }

    PlanarScalarGrid(int[] size) {
        this.size = size;
        this.data = new double[ size[0] ][ size[1] ];
        if (this.size.length != 2)
            throw new IllegalArgumentException("PlanarScalarGrid can only have 2 dimensional size.");
    }

    PlanarScalarGrid() {
        this.size = new int[] {256, 256};
        this.data = new double[256][256];
    }

    public int[] getSize() {
        return size;
    }

    public double getPoint(double x, double y) {
        int xIndex = (int)(x * this.size[0]);
        int yIndex = (int)(y * this.size[1]);
        return this.data[xIndex][yIndex];
    }

    public void setPoint(double x, double y, double value) {
        int xIndex = (int)(x * this.size[0]);
        int yIndex = (int)(y * this.size[1]);
        this.data[xIndex][yIndex] = value;
    }
}