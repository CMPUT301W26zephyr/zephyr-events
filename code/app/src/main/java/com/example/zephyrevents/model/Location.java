package com.example.zephyrevents.model;

import android.util.Log;

/**
 * Encapsulates a location as a set of coordinates (Coordinate) and a string representing the City/Province.
 */
public class Location {
    public Coordinate coordinate;
    public String locationString;
    private static final String TAG = "Location";


    public Location(){}

    /**
     * Constructor that accepts a Coordinate object and a locationString
     * @param coordinate
     * @param locationString
     */
    public Location(Coordinate coordinate,String locationString){
        this.coordinate = coordinate;
        this.locationString = locationString;
    }

    /**
     * Constructor that accepts coordinates (longitude, latitude) and a locationString
     * Creates Coordinate object from coordinates
     * @param lat
     * @param lng
     * @param locationString
     */
    public Location(double lat,double lng,String locationString){
        this(new Coordinate(lat, lng), locationString);
    }


    Location(Coordinate coordinate){
        this(coordinate, "Location not set");
        Log.d(TAG, "Location name missing for coordinate: " + coordinate);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getLocationString() {
        return locationString;
    }

    public void setLocationString(String locationString) {
        this.locationString = locationString;
    }
}


