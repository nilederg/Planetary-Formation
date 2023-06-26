import storage.positionals.GeoCoord
import storage.positionals.Vector3
import storage.STL.TriangleFace
import java.io.IOException
import java.util.*

object PlanetaryFormation {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val planet = TerrestrialPlanet(2, 10000000000L)
        planet.initFractalNoise(5, 6)
        println(planet.terrain.getPoint(GeoCoord(Math.toRadians(40.0), Math.toRadians(20.0))))
        println(planet.terrain.getPoint(GeoCoord(Math.toRadians(40.0), Math.toRadians(21.0))))
        println(planet.terrain.getPoint(GeoCoord(Math.toRadians(90.0), Math.toRadians(128.0))))
        println(TriangleFace.Companion.fromOrientingPoint(Vector3(doubleArrayOf(0.0, 0.0, 0.0)), true, arrayOf<Vector3>(Vector3(doubleArrayOf(9.0, 4.0, 5.0)), Vector3(doubleArrayOf(9.0, 3.0, 8.0)), Vector3(doubleArrayOf(16.0, 49.0, 1.0)))).exportCode().contentToString())
        planet.terrain.exportFaceSTL("../face.stl")
    }

    fun getDouble(prompt: String, sc: Scanner): Double {
        var radius: Double
        while (true) {
            try {
                print(prompt)
                radius = sc.nextLine().toDouble()
                break
            } catch (e: NumberFormatException) {
                println("\nNot a valid input. Please input a double.")
            }
        }
        return radius
    }
}