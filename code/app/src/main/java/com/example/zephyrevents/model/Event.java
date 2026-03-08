package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;
import com.example.zephyrevents.util.TimeHelper;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * This is a class that defines an event.
 * Holds basic info (name, description, times) and optional detail fields
 * used on the event detail screen (price, location, organizer, waitlist capacity).
 */
public class Event {
    private  String id;
    private  String name;
    private String description;
    private  long startTime;
    private  long endTime;

    /** Optional: display price (e.g. "$50.00"). Used on event detail screen. */
    private String price;
    /** Optional: event location. Used on event detail screen. */
    private String location;
    /** Optional: organizer display name. Used on event detail screen. */
    private String organizerName;
    /** Optional: max number of participants. Used for waitlist info. */
    private int capacity;
    /** Optional: current number of applicants on waitlist. Used for waitlist info. */
    private int currentApplicants;
    /** Optional: registration end time (millis). Used for waitlist info. */
    private long registrationEndTime;

    // no arg constructor for firebase
    public Event() {}

    public Event(String id, String name, String description, long startTime,  long endTime ){
        this.name = name;
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    /**
     * Full constructor including detail fields for the event detail screen.
     */
    public Event(String id, String name, String description, long startTime, long endTime,
                 String price, String location, String organizerName,
                 int capacity, int currentApplicants, long registrationEndTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.location = location;
        this.organizerName = organizerName;
        this.capacity = capacity;
        this.currentApplicants = currentApplicants;
        this.registrationEndTime = registrationEndTime;
    }

    // Constructor to autogenerate string id.
    public Event(String name, String description, long startTime, long endTime){
        this(GenerateId.getUniqueId(), name, description, startTime, endTime);
    }

    /**
     * Returns true if the event waitlist has reached capacity (no more join allowed).
     */
    public boolean isCapacityFull() {
        return capacity > 0 && currentApplicants >= capacity;
    }

    public String getId() {
        return id;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getCurrentApplicants() { return currentApplicants; }
    public void setCurrentApplicants(int currentApplicants) { this.currentApplicants = currentApplicants; }
    public long getRegistrationEndTime() { return registrationEndTime; }
    public void setRegistrationEndTime(long registrationEndTime) { this.registrationEndTime = registrationEndTime; }
}
