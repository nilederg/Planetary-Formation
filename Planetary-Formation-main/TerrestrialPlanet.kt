import Storage.Positionals.Noise3;
import Storage.Positionals.Vector3;
import Storage.ScalarSphere;

public class TerrestrialPlanet {
    public ScalarSphere terrain;

    TerrestrialPlanet (int resolution, long maxRAM) {
        terrain = new ScalarSphere(resolution, maxRAM);
    }

    // Scale is the lowest frequency, depth is the highest
    public void initFractalNoise(int scale, int depth) {
        System.out.println("Generating fractal noise...");

        Noise3[] layers = new Noise3[depth - scale];

        for (int i = 0; i < depth - scale; i ++) {
            int freq = 1 + (int) Math.pow(2, i + scale);
            layers[i] = new Noise3(new int[] {freq, freq, freq});
            layers[i].randomize();
        }
        System.out.println("Fractal noise generated");
        System.out.println("Filling sphere with noise...");

        terrain.mutateSphereLocal((Vector3 point, double value) -> {
            Vector3 adjustedPoint = point.clone();
            adjustedPoint.scale(0.5);
            adjustedPoint.add(new Vector3(new double[] {0.5, 0.5, 0.5}));
            double sum = 0;
            for (int i = 0; i < layers.length; i ++) {
                Noise3 layer = layers[i];
                Vector3 layerPoint = adjustedPoint.clone();
                layerPoint.scale(layer.dimensions[0] - 1);
                sum += layer.getPoint(layerPoint) * Math.pow(0.5, i);
            }
            return sum;
        }, (Vector3 point, double range) -> {return true;});
        System.out.println("Sphere randomized with fractal noise");
    }
}
