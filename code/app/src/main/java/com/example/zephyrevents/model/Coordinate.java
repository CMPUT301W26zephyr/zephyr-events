package com.example.zephyrevents.model;

public class Coordinate {
    private double lat;
    private double lng;

    // no arg constructor for firebase
    public Coordinate() {};

    public Coordinate(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
