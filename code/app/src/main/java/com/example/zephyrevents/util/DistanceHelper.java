package com.example.zephyrevents.util;


import com.example.zephyrevents.model.Coordinate;

public final class DistanceHelper {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private DistanceHelper() {};


    /**
     * This is an internal function to calculate the km distance between two Coordinates
     * This is pretty similar to some stuff i've done previously so Haversine is what is needed
     * https://www.geeksforgeeks.org/dsa/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
     * ^ Here is a site where I got the java implementation of the math.
     *
     * @param cord1 A coordinate point you want to compare with another.
     * @param cord2 A coordinate point you want to compare with another.
     * @return The shortest km distance between two coordinate points
     */
    private static double haversineDistance(Coordinate cord1, Coordinate cord2){
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(cord1.getLat() - cord2.getLat());
        double dLon = Math.toRadians(cord1.getLng() - cord2.getLng());

        // convert to radians
        var lat1 = Math.toRadians(cord1.getLat());
        var lat2 = Math.toRadians(cord2.getLat());

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Publicly exposed function that abstracts. If you need a boolean for whether a point is
     * within a certain km distance radius of another point, use this.
     * @param cord1 A coordinate point you want to compare with another.
     * @param cord2 A coordinate point you want to compare with another.
     * @param distanceKm The maximum allowed distance between two coordinates.
     * @return A boolean value, if two coordinates further than distanceKm away from each other.
     */
    public static boolean isWithinDistance(Coordinate cord1, Coordinate cord2, double distanceKm){
        return (haversineDistance(cord1, cord2) <= distanceKm);
    }

    }