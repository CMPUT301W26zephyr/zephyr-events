package com.example.zephyrevents.model;

import android.os.Build;

import java.time.Instant;

/**
 * This is a class that defines a waitlist.
 */
public class WaitlistEntry {
    private String userId;
    private String eventId;
    private long timestamp; // Could use type Instant, but had android api version issues.

    // latitude and longitude for now, unless if android has a better way
    // used for potential location enforcement.
    private  double lat;
    private  double lng;
    // Using enum here instead of string.
    private EventStatus status;

    // no arg constructor for firebase
    public WaitlistEntry() {}
    public WaitlistEntry(
            String userId,
            String eventId,
            double lat,
            double lng
    ) {
        this.userId = userId;
        this.eventId = eventId;
        this.timestamp = System.currentTimeMillis();
        this.lat = lat;
        this.lng = lng;
        this.status = EventStatus.PENDING;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
    /**
     * The only Attribute in this class that should be mutable is status
     * The setters below are because firebase might have issues deserializing without them.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}