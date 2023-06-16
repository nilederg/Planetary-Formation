package Storage;

public interface PlanarScalarCompressedData extends PlanarScalarData{
    // Returns a grid of the values in the compressed data
    PlanarScalarGrid decompress();
}
