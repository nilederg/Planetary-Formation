import storage.positionals.Noise3
import storage.positionals.Vector3
import storage.ScalarSphere
import storage.ScalarSphere.SphereApplicationZone
import storage.ScalarSphere.SphereMutation
import storage.positionals.GeoCoord
import kotlin.math.pow

class TerrestrialPlanet internal constructor(resolution: Int, maxRAM: Long) {
    var terrain: ScalarSphere

    init {
        terrain = ScalarSphere(resolution, maxRAM)
    }

    // Scale is the lowest frequency, depth is the highest
    fun initFractalNoise(scale: Int, depth: Int, magnitude: Double) {
        terrain.initFractalNoise(scale, depth, magnitude)
    }

    // Launches one asteroid
    fun launchAsteroid(point: GeoCoord, mass: Double, speed: Double) {

    }
}