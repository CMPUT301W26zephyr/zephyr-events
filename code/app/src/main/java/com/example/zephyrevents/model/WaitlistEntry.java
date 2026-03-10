package com.example.zephyrevents.model;

import com.example.zephyrevents.util.TimeHelper;

/**
 * This is a class that defines a waitlist.
 */
public class WaitlistEntry {
    private String userId;
    private String eventId;
    private long timestamp; // Time user joined in millis

    private Coordinate coordinate;
    // Using enum here instead of string.
    private Status status;

    // no arg constructor for firebase
    public WaitlistEntry() {}
    public WaitlistEntry(
            String userId,
            String eventId,
            double lat,
            double lng,
            Status status
    ) {
        this.userId = userId;
        this.eventId = eventId;
        this.timestamp = TimeHelper.now();
        this.status = status;
        this.coordinate = new Coordinate(lat,lng);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    /**
     * The only Attribute in this class that should be mutable is status and maybe coordinates
     * The setters below are because firebase might have issues deserializing without them.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Coordinate getCoordinates() {
        return coordinate;
    }

    public void setCoordinates(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}