package com.example.zephyrevents.util;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigationevent.NavigationEventDispatcher;

import com.example.zephyrevents.model.Coordinate;
import com.google.android.gms.tasks.CancellationTokenSource;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import android.location.Location;


/**
 * Utility class providing helper methods for distance calculations and user location retrieval.
 * Uses the Haversine formula for great-circle distance between two geographic coordinates.
 */
public final class DistanceHelper {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DistanceHelper() {
    }

    ;

    /**
     * Callback interface for receiving the result of a location request.
     */
    public interface LocationCallback {
        /**
         * Called when the user's location is successfully retrieved.
         *
         * @param lat the latitude of the user's current location
         * @param lng the longitude of the user's current location
         */
        void onLocation(double lat, double lng);

        /**
         * Called when the location request fails, for example due to missing
         * permissions or unavailable location services.
         */
        void onFailure();
    }

    /**
     * This is an internal method to calculate the km distance between two Coordinates
     * This is pretty similar to some stuff i've done previously so Haversine is what is needed
     * https://www.geeksforgeeks.org/dsa/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
     * ^ Here is a site where I got the java implementation of the math.
     *
     * @param cord1 A coordinate point you want to compare with another.
     * @param cord2 A coordinate point you want to compare with another.
     * @return The shortest km distance between two coordinate points
     */
    private static double haversineDistance(Coordinate cord1, Coordinate cord2) {
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
     *
     * @param cord1      A coordinate point you want to compare with another.
     * @param cord2      A coordinate point you want to compare with another.
     * @param distanceKm The maximum allowed distance between two coordinates.
     * @return A boolean value, if two coordinates further than distanceKm away from each other.
     */
    public static boolean isWithinDistance(Coordinate cord1, Coordinate cord2, double distanceKm) {
        return (haversineDistance(cord1, cord2) <= distanceKm);
    }


    // The following two functions are from Anthropic, Claude (claude.ai), "Android fused location provider helper methods to get current user location", 2025-03-30

    /**
     * Returns the best available last-known location or requests a fresh fix if none is cached.
     * @param context used for permission check and location client
     * @param callback receives coordinate or failure
     */
    public static void getUserLocation(Context context, LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            callback.onFailure();
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                callback.onLocation(location.getLatitude(), location.getLongitude());
            } else {
                requestFreshLocation(context, client, callback);
            }
        }).addOnFailureListener(e -> callback.onFailure());
    }

    /**
     * Requests a single high-accuracy current location when
     * @param context  used for permission checks
     * @param client fused location client to call
     * @param callback receives latitude/longitude on sucesss
     */
    private static void requestFreshLocation(
            Context context,
            FusedLocationProviderClient client,
            LocationCallback callback
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            callback.onFailure();
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();

        client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.getToken()
        ).addOnSuccessListener(location -> {
            if (location != null) {
                callback.onLocation(location.getLatitude(), location.getLongitude());
            } else {
                callback.onFailure();
            }
        }).addOnFailureListener(e -> callback.onFailure());
    }
}
