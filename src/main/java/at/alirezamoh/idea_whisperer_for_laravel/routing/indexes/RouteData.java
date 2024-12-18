package at.alirezamoh.idea_whisperer_for_laravel.routing.indexes;

import java.util.Objects;

public record RouteData(String name, String uri, String filePath, int offset) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RouteData routeData = (RouteData) o;

        return offset == routeData.offset
            && Objects.equals(name, routeData.name)
            && Objects.equals(uri, routeData.uri)
            && Objects.equals(filePath, routeData.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uri, filePath, offset);
    }
}
