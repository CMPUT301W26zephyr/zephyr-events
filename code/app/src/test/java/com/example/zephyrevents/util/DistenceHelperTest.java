package com.example.zephyrevents.util;

import static org.junit.Assert.*;


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
     * Checks if <= less than or equal to check works.
     */
    @Test
    public void testEqual(){
        var etlc1 = etlcCoord();
        var etlc2 = etlcCoord();

        var val = DistanceHelper.isWithinDistance(etlc1, etlc2, 0.1);
        //Should pass because same area, even with tiny distanceKm.
        assertTrue(val);
    }
}
