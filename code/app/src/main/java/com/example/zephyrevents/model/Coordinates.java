package com.example.zephyrevents.model;

public class Coordinates {
    private double lat;
    private double lng;

    // no arg constructor for firebase
    public Coordinates() {};

    public Coordinates(double lat,double lng){
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
