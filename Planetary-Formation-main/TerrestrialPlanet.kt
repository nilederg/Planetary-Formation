import storage.positionals.Noise3
import storage.positionals.Vector3
import storage.ScalarSphere
import storage.ScalarSphere.SphereApplicationZone
import storage.ScalarSphere.SphereMutation
import kotlin.math.pow

class TerrestrialPlanet internal constructor(resolution: Int, maxRAM: Long) {
    var terrain: ScalarSphere

    init {
        terrain = ScalarSphere(resolution, maxRAM)
    }

    // Scale is the lowest frequency, depth is the highest
    fun initFractalNoise(scale: Int, depth: Int) {
        println("Generating fractal noise...")
        val layers: Array<Noise3> = Array(depth - scale) {i ->
            val freq: Int = 1 + 2.0.pow((i + scale).toDouble()).toInt()
            val layer = Noise3(intArrayOf(freq, freq, freq))
            layer.randomize()
            return@Array layer
        }
        println("Fractal noise generated")
        println("Filling sphere with noise...")
        terrain.mutateSphereLocal({ point: Vector3, _: Double ->
            val adjustedPoint: Vector3 = point.clone()
            adjustedPoint.scale(0.5)
            adjustedPoint.add(Vector3(doubleArrayOf(0.5, 0.5, 0.5)))
            var sum = 0.0
            for (i in layers.indices) {
                val layer: Noise3 = layers[i]
                val layerPoint: Vector3 = adjustedPoint.clone()
                layerPoint.scale((layer.dimensions[0] - 1).toDouble())
                sum += layer.getPoint(layerPoint) * 0.5.pow(i.toDouble())
            }
            sum
        }, { _: Vector3, _: Double -> true })
        println("Sphere randomized with fractal noise")
    }
}