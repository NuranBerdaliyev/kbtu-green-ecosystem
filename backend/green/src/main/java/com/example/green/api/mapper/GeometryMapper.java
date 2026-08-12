package com.example.green.api.mapper;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Component;

@Component
public class GeometryMapper {
    private final WKTWriter wktWriter = new WKTWriter();

    public Point fromWkt(String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            if (!(geometry instanceof Point point)) {
                throw new IllegalArgumentException("WKT должен быть POINT");
            }
            point.setSRID(4326);
            return point;
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный WKT: " + wkt, e);
        }
    }

    public String toWkt(Point point) {
        if (point == null) {
            return null;
        }
        return wktWriter.write(point);
    }
}
