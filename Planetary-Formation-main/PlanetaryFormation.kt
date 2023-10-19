import storage.PlanarScalarGrid
import storage.ScalarQuadTree
import storage.positionals.GeoCoord
import storage.positionals.Index
import java.io.IOException
import java.util.*
import kotlin.math.absoluteValue

fun main(args: Array<String>) {
    val planet = TerrestrialPlanet(4, 10000000000L)
    //planet.initFractalNoise(1, 6, 1000000.0)
    planet
    println(planet.terrain.getPoint(GeoCoord(Math.toRadians(40.0), Math.toRadians(20.0))))
    println(planet.terrain.getPoint(GeoCoord(Math.toRadians(40.0), Math.toRadians(23.0))))
    println(planet.terrain.getPoint(GeoCoord(Math.toRadians(90.0), Math.toRadians(128.0))))
//    println(TriangleFace.fromOrientingPoint(Vector3(doubleArrayOf(0.0, 0.0, 0.0)), true, arrayOf(Vector3(doubleArrayOf(9.0, 4.0, 5.0)), Vector3(doubleArrayOf(9.0, 3.0, 8.0)), Vector3(doubleArrayOf(16.0, 49.0, 1.0)))).exportCode().contentToString())
//    planet.terrain.exportFaceSTL("../face.stl")

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
