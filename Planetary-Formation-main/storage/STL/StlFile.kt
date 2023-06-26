package storage.STL

import java.io.FileWriter
import java.io.IOException

class StlFile constructor(filename: String, unit: String, header: String) {
    private val file: FileWriter
    private var triCount: Int
    fun getTriCount(): Int {
        return triCount
    }

    // Header is 80B, 10B reserved for unit declaration, 70B remain
    // Reserve 4B at the start of the file for triangle count
    init {
        file = FileWriter(filename)
        triCount = 0
        if (header.length != 70) throw IllegalArgumentException("Header must be 70 bytes")
        if (unit.length != 2) throw IllegalArgumentException("Unit must be 2 bytes (mm,cm, m,ft,in,ly)")
        file.write("$header\nUNITS=$unit\n    ")
    }

    @Throws(IOException::class)
    fun writeTriangle(triangle: TriangleFace) {
        triCount++
        val code: ByteArray = triangle.exportCode()
        for (codeByte: Byte in code) file.append(Char(codeByte.toUShort()))
    }

    @Throws(IOException::class)
    fun writeTriangles(triangles: Array<TriangleFace>) {
        for (triangle: TriangleFace in triangles) writeTriangle(triangle)
    }
}