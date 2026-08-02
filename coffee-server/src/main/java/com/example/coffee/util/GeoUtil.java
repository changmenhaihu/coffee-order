package com.example.coffee.util;

public class GeoUtil {

    private static final double EARTH_RADIUS = 6371000.0;

    private GeoUtil() {}

    public static double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0fm", meters);
        }
        return String.format("%.1fkm", meters / 1000);
    }
}
