package com.example.zephyrevents.model;

import android.util.Log;

public class Location {
    public Coordinate coordinate;
    public String location;
    private static final String TAG = "Location";


    Location(){}

    Location(Coordinate coordinate,String location){
        this.coordinate = coordinate;
        this.location = location;
    }
    Location(double lat,double lng,String location){
        this(new Coordinate(lat, lng), location);
    }


    Location(Coordinate coordinate){
        this(coordinate, "Location not set");
        Log.d(TAG, "Location name missing for coordinate: " + coordinate);
    }
}


