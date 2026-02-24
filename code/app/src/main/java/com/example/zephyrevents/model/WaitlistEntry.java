package com.example.zephyrevents.model;

/**
 * This is a class that defines a waitlist.
 */
public class WaitlistEntry {
    private String userId;
    private String eventId;
    private long timestamp; // Could use type Instant, but had android api version issues.

    private Coordinate coordinate;
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
        this.status = EventStatus.PENDING;
        this.coordinate = new Coordinate(lat,lng);
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