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
        int xIndex = Math.min((int)(point.getX() * 256.0), 255);
        int yIndex = Math.min((int)(point.getY() * 256.0), 255);
        return this.data[xIndex][yIndex];
    }

    @Override
    public void mutateLocal(ScalarQuadTree.LocalMutator operation) {
        for (int i = 0; i < 256; i ++) {
            for (int j = 0; j < 256; j ++) {
                 data[i][j] = operation.mutate(new Vector2(new double[] {i / 256.0, j / 256.0}), data[i][j]);
            }
        }
    }

    public PlanarScalarGrid getQuadrant(boolean x, boolean y) {
        int xStart = x ? 0 : 128;
        int yStart = y ? 0 : 128;
        double[][] quadrantData = new double[256][256];
        for (int i = 0; i < 128; i ++) {
            for (int j = 0; j < 128; j ++) {
                // @formatter:off
                quadrantData[i*2]    [j*2]     = data[i + xStart][j + yStart];
                quadrantData[i*2]    [j*2 + 1] = data[i + xStart][j + yStart];
                quadrantData[i*2 + 1][j*2]     = data[i + xStart][j + yStart];
                quadrantData[i*2 + 1][j*2 + 1] = data[i + xStart][j + yStart];
                // @formatter:on
            }
        }
        return new PlanarScalarGrid(quadrantData);
    }
}