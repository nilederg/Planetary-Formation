package storage.STL

import java.io.FileWriter
import java.io.IOException

class StlFile constructor(filename: String, unit: String, header: String) {
    private val file: FileWriter
    private var triCount: Int
    private val buffer: CharArray
    private var bufferUsage: Int

    fun getTriCount(): Int {
        return triCount
    }

    // Header is 80B, 10B reserved for unit declaration, 70B remain
    // Reserve 4B at the start of the file for triangle count
    init {
        file = FileWriter(filename)
        triCount = 0
        buffer = CharArray(65536)
        bufferUsage = 0
        if (header.length > 70) throw IllegalArgumentException("Header must be 70 bytes or less")
        if (unit.length != 2) throw IllegalArgumentException("Unit must be 2 bytes (mm,cm, m,ft,in,ly)")
        if (header.length < 70) {
            file.write("$header\nUNITS=$unit" +
                    String(CharArray(70-header.length) { return@CharArray ' ' }) +
                    "\n    ")
        } else {
            file.write("$header\nUNITS=$unit\n    ")//TODO Write to triangle count
        }
    }

    @Throws(IOException::class)
    fun writeTriangle(triangle: TriangleFace) {
        triCount++
        val code: ByteArray = triangle.exportCode()
        for (codeByte: Byte in code) buffer[bufferUsage] = Char(codeByte.toUShort())
        bufferUsage++
        if (bufferUsage == 65536) {
            file.append(buffer.toString())
            bufferUsage = 0
        }
    }

    @Throws(IOException::class)
    fun writeTriangles(triangles: Array<TriangleFace>) {
        for (triangle: TriangleFace in triangles) writeTriangle(triangle)
    }

    fun close() {
        val remainingBuffer: CharArray = CharArray(bufferUsage) { i: Int -> return@CharArray buffer[i] }
        file.append(remainingBuffer.toString())
        file.close()
    }
}