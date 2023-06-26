package storage

open interface PlanarScalarCompressedData : PlanarScalarData {
    open fun decompress(): PlanarScalarGrid?
}