package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a class that defines an event.
 * Holds basic info (name, description, times) and optional detail fields
 * used on the event detail screen (price, location, organizer, waitlist capacity).
 */
public class Event {
    private String eventId;
    private String name;
    private String description;
    private EventTime time;
    private Location location;
    private double price;
    private int capacity;
    private int applicantCount;
    private Integer waitlistCapacity;
    private long registrationStartTime;
    private long registrationEndTime;
    private String organizerId;
    private String imageUrl;
    private EventStatus status;
    private String organizerName;
    private int currentApplicants;
    private int commentsCount;

    /** If true, event is hidden from public listings and has no promotional QR. */
    private boolean privateEvent;
    /** Users who may manage the event alongside the primary organizer. */
    private List<String> coOrganizerUserIds;
    /** Entrants invited to the private waitlist but who have not accepted yet. */
    private List<String> pendingPrivateWaitlistInviteUserIds;
    private List<String> pendingCoOrganizerUserIds;


    public Event() {
    }

    /**
     * Constructor with essential parameters
     *
     * @param id
     * @param name
     * @param description
     * @param startTime
     * @param endTime
     */
    public Event(String id, String name, String description, long startTime,  long endTime ){
        this.eventId = GenerateId.getUniqueId();
        this.name = name;
        this.time = new EventTime(startTime, endTime);
        this.description = description;
    }

    /**
     * Full constructor including detail fields for the event detail screen.
     *
     * @param name
     * @param description
     * @param startTime
     * @param endTime
     * @param location
     * @param lng
     * @param lat
     * @param price
     * @param capacity
     * @param applicantCount
     * @param registrationEndTime
     * @param organizerId
     * @param imageUrl
     * @param status
     */
    public Event(String name, String description, long startTime, long endTime, String location, long lng, long lat, double price, int capacity, int applicantCount, long registrationEndTime, String organizerId, String imageUrl, EventStatus status) {
        this.eventId = GenerateId.getUniqueId();
        this.name = name;
        this.description = description;
        this.time = new EventTime(startTime, endTime);
        this.location = new Location(lat, lng, location);
        this.price = price;
        this.capacity = capacity;
        this.applicantCount = applicantCount;
        this.registrationEndTime = registrationEndTime;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    // Constructor to autogenerate string id.
    public Event(String name, String description, EventTime time, Location location, double price, int capacity, int applicantCount, long registrationEndTime, String organizerId, String imageUrl, EventStatus status, String organizerName, int currentApplicants) {
        this(GenerateId.getUniqueId(), name, description, time, location, price, capacity, applicantCount, registrationEndTime, organizerId, imageUrl, status, organizerName, currentApplicants);
    }

    public Event(String eventId, String name, String description, EventTime time, Location location, double price, int capacity, int applicantCount, long registrationEndTime, String organizerId, String imageUrl, EventStatus status, String organizerName, int currentApplicants) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.time = time;
        this.location = location;
        this.price = price;
        this.capacity = capacity;
        this.applicantCount = applicantCount;
        this.registrationEndTime = registrationEndTime;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.status = status;
        this.organizerName = organizerName;
        this.currentApplicants = currentApplicants;
    }

    /**
     * Returns true if the event waitlist has reached capacity (no more join allowed).
     *
     * @return  true if event waitlist has reached capacity, otherwise false.
     */
    public boolean isCapacityFull() {
        return waitlistCapacity != null && waitlistCapacity > 0 && currentApplicants >= waitlistCapacity;
    }

    public void setCapacityFull(boolean capacityFull) {}  // Does nothing as this is computed property, prevents constant Firebase warnings

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public EventTime getTime() {
        return time;
    }

    public void setTime(EventTime time) {
        this.time = time;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public long getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(long registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
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

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public int getCurrentApplicants() {
        return currentApplicants;
    }

    public void setCurrentApplicants(int currentApplicants) {
        this.currentApplicants = currentApplicants;
    }

    public Integer getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public void setWaitlistCapacity(Integer waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

    public boolean isPrivateEvent() {
        return privateEvent;
    }

    public void setPrivateEvent(boolean privateEvent) {
        this.privateEvent = privateEvent;
    }

    public List<String> getCoOrganizerUserIds() {
        if (coOrganizerUserIds == null) {
            coOrganizerUserIds = new ArrayList<>();
        }
        return coOrganizerUserIds;
    }

    public void setCoOrganizerUserIds(List<String> coOrganizerUserIds) {
        this.coOrganizerUserIds = coOrganizerUserIds;
    }

    public List<String> getPendingPrivateWaitlistInviteUserIds() {
        if (pendingPrivateWaitlistInviteUserIds == null) {
            pendingPrivateWaitlistInviteUserIds = new ArrayList<>();
        }
        return pendingPrivateWaitlistInviteUserIds;
    }

    public void setPendingPrivateWaitlistInviteUserIds(List<String> pendingPrivateWaitlistInviteUserIds) {
        this.pendingPrivateWaitlistInviteUserIds = pendingPrivateWaitlistInviteUserIds;
    }

    public List<String> getPendingCoOrganizerUserIds() {
        if (pendingCoOrganizerUserIds == null) {
            pendingCoOrganizerUserIds = new ArrayList<>();
        }
        return pendingCoOrganizerUserIds;
    }

    public void setPendingCoOrganizerUserIds(List<String> pendingCoOrganizerUserIds) {
        this.pendingCoOrganizerUserIds = pendingCoOrganizerUserIds;
    }

    public boolean hasPosterImage(){
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
}