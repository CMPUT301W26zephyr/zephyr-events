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
    private  long startTime;
    private  long endTime;

    Event(String name, long endTime){
        this.id = GenerateId.getUniqueId();
        this.name = name;
        this.startTime = TimeHelper.now();;
        this.endTime = endTime;
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
