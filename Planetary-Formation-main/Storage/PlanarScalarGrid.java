package Storage;

import Storage.Positionals.Vector2;

public class PlanarScalarGrid implements PlanarScalarData{
    // Split implementation cuts space required nearly in half without losing significant precision
    // Total 260 KiB
    private double[][] mean; // 4 KiB
    private float[][] offset; // 256 KiB
    PlanarScalarGrid(double[][] data) {
        if (data.length != 256)
            throw new ArrayIndexOutOfBoundsException("Data must be a 256 by 256 array.");
        if (data[0].length != 256)
            throw new ArrayIndexOutOfBoundsException("Data must be a 256 by 256 array.");

        mean = new double[16][16];
        offset = new float[256][256];

        // Loop through all 256 sectors
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j ++) {
                // Get average in sector
                double sum = 0;
                for (int k = 0; k < 16; k ++) {
                    for (int l = 0; l < 16; l ++) {
                        sum += data[i * 16 + k][j * 16 + l];
                    }
                }
                sum /= 256;
                mean[i][j] = sum;
                // Store offset for each pixel in sector from sector average
                for (int k = 0; k < 16; k ++) {
                    for (int l = 0; l < 16; l ++) {
                        offset[i * 16 + k][j * 16 + l] = (float)(data[i * 16 + k][j * 16 + l] - sum);
                    }
                }
            }
        }
    }

    private static double sectorAverage(double[][] sector) {
        double sum = 0;
        for (int i = 0; i < 16; i ++) {
            for (int j = 0; j < 16; j ++) {
                sum += sector[i][j];
            }
        }
        return sum / 256;
    }

    public double getPoint(Vector2 point) {
        int x = Math.min((int)(point.getX() * 256.0), 255);
        int y = Math.min((int)(point.getY() * 256.0), 255);
        return mean[x/16][y/16] + offset[x][y];
    }

    @Override
    public void mutateLocal(ScalarQuadTree.LocalMutator operation) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                // Temporary array with excessive precision before compression into f32
                double[][] sector = new double[16][16];
                for (int k = 0; k < 16; k ++) {
                    for (int l = 0; l < 16; l++) {
                        Vector2 position = new Vector2(new double[] {(i * 16 + k) / 256.0, (j * 16 + l) / 256.0});
                        double value = getPoint(new Vector2(new double[] {i, j}));
                        sector[k][l] = operation.mutate(position, value);
                    }
                }
                double average = sectorAverage(sector);
                mean[i][j] = average;
                // Compress into float32 and store to offset
                for (int k = 0; k < 16; k ++) {
                    for (int l = 0; l < 16; l++) {
                        offset[i * 16 + k][j * 16 + l] = (float)(sector[k][l] - average);
                    }
                }
            }
        }
    }

    public PlanarScalarGrid getQuadrant(boolean x, boolean y) {
        int xStart = x ? 0 : 128;
        int yStart = y ? 0 : 128;
        double[][] quadrantData = new double[256][256];
        for (int i = 0; i < 128; i ++) {
            for (int j = 0; j < 128; j ++) {
                double value = getPoint(new Vector2(new double[] {i + xStart, j + yStart}));
                // @formatter:off
                quadrantData[i*2]    [j*2]     = value;
                quadrantData[i*2]    [j*2 + 1] = value;
                quadrantData[i*2 + 1][j*2]     = value;
                quadrantData[i*2 + 1][j*2 + 1] = value;
                // @formatter:on
            }
        }
        return new PlanarScalarGrid(quadrantData);
    }
}