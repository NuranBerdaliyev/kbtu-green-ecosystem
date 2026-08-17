package com.example.green.service.util;

import org.locationtech.jts.geom.Point;

public final class GeoUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}

    public static double distanceKm(Point a, Point b) {
        if (a == null || b == null) return 0.0;
        double lat1 = Math.toRadians(a.getY());
        double lat2 = Math.toRadians(b.getY());
        double dLat = Math.toRadians(b.getY() - a.getY());
        double dLon = Math.toRadians(b.getX() - a.getX());

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return EARTH_RADIUS_KM * c;
    }
}