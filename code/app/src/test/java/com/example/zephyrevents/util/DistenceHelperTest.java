package com.example.zephyrevents.util;

import static org.junit.Assert.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.zephyrevents.model.Coordinate;

import org.junit.Test;

/**
 * This class represents unit tests for the DistanceHelper package
 */
public class DistenceHelperTest {
    /**
     * This method turns latitude and longitude to a new Coordinate object
     * @param lat latitude of a geopoint
     * @param lng longitude of a geopoint
     * @return A new Coordinate object that contains the longitude and latitude
     */
    private Coordinate getTestCoord(double lat, double lng){
        return new Coordinate(lat, lng);
    }

    /**
     * A test point with the Coordinates representing ETLC on University Of Alberta Campus
     * @return A new Coordinate object that contains the longitude and latitude corresponding to the ETLC building on the Univeristy of Alberta Campus
     */
    private Coordinate etlcCoord(){
        var lat = 53.5277377444124;
        var lng = -113.52865642442397;
        return getTestCoord(lat, lng);
    }

    /**
     * A test point with the Coordinates representing CCIS on University Of Alberta Campus
     * @return A new Coordinate object that contains the longitude and latitude corresponding to the CCIS building on the Univeristy of Alberta Campus
     */
    private Coordinate ccisCoord(){
        var lat = 53.52813796322045;
        var lng = -113.52388454567081;
        return getTestCoord(lat, lng);
    }

    /**
     * A unit test that checks if DistanceHelper.isWithinDistance correctly returns false when two points are out of a short range.
     */
    @Test
    public void testOutBounds(){
        var etlc = etlcCoord();
        var ccis = ccisCoord();

        var val = DistanceHelper.isWithinDistance(etlc, ccis, 0.1);
        // Should result to false cause
        assertFalse(val);
    }

    /**
     * A unit test that checks if DistanceHelper.isWithinDistance correctly returns true when two points are within sufficient range.
     */
    @Test
    public void testInBounds(){
        var etlc = etlcCoord();
        var ccis = ccisCoord();

        var val = DistanceHelper.isWithinDistance(etlc, ccis, 5);
        //Should pass because ccis is within 5km of etlc
        assertTrue(val);
    }
    /**
     * A unit test that checks if DistanceHelper.isWithinDistance correctly returns true when two exact points are within a short distance.
     * Checks if less than or equal to check works.
     */
    @Test
    public void testEqual(){
        var etlc1 = etlcCoord();
        var etlc2 = etlcCoord();

        var val = DistanceHelper.isWithinDistance(etlc1, etlc2, 0.1);
        //Should pass because same area, even with tiny distanceKm.
        assertTrue(val);
    }

    /**
     *  Ensures {@code getUserLocation} calls {@code onFailure} and never {@code onLocation}
     * Check if onfailure is return when location permission isn't granted
     */


    @Test
    public void getUserLocation_noLocationPermissionGranted() {
        Context context = mock(Context.class);
        DistanceHelper.LocationCallback callback = mock(DistanceHelper.LocationCallback.class);
        try (MockedStatic<ContextCompat> compat = Mockito.mockStatic(ContextCompat.class)) {
            compat.when(() -> ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION))
                    .thenReturn(PackageManager.PERMISSION_DENIED);
            DistanceHelper.getUserLocation(context, callback);
            verify(callback).onFailure();
            verify(callback, never()).onLocation(org.mockito.ArgumentMatchers.anyDouble(),
                    org.mockito.ArgumentMatchers.anyDouble());
        }
    }

    /**
     * Units test that check that when location permission is granted, then location is granted with longitude and latitude
     */

    @Test
    @SuppressWarnings("unchecked")
    public void getUserLocation_LocationPermisionGranted() {
        Context context = mock(Context.class);
        DistanceHelper.LocationCallback callback = mock(DistanceHelper.LocationCallback.class);
        FusedLocationProviderClient client = mock(FusedLocationProviderClient.class);

        // Raw Task: avoids IDE/generic confusion with android.location.Location vs Play Services
        Task<?> lastLocationTask = mock(Task.class);
        when(client.getLastLocation()).thenReturn((Task) lastLocationTask);
        when(lastLocationTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn((Task) lastLocationTask);
        doAnswer(invocation -> {
            OnSuccessListener<android.location.Location> listener = invocation.getArgument(0);
            android.location.Location mockFix = mock(android.location.Location.class);
            when(mockFix.getLatitude()).thenReturn(53.5277);
            when(mockFix.getLongitude()).thenReturn(-113.5286);
            listener.onSuccess(mockFix);
            return lastLocationTask;
        }).when(lastLocationTask).addOnSuccessListener(any(OnSuccessListener.class));
        try (MockedStatic<ContextCompat> compat = Mockito.mockStatic(ContextCompat.class);
             MockedStatic<LocationServices> services = Mockito.mockStatic(LocationServices.class)) {
            compat.when(() -> ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION))
                    .thenReturn(PackageManager.PERMISSION_GRANTED);
            services.when(() -> LocationServices.getFusedLocationProviderClient(context))
                    .thenReturn(client);
            DistanceHelper.getUserLocation(context, callback);
            verify(callback).onLocation(53.5277, -113.5286);
            verify(callback, never()).onFailure();
        }
    }

}
