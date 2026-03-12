package com.example.zephyrevents.model;

import android.util.Log;

public class Location {
    public Coordinate coordinate;
    public String locationString;
    private static final String TAG = "Location";


    public Location(){}

    public Location(Coordinate coordinate,String locationString){
        this.coordinate = coordinate;
        this.locationString = locationString;
    }
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


