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
                throw new IllegalArgumentException("WKT must contain POINT");
            }

            double longitude = point.getX();
            double latitude = point.getY();

            if (!Double.isFinite(longitude)
                    || !Double.isFinite(latitude)) {
                throw new IllegalArgumentException("Coordinates must be finite numbers");
            }

            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }

            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
            point.setSRID(4326);
            return point;

        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid WKT: " + wkt,
                    exception
            );
        }
    }

    public String toWkt(Point point) {
        if (point == null) {
            return null;
        }
        return wktWriter.write(point);
    }
}
