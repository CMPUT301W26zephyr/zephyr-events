package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

/**
 * This is a class that defines an event.
 */
public class Event {
    private  String eventId;
    private  String name;
    private String description;
    private EventTime time;
    private Location location;
    private double price;
    private int capacity;
    private int applicantCount;
    private long registrationEndTime;
    private String organizerId;
    private String imageUrl;
    private EventStatus status;

    // no arg constructor for firebase
    public Event() {};


    public Event(String name, String description, long startTime, long endTime, String location,long lng,long lat,  double price, int capacity, int applicantCount, long registrationEndTime, String organizerId, String imageUrl, EventStatus status) {
        this.eventId = GenerateId.getUniqueId();
        this.name = name;
        this.description = description;
        this.time = new EventTime(startTime, endTime);
        this.location = new Location(lat,lng,location);
        this.price = price;
        this.capacity = capacity;
        this.applicantCount = applicantCount;
        this.registrationEndTime = registrationEndTime;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EventTime getTime() {
        return time;
    }

    public void setTime(EventTime time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public int getApplicantCount() {
        return applicantCount;
    }

    public void setApplicantCount(int applicantCount) {
        this.applicantCount = applicantCount;
    }

    public long getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(long registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}
