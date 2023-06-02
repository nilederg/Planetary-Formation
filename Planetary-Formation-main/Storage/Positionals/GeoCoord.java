package Storage.Positionals;

import java.util.Objects;

// Against my better judgement, a class without boilerplate (not anymore ATTICUS)
// I will NEVER need more than this in geoCoord, it's simply cleaner than sending a 2 double array.
// I am confident in this one violation of encapsulation principles.
public record GeoCoord(double latitude, double longitude) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoCoord geoCoord = (GeoCoord) o;
        return Double.compare(geoCoord.latitude, latitude) == 0 && Double.compare(geoCoord.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
