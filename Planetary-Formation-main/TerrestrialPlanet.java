import Storage.Positionals.Noise3;
import Storage.ScalarSphere;

public class TerrestrialPlanet {
    private ScalarSphere terrain;

    public void fractalNoise(int depth) {
        Noise3[] layers = new Noise3[depth];

        for (int i = 0; i < depth; i ++) {
            int freq = (int) Math.pow(2, i);
            layers[i] = new Noise3(new int[] {freq, freq, freq});

        }
    }
}
