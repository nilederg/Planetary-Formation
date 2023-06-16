package Storage.STL;

import java.io.FileWriter;
import java.io.IOException;

public class StlFile {
    private FileWriter file;

    private int triCount;

    public int getTriCount() {
        return triCount;
    }

    // Header is 80B, 10B reserved for unit declaration, 70B remain
    // Reserve 4B at the start of the file for triangle count
    // Creates a file interface with the filename, the unit that the file will be stored in, and a short informative header.
    StlFile(String filename, String unit, String header) throws IOException {
        file = new FileWriter(filename);
        triCount = 0;

        if (header.length() != 72)
            throw new IllegalArgumentException("Header must be 70 bytes");
        if (unit.length() != 2)
            throw new IllegalArgumentException("Unit must be 2 bytes (mm,cm, m,ft,in,ly)");

        file.write(header + "\nUNITS=" + unit + "\n    ");
    }

    // Writes a TriangleFace to the file
    public void writeTriangle(TriangleFace triangle) throws IOException {
        this.triCount ++;

        byte[] code = triangle.exportSTLCode();

        for (byte codeByte : code)
            this.file.append((char) codeByte);
    }
}
