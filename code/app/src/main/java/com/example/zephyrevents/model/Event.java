package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;
import com.example.zephyrevents.util.TimeHelper;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * This is a class that defines an event.
 */
public class Event {
    private  String id;
    private  String name;
    private String description;
    private  long startTime;
    private  long endTime;

    // no arg constructor for firebase
    public Event() {};

    public Event(String id, String name, String description, long startTime,  long endTime ){
        this.name = name;
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }


    // Constructor to autogenerate string id.
    public Event(String name, String description, long startTime, long endTime){
        this(GenerateId.getUniqueId(), name, description, startTime, endTime);
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
}
