package storage.positionals

import java.util.*

// Against my better judgement, a class without boilerplate (not anymore ATTICUS)
// I will NEVER need more than this in geoCoord, it's simply cleaner than sending a 2 double array.
// I am confident in this one violation of encapsulation principles.

data class GeoCoord constructor(val latitude: Double, val longitude: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val geoCoord: GeoCoord = other as GeoCoord
        return geoCoord.latitude.compareTo(latitude) == 0 && geoCoord.longitude.compareTo(longitude) == 0
    }

    public override fun hashCode(): Int {
        return Objects.hash(latitude, longitude)
    }
}