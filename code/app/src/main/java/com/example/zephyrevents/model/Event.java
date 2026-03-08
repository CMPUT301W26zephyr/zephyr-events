package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

/**
 * This is a class that defines an event.
 */
public class Event {
    private  String eventId;
    private  String name;
    private String description;
    private  long startTime;
    private  long endTime;
    private Location location;
    private double price;
    private int capacity;
    private int applicantCount;
    private long registrationEndTime;
    private String organizerId;
    private String imageUrl;
    private String status;

    // no arg constructor for firebase
    public Event() {};


    public Event(String name, String description, long startTime, long endTime, String location, double price, int capacity, int applicantCount, long registrationEndTime, String organizerId, String imageUrl, String status) {
        this.Id = GenerateId.getUniqueId();
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.price = price;
        this.capacity = capacity;
        this.applicantCount = applicantCount;
        this.registrationEndTime = registrationEndTime;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.status = status;
    }


    // Constructor to autogenerate string id.
    public Event(String name, String description, long startTime, long endTime){
        this(GenerateId.getUniqueId(), name, description, startTime, endTime);
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

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
