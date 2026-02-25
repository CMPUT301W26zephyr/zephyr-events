package com.example.zephyrevents;

import static org.junit.Assert.*;


import com.example.zephyrevents.model.Coordinate;
import com.example.zephyrevents.util.DistanceHelper;

import org.junit.Test;

public class DistenceHelperTest {
    private Coordinate getTestCoord(double lat, double lng){
        return new Coordinate(lat, lng);
    }
    private Coordinate etlcCoord(){
        var lat = 53.5277377444124;
        var lng = -113.52865642442397;
        return getTestCoord(lat, lng);
    }
    private Coordinate ccisCoord(){
        var lat = 53.52813796322045;
        var lng = -113.52388454567081;
        return getTestCoord(lat, lng);
    }


    @Test
    public void testOutBounds(){
        var etlc = etlcCoord();
        var ccis = ccisCoord();

        var val = DistanceHelper.isWithinDistance(etlc, ccis, 0.1);
        // Should result to false cause
        assertFalse(val);
    }
    @Test
    public void testInBounds(){
        var etlc = etlcCoord();
        var ccis = ccisCoord();

        var val = DistanceHelper.isWithinDistance(etlc, ccis, 5);
        //Should pass because ccis is within 5km of etlc
        assertTrue(val);
    }
}
